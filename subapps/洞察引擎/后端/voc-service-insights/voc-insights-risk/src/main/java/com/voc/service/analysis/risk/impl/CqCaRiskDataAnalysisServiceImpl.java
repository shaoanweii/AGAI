package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.risk.constant.ChannelMappingConstant;
import com.voc.service.analysis.risk.constant.DeduplicationCompareResult;
import com.voc.service.analysis.risk.entity.CarDataEntity;
import com.voc.service.analysis.risk.entity.ReportModelTagsResultDataRiskEntity;
import com.voc.service.analysis.risk.kafka.RiskDataProducer;
import com.voc.service.analysis.risk.mapper.RiskDataAnalysisMapper;
import com.voc.service.analysis.risk.utils.DailyResetCodeGenerator;
import com.voc.service.analysis.risk.utils.TaskTimeWindowUtil;
import com.voc.service.analysis.risk.utils.TwoSetDeduplicationUtil;
import com.voc.service.analysis.risk.utils.XxlJobCornManager;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.ICqCaRiskDataAnalysisService;
import com.voc.service.insights.engine.api.IInsAccountLexiconService;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.IInsClosedRuleService;
import com.voc.service.insights.engine.api.constants.ContentTypeEnum;
import com.voc.service.insights.engine.api.data.InsDataResourceDescService;
import com.voc.service.insights.engine.api.data.InsDataResourceService;
import com.voc.service.insights.engine.api.model.WarningTaskRunModel;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import com.voc.service.insights.engine.vo.InsAccountLexiconVo;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class CqCaRiskDataAnalysisServiceImpl implements ICqCaRiskDataAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CqCaRiskDataAnalysisServiceImpl.class);

    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String clientId = "764547797eb2e192763f5334028d49c9";
    private final String ruleConfigId = "1";
    @Autowired
    RiskDataAnalysisMapper riskDataAnalysisMapper;

    @Autowired
    IInsClosedRuleService iInsClosedRuleService;

    @Autowired
    RiskDataProducer riskDataProducer;

    @Autowired
    InsDataResourceService insDataResourceService;

    @Autowired
    InsDataResourceDescService insDataResourceDescService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    IInsAccountLexiconService iInsAccountLexiconService;

    @Autowired
    DailyResetCodeGenerator dailyResetCodeGenerator;

    @Autowired
    IInsChannelInfoService iInsChannelInfoService;


    @Autowired
    ReportModelTagsResultDataRiskImpl reportModelTagsResultDataRiskImpl;

    @Autowired
    TwoSetDeduplicationUtil twoSetDeduplicationUtil;

    @Autowired
    XxlJobCornManager xxlJobCornManager;

    private static final String RISK_DATA_ID_KEY_PREFIX = "risk:dataId:distinct";


    @Override
    public Boolean createJob() {
        log.info("创建长安风险数据任务");
        List<String> ruleIdList = riskDataAnalysisMapper.queryRuleIdList();
        log.info("获取长安风险数据任务:{}", ruleIdList.size());
        if (CollectionUtil.isEmpty(ruleIdList)) {
            log.info("没有需要执行的长安风险数据任务");
            return Boolean.FALSE;
        }
        int deleted = riskDataAnalysisMapper.deleteJobIdList();
        log.info("删除任务条数:{}", deleted);
        xxlJobCornManager.batchCreateXxlJobForRuleIds(ruleIdList);
        log.info("添加长安风险数据任务完成");
        return Boolean.TRUE;
    }

    @Override
    public Boolean delJob(List<String> ruleIdList) {
        log.info("删除长安风险数据任务:{}", ruleIdList);
        if (CollectionUtil.isEmpty(ruleIdList)) {
            log.info("没有需要删除的长安风险数据任务");
            return Boolean.FALSE;
        }
        int delJob = riskDataAnalysisMapper.delJob(ruleIdList);
        return delJob > 0;
    }

    /**
     * 预警单点闭环规则
     *
     * @param param
     * @return
     */
    @Override
    public Boolean warningTaskRun(WarningTaskRunModel param) {
        String ruleId = param.getRuleId();
        log.info("开始执行长安风险数据任务, ruleId:{}", ruleId);
        InsClosedRuleModel insClosedRuleModel = iInsClosedRuleService.queryRuleDetail(ruleId);
        if (ObjectUtils.isEmpty(insClosedRuleModel)) {
            log.info("没有找到对应的长安风险数据任务, ruleId:{}", ruleId);
            return Boolean.FALSE;
        }
        // 设置时间窗口
        this.setupTimeWindow(param);

        List<InsClosedRuleConditionModel> conditions = insClosedRuleModel.getConditions();
        if (CollectionUtil.isEmpty(conditions)) {
            log.info("没有条件配置, ruleId:{}", ruleId);
            return Boolean.FALSE;
        }
        // 初始化条件模型
        String brandCode = insClosedRuleModel.getBrandCode();
        log.info("获取品牌code, ruleId:{}, brandCode:{}", ruleId, brandCode);
        WarningTaskConditionsModel model = new WarningTaskConditionsModel();
        model.setBrandCode(brandCode);
        model.setChannelIds(insClosedRuleModel.getDataSource());
        model.setStartTime(param.getStartTime());
        model.setEndTime(param.getEndTime());
        this.parseConditions(conditions, model);
        log.info("获取长安风险数据任务条件, ruleId:{}", ruleId);
        // 查询原始数据
        List<WarningTaskResultModel> warningTaskResultModels = riskDataAnalysisMapper.querySoundsData(model);
        log.info("获取长安风险数据任务结果, ruleId:{}, size:{}", ruleId, warningTaskResultModels.size());
        if (CollectionUtil.isEmpty(warningTaskResultModels)) {
            log.info("没有获取长安风险数据任务结果, ruleId:{}", ruleId);
            return Boolean.FALSE;
        }
        // 处理过滤逻辑
        List<WarningTaskResultModel> filterWarningTaskList = this.processFilterLogic(
                conditions, warningTaskResultModels);
        log.info("处理长安风险数据任务结果, ruleId:{}, size:{}", ruleId, filterWarningTaskList.size());
        if (CollectionUtil.isEmpty(filterWarningTaskList)) {
            return Boolean.FALSE;
        }
        // 组装并发送数据
        List<ReportModelTagsResultDataRiskEntity> reportModelTagsResultDataRiskEntityList =
                this.assembleData(filterWarningTaskList, insClosedRuleModel);
        //发送数据之前走舆情公共数据去重逻辑
        List<ReportModelTagsResultDataRiskEntity> entityList = dataDistinct(reportModelTagsResultDataRiskEntityList);
        log.info("对比舆情公关数据去重结果, ruleId:{}, size:{}", ruleId, entityList.size());
        List<ReportModelTagsResultDataRiskEntity> resultDataRiskEntityList = processRiskData(entityList);
        log.info("发送长安风险数据redis去重任务结果, ruleId:{}, size:{}", ruleId, resultDataRiskEntityList.size());
        if (CollectionUtil.isEmpty(resultDataRiskEntityList)) {
            return Boolean.FALSE;
        }
        riskDataProducer.pushData(MessageDTO.builder().data(resultDataRiskEntityList).build());
        log.info("结束长安风险数据任务结果, ruleId:{}", ruleId);
        return Boolean.TRUE;
    }


    public List<ReportModelTagsResultDataRiskEntity> processRiskData(List<ReportModelTagsResultDataRiskEntity> entityList) {
        // 1. 空集合直接返回空列表
        entityList = deduplicateByDataIdWithPriority(entityList);
        if (CollectionUtils.isEmpty(entityList)) {
            log.info("入参entityList为空，直接返回空列表");
            return List.of();
        }

        // 2. 提取所有非空的dataId
        List<String> dataIdList = entityList.stream()
                .map(ReportModelTagsResultDataRiskEntity::getDataId) // 确保实体类有getDataId()方法
                .filter(dataId -> dataId != null && !dataId.isEmpty()) // 过滤空dataId
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(dataIdList)) {
            log.warn("entityList中所有dataId为空");
            return List.of();
        }
        // 3. 构建Redis集合Key（关联ruleId，避免不同规则数据冲突）
        String redisKey = RISK_DATA_ID_KEY_PREFIX;
        // 4. 批量获取缓存中已存在的dataId
        Set<String> existDataIds = stringRedisTemplate.opsForSet().members(redisKey);
        // 5. 筛选出缓存中不存在的新dataId
        List<String> newDataIds = dataIdList.stream()
                .filter(dataId -> existDataIds == null || !existDataIds.contains(dataId))
                .collect(Collectors.toList());

        // 6. 若没有新dataId，直接返回空列表
        if (CollectionUtils.isEmpty(newDataIds)) {
            log.info("所有dataId已存在Redis缓存中，无新数据处理");
            return List.of();
        }

        // 7. 过滤entityList：只保留dataId在newDataIds中的元素（核心需求）
        List<ReportModelTagsResultDataRiskEntity> filteredEntityList = entityList.stream()
                .filter(entity -> {
                    String dataId = entity.getDataId();
                    return dataId != null && newDataIds.contains(dataId);
                })
                .collect(Collectors.toList());

        try {
            // 8. 将新dataId批量存入Redis集合
            Long addCount = stringRedisTemplate.opsForSet().add(redisKey, newDataIds.toArray(new String[0]));
            // 设置缓存过期时间
            stringRedisTemplate.expire(redisKey, 7, TimeUnit.DAYS);

            log.info("成功缓存新dataId数量:{}, 过期时间:{}小时",
                    addCount, 24);
        } catch (Exception e) {
            log.error("缓存新dataId失败, newDataIds:{}", newDataIds, e);
            return List.of();
        }
        // 9. 返回过滤后的新数据列表
        log.info("最终返回的新数据数量:{}", filteredEntityList.size());
        return filteredEntityList;
    }


    // 核心方法：按dataId去重，优先取intentionType=抱怨，无则随机取（仅处理重复dataId）
    public List<ReportModelTagsResultDataRiskEntity> deduplicateByDataIdWithPriority(
            List<ReportModelTagsResultDataRiskEntity> entityList) {

        // 1. 空值/空列表校验，避免空指针
        if (entityList == null || entityList.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 按dataId分组：key=dataId，value=该dataId对应的所有实体
        Map<Object, List<ReportModelTagsResultDataRiskEntity>> groupByDataId = entityList.stream()
                .collect(Collectors.groupingBy(ReportModelTagsResultDataRiskEntity::getDataId));

        // 3. 初始化随机数生成器（提升随机性）
        Random random = new Random(System.currentTimeMillis());

        // 4. 遍历分组处理：仅处理重复dataId，优先选抱怨类型
        List<ReportModelTagsResultDataRiskEntity> resultList = new ArrayList<>();
        for (Map.Entry<Object, List<ReportModelTagsResultDataRiskEntity>> entry : groupByDataId.entrySet()) {
            List<ReportModelTagsResultDataRiskEntity> groupList = entry.getValue();

            // 情况1：dataId无重复（列表长度=1），直接保留
            if (groupList.size() == 1) {
                resultList.add(groupList.get(0));
                continue;
            }

            // 情况2：dataId有重复，优先筛选intentionType=抱怨的条目
            List<ReportModelTagsResultDataRiskEntity> complainList = groupList.stream()
                    .filter(entity -> "抱怨".equals(entity.getIntentionType()))
                    .collect(Collectors.toList());

            ReportModelTagsResultDataRiskEntity selectedEntity;
            if (!complainList.isEmpty()) {
                // 有抱怨类型，选取第一条（若有多条抱怨，可改为随机取：complainList.get(random.nextInt(complainList.size()))）
                selectedEntity = complainList.get(0);
            } else {
                // 无抱怨类型，随机取一条
                selectedEntity = groupList.get(random.nextInt(groupList.size()));
            }
            resultList.add(selectedEntity);
        }

        return resultList;
    }


    private List<ReportModelTagsResultDataRiskEntity> dataDistinct(List<ReportModelTagsResultDataRiskEntity> entityList) {
        List<CarDataEntity> dayApiData = riskDataAnalysisMapper.toDayApiData();
        log.info("获取舆情公关日数据, size:{}", dayApiData.size());
        if (CollectionUtils.isEmpty(dayApiData)) {
            return entityList;
        }
        List<ReportModelTagsResultDataRiskEntity> newEntityList = this.assembleDistinctData(dayApiData);
        DeduplicationCompareResult deduplicationCompareResult = twoSetDeduplicationUtil.compareAndDeduplicate(newEntityList, entityList);
        List<ReportModelTagsResultDataRiskEntity> onlyInA = deduplicationCompareResult.getOnlyInA();
        onlyInA.addAll(deduplicationCompareResult.getDuplicateData());
        if (!CollectionUtils.isEmpty(onlyInA)) {
            List<String> codeList = onlyInA.stream().map(ReportModelTagsResultDataRiskEntity::getSoundsId).toList();
            // 使用Redis优化查询已存在的数据
            String redisKey = "risk_data:sounds_ids";
            Set<String> existIds = stringRedisTemplate.opsForSet().members(redisKey);
            if (existIds != null) {
                log.info("获取已存在数据, size:{}", existIds.size());
            }
            // 如果Redis中没有缓存，则从数据库加载并设置缓存
            if (CollectionUtils.isEmpty(existIds)) {
                QueryWrapper<ReportModelTagsResultDataRiskEntity> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("sounds_id", codeList);
                List<ReportModelTagsResultDataRiskEntity> riskEntities = reportModelTagsResultDataRiskImpl.list(queryWrapper);
                // 将结果存入Redis
                if (CollectionUtils.isNotEmpty(riskEntities)) {
                    Set<String> soundsIds = riskEntities.stream()
                            .map(ReportModelTagsResultDataRiskEntity::getSoundsId)
                            .collect(Collectors.toSet());
                    stringRedisTemplate.opsForSet().add(redisKey, soundsIds.toArray(new String[0]));
                    stringRedisTemplate.expire(redisKey, 48, TimeUnit.HOURS); // 设置过期时间
                    existIds = soundsIds;
                }
            }
            List<ReportModelTagsResultDataRiskEntity> filteredData = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(existIds)) {
                // 过滤出Redis中不存在的数据
                for (ReportModelTagsResultDataRiskEntity data : onlyInA) {
                    if (!existIds.contains(data.getSoundsId())) {
                        filteredData.add(data);
                    }
                }
            } else if (CollectionUtils.isEmpty(existIds)) {
                log.info("数据库redis第一次新增:{}", onlyInA.size());
                filteredData = onlyInA;
            }
            log.info("要新增的舆情公关数据, size:{}", filteredData.size());
            if (!CollectionUtils.isEmpty(filteredData)) {
                // 将新增的sounds_id添加到Redis中 避免后续重复数据
                List<String> newSoundsIds = filteredData.stream()
                        .map(ReportModelTagsResultDataRiskEntity::getSoundsId)
                        .toList();
                stringRedisTemplate.opsForSet().add(redisKey, newSoundsIds.toArray(new String[0]));
                // 保存到数据库
                reportModelTagsResultDataRiskImpl.saveBatch(filteredData);
            }
        }
        log.info("数据去重结果, size:{}", deduplicationCompareResult.getOnlyInB().size());
        return deduplicationCompareResult.getOnlyInB();
    }

    /**
     * 洞察引擎测试接口
     *
     * @param param
     * @return
     */
    @Override
    public Boolean warningTestTaskRun(WarningTaskRunModel param) {
        log.info("开始执行长安风险数据任务测试:{}", param);
        String ruleId = param.getRuleId();
        String batchId = param.getBatchId();
        InsClosedRuleModel insClosedRuleModel = iInsClosedRuleService.queryRuleDetail(ruleId);
        if (ObjectUtils.isEmpty(insClosedRuleModel)) {
            return Boolean.FALSE;
        }
        List<InsClosedRuleConditionModel> conditions = insClosedRuleModel.getConditions();
        if (CollectionUtil.isEmpty(conditions)) {
            return Boolean.FALSE;
        }
        // 初始化条件模型
        String brandCode = insClosedRuleModel.getBrandCode();
        log.info("获取测试任务品牌code, ruleId:{}, brandCode:{}", ruleId, brandCode);
        WarningTaskConditionsModel model = new WarningTaskConditionsModel();
        model.setBrandCode(brandCode);
        model.setChannelIds(insClosedRuleModel.getDataSource());
        model.setBatchId(batchId);
        this.parseConditions(conditions, model);
        // 查询原始数据
        List<WarningTaskResultModel> warningTaskResultModels = riskDataAnalysisMapper.executeRuleTestInfo(model);
        log.info("获取测试任务, ruleId:{}, size:{}", ruleId, warningTaskResultModels.size());
        if (CollectionUtil.isEmpty(warningTaskResultModels)) {
            log.info("获取测试任务数据结果, ruleId:{}", ruleId);
            return Boolean.FALSE;
        }
        // 处理过滤逻辑
        List<WarningTaskResultModel> filterWarningTaskList = this.processFilterLogic(
                conditions, warningTaskResultModels);
        log.info("获取测试任务风险数据任务结果, ruleId:{}, size:{}", ruleId, filterWarningTaskList.size());
        if (CollectionUtil.isEmpty(filterWarningTaskList)) {
            return Boolean.FALSE;
        }
        // 组装并发送数据
        this.assembleRuleTestData(filterWarningTaskList, insClosedRuleModel);
        riskDataProducer.pushTestData(MessageDTO.builder().data(filterWarningTaskList).build());
        log.info("结束长安测试数据任务结果, ruleId:{}", ruleId);
        return Boolean.TRUE;
    }

    @Override
    public Boolean publicOpinionDistinct() {
        List<ReportModelTagsResultDataRiskEntity> entityList = riskDataAnalysisMapper.queryResultDataRisk();
        log.info("开始执行长安风险数据去重任务");
        if (CollectionUtils.isEmpty(entityList)) {
            return Boolean.FALSE;
        }
        List<ReportModelTagsResultDataRiskEntity> resultDataRiskEntityList = dataDistinct(entityList);
        log.info("开始执行长安风险数据去重任务, size:{}", resultDataRiskEntityList.size());
        return Boolean.TRUE;
    }

    private void setupTimeWindow(WarningTaskRunModel param) {
        TaskTimeWindowUtil.TimeWindow timeWindow = TaskTimeWindowUtil.calculateTimeWindow();
        String startStr = timeWindow.getWindowStart().format(DB_FORMATTER);
        String endStr = timeWindow.getWindowEnd().format(DB_FORMATTER);
        log.info("查询数据窗口：{} 至 {}", startStr, endStr);
        if (StringUtils.isEmpty(param.getStartTime())) {
            param.setStartTime(startStr);
            param.setEndTime(endStr);
        }
    }

    private List<WarningTaskResultModel> processFilterLogic(
            List<InsClosedRuleConditionModel> conditions,
            List<WarningTaskResultModel> warningTaskResultModels) {

        log.info("开始处理原文和标题匹配规则:{}", warningTaskResultModels.size());

        List<WarningTaskResultModel> filterWarningTaskList = new ArrayList<>();
        // 分别处理 content 和 title 条件
        List<InsClosedRuleConditionModel> contentConditions = conditions.stream()
                .filter(condition -> StringUtils.equals(condition.getConditionType(), "content"))
                .toList();
        List<InsClosedRuleConditionModel> titleConditions = conditions.stream()
                .filter(condition -> StringUtils.equals(condition.getConditionType(), "title"))
                .toList();

        // 如果都没有 lexicon 类型的条件，则返回全部数据
        if (!hasLexiconCondition(contentConditions) && !hasLexiconCondition(titleConditions)) {
            filterWarningTaskList.addAll(warningTaskResultModels);
            return filterWarningTaskList;
        }
        List<ResourceDescDto> resourceGroupList = this.getResourceGroup();
        log.info("获取资源组:{}", resourceGroupList.size());
        // 处理 content 条件
        if (CollectionUtil.isNotEmpty(contentConditions)) {
            log.info("开始处理原文匹配规则");
            processLexiconConditions(contentConditions, warningTaskResultModels, resourceGroupList, filterWarningTaskList, "content");
            log.info("原文规则执行完匹配到数据条数:{}", filterWarningTaskList.size());
        }
        if (CollectionUtil.isNotEmpty(contentConditions) && CollectionUtil.isEmpty(filterWarningTaskList)) {
            log.info("原文过滤完没有满足的直接返回:{}", filterWarningTaskList.size());
            return filterWarningTaskList;
        }
        // 处理 title 条件
        if (CollectionUtil.isNotEmpty(titleConditions)) {
            log.info("开始处理标题匹配规则");
            processLexiconConditions(titleConditions, warningTaskResultModels, resourceGroupList, filterWarningTaskList, "title");
            log.info("标题规则执行完匹配到数据条数:{}", filterWarningTaskList.size());
        }
        log.info("结束处理原文和标题匹配规则满足条数:{}", filterWarningTaskList.size());
        // 去重处理
        return filterWarningTaskList.stream().distinct().collect(Collectors.toList());
    }

    private boolean hasLexiconCondition(List<InsClosedRuleConditionModel> conditions) {
        return conditions.stream()
                .anyMatch(condition -> "lexicon".equals(condition.getOption()));
    }

    private void processLexiconConditions(
            List<InsClosedRuleConditionModel> conditions,
            List<WarningTaskResultModel> warningTaskResultModels,
            List<ResourceDescDto> resourceGroupList,
            List<WarningTaskResultModel> resultContainer,
            String conditionType) {

        InsClosedRuleConditionModel conditionModel = conditions.get(0);
        String option = conditionModel.getOption();
        String value = conditionModel.getValue();
        List<InsAccountLexiconVo> allAccountLexiconList = getAllAccountLexiconList();
        if ("lexicon".equals(option)) {
            Map<String, Object> lexiconMap = JSON.parseObject(value, new TypeReference<>() {
            });

            // 处理 key="1" 的情况
            if (MapUtils.isNotEmpty(lexiconMap) && lexiconMap.containsKey("1") && lexiconMap.containsKey("type")) {
                String type = (String) lexiconMap.get("type");
                List<String> idList = extractStringList(lexiconMap.get("1"));
                if (StringUtils.isNotEmpty(type) && type.equals("rule")) {
                    List<ResourceDescDto> descDtoList = resourceGroupList.stream()
                            .filter(item -> idList.contains(item.getResourceId()))
                            .toList();
                    log.info("开始处理规则词库lexicon=1的匹配规则:{},type:{}", descDtoList.size(), type);
                    filterAndAddResults(warningTaskResultModels, descDtoList, resultContainer, conditionType);
                } else if (StringUtils.isNotEmpty(type) && type.equals("account")) {
                    List<InsAccountLexiconVo> descDtoList = allAccountLexiconList.stream()
                            .filter(item -> idList.contains(item.getResourceId()))
                            .toList();
                    log.info("开始处理l账户词库lexicon=1的匹配规则:{}", descDtoList.size());
                    filterAccountResults(warningTaskResultModels, descDtoList, resultContainer, conditionType);
                }
            }

            // 处理 key="2" 的情况
            if (MapUtils.isNotEmpty(lexiconMap) && lexiconMap.containsKey("2") && lexiconMap.containsKey("type")) {
                String type = (String) lexiconMap.get("type");
                List<String> idList = extractStringList(lexiconMap.get("2"));
                if (StringUtils.isNotEmpty(type) && type.equals("rule")) {
                    List<ResourceDescDto> descDtoList = resourceGroupList.stream()
                            .filter(item -> idList.contains(item.getId()))
                            .toList();
                    log.info("开始处理规则词库lexicon=2的匹配规则:{}", descDtoList.size());
                    filterAndAddResults(warningTaskResultModels, descDtoList, resultContainer, conditionType);
                } else if (StringUtils.isNotEmpty(type) && type.equals("account")) {
                    List<InsAccountLexiconVo> descDtoList = allAccountLexiconList.stream()
                            .filter(item -> idList.contains(item.getId()))
                            .toList();
                    log.info("开始处理账户词库lexicon=2的匹配规则:{}", descDtoList.size());
                    filterAccountResults(warningTaskResultModels, descDtoList, resultContainer, conditionType);
                }
            }
        }
    }


    private void filterAccountResults(
            List<WarningTaskResultModel> sourceList,
            List<InsAccountLexiconVo> descDtoList,
            List<WarningTaskResultModel> targetList, String conditionType) {

        for (WarningTaskResultModel taskResultModel : sourceList) {
            boolean containsAny = Boolean.FALSE;
            if (conditionType.equals("content")) {
                containsAny = descDtoList.stream()
                        .anyMatch(item -> StrUtil.contains(taskResultModel.getContent(), "@" + item.getAccountName()));
            } else if (conditionType.equals("title")) {
                containsAny = descDtoList.stream()
                        .anyMatch(item -> StrUtil.contains(taskResultModel.getTitle(), "@" + item.getAccountName()));
            }
            if (containsAny) {
                targetList.add(taskResultModel);
            }
        }
        log.info("处理账户词库{}规则满足条数:{}", conditionType, targetList.size());
    }

    private void filterAndAddResults(
            List<WarningTaskResultModel> sourceList,
            List<ResourceDescDto> descDtoList,
            List<WarningTaskResultModel> targetList, String conditionType) {

        for (WarningTaskResultModel taskResultModel : sourceList) {
            boolean containsAny = Boolean.FALSE;
            if (conditionType.equals("content")) {
                containsAny = descDtoList.stream()
                        .anyMatch(item -> StrUtil.contains(taskResultModel.getContent(), item.getName()));
            } else if (conditionType.equals("title")) {
                containsAny = descDtoList.stream()
                        .anyMatch(item -> StrUtil.contains(taskResultModel.getTitle(), item.getName()));
            }
            if (containsAny) {
                targetList.add(taskResultModel);
            }
        }
        log.info("处理规则词库{}规则满足条数:{}", conditionType, targetList.size());
    }


    private void parseConditions(List<InsClosedRuleConditionModel> conditions,
                                 WarningTaskConditionsModel model) {

        List<InsAccountLexiconVo> allAccountLexiconList = getAllAccountLexiconList();
        for (InsClosedRuleConditionModel condition : conditions) {
            String conditionType = condition.getConditionType();
            String conditionValue = condition.getValue();
            String option = condition.getOption();

            switch (conditionType) {
                case "carSeries":
                    log.info("carSeries: {}", conditionValue);
                    handleCarSeriesCondition(conditionValue, model);
                    break;
                case "intention":
                    log.info("intention: {}", conditionValue);
                    model.setIntentionType(conditionValue);
                    break;
                case "AD_type":
                    log.info("AD_type: {}", conditionValue);
                    if (ObjectUtils.isNotEmpty(conditionValue)) {
                        List<String> adTypeList = JSON.parseArray(conditionValue, String.class);
                        model.setAdTypeList(adTypeList);
                    }
                    break;
                case "content":
                    log.info("content: {}", conditionValue);
                    handleContentCondition(option, conditionValue, model);
                    break;
                case "title":
                    log.info("title: {}", conditionValue);
                    handleTitleCondition(option, conditionValue, model);
                    break;
                case "regulation_content_type":
                    log.info("regulation_content_type: {}", conditionValue);
                    handleRegulationContentType(conditionValue, model);
                    break;
                case "publish_user":
                    log.info("publish_user: {}", conditionValue);
                    handlePublishUserCondition(conditionValue, model, allAccountLexiconList);
                    break;
                case "original_post_user":
                    log.info("original_post_user: {}", conditionValue);
                    handleOriginalPostUserCondition(conditionValue, model, allAccountLexiconList);
                    break;
                case "experience_code":
                    log.info("experience_code: {}", conditionValue);
                    handleExperienceCodeCondition(conditionValue, model);
                    break;
                case "standpoint":
                    log.info("standpoint: {}", conditionValue);
                    model.setTopicCode(conditionValue);
                    break;
                case "emotion":
                    log.info("emotion: {}", conditionValue);
                    model.setSentiment(conditionValue);
                    break;
                default:
                    log.warn("未知的条件类型: {}", conditionType);
                    break;
            }
        }
    }

    private void handleCarSeriesCondition(String conditionValue, WarningTaskConditionsModel model) {
        if (StringUtils.isNotBlank(conditionValue)) {
            model.setCarSeriesCode(JSON.parseArray(conditionValue, String.class));
        }
    }

    private void handleContentCondition(String option, String conditionValue, WarningTaskConditionsModel model) {
        if ("value".equals(option) && StringUtils.isNotBlank(conditionValue)) {
            model.setContent(conditionValue);
        }
    }

    private void handleTitleCondition(String option, String conditionValue, WarningTaskConditionsModel model) {
        if ("value".equals(option) && StringUtils.isNotBlank(conditionValue)) {
            model.setTitle(conditionValue);
        }
    }

    private void handleRegulationContentType(String conditionValue, WarningTaskConditionsModel model) {
        Map<String, String> stringMap = JSON.parseObject(conditionValue, new TypeReference<>() {
        });
        if (stringMap.containsKey("1")) {
            model.setContentType(stringMap.get("1"));
        }
        if (stringMap.containsKey("2")) {
            model.setContentType(ContentTypeEnum.PERSONAGE.getCode());
            if ("comment".equals(stringMap.get("2"))) {
                model.setContentTypeMin("N");
            } else {
                model.setContentTypeMin("Y");
            }
        }
    }

    private void handlePublishUserCondition(String conditionValue,
                                            WarningTaskConditionsModel model,
                                            List<InsAccountLexiconVo> allAccountLexiconList) {
        Map<String, Object> publishUserMap = JSON.parseObject(conditionValue, new TypeReference<>() {
        });
        if (MapUtils.isNotEmpty(publishUserMap)) {
            if (publishUserMap.containsKey("1")) {
                Object obj1 = publishUserMap.get("1");
                List<String> idList = extractStringList(obj1);
                List<WarningUserModel> userList = buildWarningUserList(
                        idList,
                        allAccountLexiconList,
                        InsAccountLexiconVo::getResourceId
                );
                model.setMainPostUser(userList);
            }
            if (publishUserMap.containsKey("2")) {
                Object obj1 = publishUserMap.get("2");
                List<String> idList = extractStringList(obj1);
                List<WarningUserModel> userList = buildWarningUserList(
                        idList,
                        allAccountLexiconList,
                        InsAccountLexiconVo::getId
                );
                model.setMainPostUser(userList);
            }
        }
    }

    private void handleOriginalPostUserCondition(String conditionValue,
                                                 WarningTaskConditionsModel model,
                                                 List<InsAccountLexiconVo> allAccountLexiconList) {
        Map<String, Object> originalPostUserMap = JSON.parseObject(conditionValue, new TypeReference<>() {
        });
        if (MapUtils.isNotEmpty(originalPostUserMap)) {
            if (originalPostUserMap.containsKey("1")) {
                Object obj1 = originalPostUserMap.get("1");
                List<String> idList = extractStringList(obj1);
                List<WarningUserModel> userList = buildWarningUserList(
                        idList,
                        allAccountLexiconList,
                        InsAccountLexiconVo::getResourceId
                );
                model.setPostUser(userList);
            }
            if (originalPostUserMap.containsKey("2")) {
                Object obj1 = originalPostUserMap.get("2");
                List<String> idList = extractStringList(obj1);
                List<WarningUserModel> userList = buildWarningUserList(
                        idList,
                        allAccountLexiconList,
                        InsAccountLexiconVo::getId
                );
                model.setPostUser(userList);
            }
        }
    }


    private List<String> extractStringList(Object obj) {
        List<String> idList = new ArrayList<>();
        if (obj instanceof List<?>) {
            idList = ((List<?>) obj).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return idList;
    }


    /**
     * 构建WarningUserModel列表的通用方法
     */
    private List<WarningUserModel> buildWarningUserList(
            List<String> idList,
            List<InsAccountLexiconVo> accountLexiconList,
            java.util.function.Function<InsAccountLexiconVo, String> idExtractor) {

        return accountLexiconList.stream()
                .filter(account -> idList.contains(idExtractor.apply(account)))
                .map(account -> WarningUserModel.builder()
                        .accountId(account.getAccountId())
                        .accountName(account.getAccountName())
                        .channel(account.getFinalChannel())
                        .build())
                .toList();
    }

    private void handleExperienceCodeCondition(String conditionValue, WarningTaskConditionsModel model) {
        Map<String, String> codeMap = JSON.parseObject(conditionValue, new TypeReference<>() {
        });
        if (MapUtils.isNotEmpty(codeMap)) {
            for (Map.Entry<String, String> entry : codeMap.entrySet()) {
                model.setLevel(entry.getKey());
                model.setTagCode(entry.getValue());
            }
        } else {
            model.setTagCode("");
        }
    }


    public List<InsAccountLexiconVo> getAllAccountLexiconList() {
        List<InsAccountLexiconVo> allAccountLexiconList = iInsAccountLexiconService.findAllAccountLexiconList();
        if (CollectionUtils.isNotEmpty(allAccountLexiconList)) {
            return allAccountLexiconList;
        }
        return Collections.emptyList();
    }


    /**
     * 获取数据源分组
     */
    public List<ResourceDescDto> getResourceGroup() {
        String cacheKey = "risk:resource:group:set";

        // 尝试从缓存获取
        try {
            Set<String> cachedDataSet = stringRedisTemplate.opsForSet().members(cacheKey);
            if (CollectionUtil.isNotEmpty(cachedDataSet)) {
                return cachedDataSet.stream()
                        .map(data -> JSON.parseObject(data, ResourceDescDto.class))
                        .collect(Collectors.toList());
            }
            log.info("从缓存中获取数据源分组成功:{}", cachedDataSet.size());
        } catch (Exception e) {
            log.warn("解析缓存数据失败，将从数据库重新加载", e);
        }

        // 缓存未命中或解析失败，从数据库查询
        List<ResourceDescDto> resourceDescList = insDataResourceDescService.queryByParam(
                InsDataResourceDescModel.builder().build());

        if (CollectionUtil.isEmpty(resourceDescList)) {
            resourceDescList = new ArrayList<>();
        }
        log.info("从数据库中获取数据源分组成功:{}", resourceDescList.size());

        // 将结果存入缓存为Set类型，设置过期时间（例如30分钟）
        try {
            Set<String> dataSet = resourceDescList.stream()
                    .map(dto -> JSON.toJSONString(dto))
                    .collect(Collectors.toSet());

            stringRedisTemplate.opsForSet().add(cacheKey, dataSet.toArray(new String[0]));
            stringRedisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存资源分组数据失败", e);
        }

        return resourceDescList;
    }


    /**
     * 组装数据
     */
    private void assembleRuleTestData(List<WarningTaskResultModel> warningTaskResultModels, InsClosedRuleModel insClosedRuleModel) {
        for (WarningTaskResultModel warningTaskResultModel : warningTaskResultModels) {
            warningTaskResultModel.setRuleId(insClosedRuleModel.getRuleId());
            warningTaskResultModel.setRuleName(insClosedRuleModel.getRuleName());
        }

    }


    private List<ReportModelTagsResultDataRiskEntity> assembleDistinctData(List<CarDataEntity> filteredDataList) {
        List<ReportModelTagsResultDataRiskEntity> entityList = new ArrayList<>();
        InsClosedRuleModel insClosedRuleModel = iInsClosedRuleService.queryRuleDetail(ruleConfigId);
        List<ChannelInfoVo> infoVoList = iInsChannelInfoService.findAll(InsChannelInfoModel.builder().clientId(clientId).build());
        Map<String, ChannelInfoVo> channelByCodeMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getCode,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));

        Map<String, ChannelInfoVo> channelIdMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getId,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));
        InsDataResourceModel insDataResourceModel = insDataResourceService.queryById(InsDataResourceModel.builder().id(insClosedRuleModel.getCategoryType()).build());
        for (CarDataEntity carData : filteredDataList) {
            ReportModelTagsResultDataRiskEntity entity = new ReportModelTagsResultDataRiskEntity();

            if (ObjectUtils.isNotEmpty(carData.getCarBrand())) {
                entity.setBrandName(ChannelMappingConstant.BRAND_MAPPING.get(carData.getCarBrand().trim()));
            }
            if (ObjectUtils.isNotEmpty(entity.getBrandName())) {
                entity.setBrandCode(ChannelMappingConstant.CHANGAN_BRANDS.get(entity.getBrandName().trim()));
            } else {
                entity.setBrandName("长安引力");
                entity.setBrandCode("A01");
                log.info("舆情公关数据品牌没有映射上赋值默认:{}", carData.getHashCode());
            }
            entity.setId(IdWorker.getId());
            entity.setSoundsId(carData.getHashCode());
            entity.setCarSeriesName(carData.getSeries());
            entity.setCarSeriesCode(carData.getSeries());
            entity.setClosedRuleId(insClosedRuleModel.getRuleId());
            entity.setAppNameFinal(carData.getAppNameFinal());
            entity.setDataId(carData.getHashCode());
            Map<String, String> platformMapping = ChannelMappingConstant.getPlatformMapping(carData.getAppNameFinal());
            entity.setChannelCode(platformMapping.get("channelCode"));
            entity.setChannelName(platformMapping.get("channelDesc"));
            if (channelByCodeMap.containsKey(entity.getChannelCode())) {
                ChannelInfoVo channelInfoVo = channelIdMap.get(channelByCodeMap.get(entity.getChannelCode()).getId());
                entity.setSecondChannelCode(channelInfoVo.getCode());
                entity.setSecondChannelName(channelInfoVo.getName());
            } else {
                entity.setSecondChannelCode(platformMapping.get("channelCode"));
                entity.setSecondChannelName(platformMapping.get("channelDesc"));
            }
            entity.setWarningTime(LocalDateTime.now());
            entity.setWarningEventNo(dailyResetCodeGenerator.generateCode());
            entity.setOneId("/");
            entity.setPublishTime(carData.getNewsPosttime());
            entity.setEventName(insClosedRuleModel.getRuleName());
            entity.setSubjectCategoryId(insDataResourceModel.getId());
            entity.setSubjectCategoryName(insDataResourceModel.getName());
            entity.setEventPriority(insClosedRuleModel.getProcessPriority());
            entity.setEventPriorityName(insClosedRuleModel.getProcessPriority());
            entity.setEventLevel(insClosedRuleModel.getEventLevel());
            entity.setEventLevelName(insClosedRuleModel.getEventLevel());
            entity.setConfirmationMethod(insClosedRuleModel.getAuditMethod());
            entity.setReviewMethod(insClosedRuleModel.getConfirmMethod());
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getAuditDepartment())) {
                entity.setReviewOrgId(insClosedRuleModel.getAuditDepartment().getId());
                entity.setReviewOrgNo(insClosedRuleModel.getAuditDepartment().getDeptNo());
                entity.setReviewOrgName(insClosedRuleModel.getAuditDepartment().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getAuditor())) {
                entity.setReviewUserId(insClosedRuleModel.getAuditor().getId());
                entity.setReviewUserEmpNo(insClosedRuleModel.getAuditor().getEmployeeId());
                entity.setReviewUserName(insClosedRuleModel.getAuditor().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getConfirmDepartment())) {
                entity.setConfirmOrgId(insClosedRuleModel.getConfirmDepartment().getId());
                entity.setConfirmOrgNo(insClosedRuleModel.getConfirmDepartment().getDeptNo());
                entity.setConfirmOrgName(insClosedRuleModel.getConfirmDepartment().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getConfirmer())) {
                entity.setConfirmUserId(insClosedRuleModel.getConfirmer().getId());
                entity.setConfirmUserEmpNo(insClosedRuleModel.getConfirmer().getEmployeeId());
                entity.setConfirmUserName(insClosedRuleModel.getConfirmer().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getMainDepartment())) {
                entity.setMainRespOrgId(insClosedRuleModel.getMainDepartment().getId());
                entity.setMainRespOrgNo(insClosedRuleModel.getMainDepartment().getDeptNo());
                entity.setMainRespOrgName(insClosedRuleModel.getMainDepartment().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getMainResponder())) {
                entity.setMainRespUserId(insClosedRuleModel.getMainResponder().getId());
                entity.setMainRespUserEmpNo(insClosedRuleModel.getMainResponder().getEmployeeId());
                entity.setMainRespUserName(insClosedRuleModel.getMainResponder().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getCcPersonnel())) {
                entity.setCcUsers(JSON.toJSONString(insClosedRuleModel.getCcPersonnel()));
            }
            entity.setContent(carData.getContent());
            entity.setContentType(ContentTypeEnum.PERSONAGE.getCode());
            entity.setIsMainPost("Y");
            entity.setSentiment(carData.getFeelTag());
            entity.setMainPostTitle(carData.getNewsTitle());
            entity.setMainPostDetails(carData.getContent());
            entity.setPostTime(carData.getNewsPosttime());
            entity.setPostUserName(carData.getNewsAuthorFinal());
            entity.setMainPostUrl(carData.getNewsUrl());
            entity.setUpdateTime(LocalDateTime.now());
            entity.setCreateTime(LocalDateTime.now());
            entity.setTaskStatus("0");
            entityList.add(entity);
        }
        return entityList;
    }


    /**
     * 组装数据
     */
    private List<ReportModelTagsResultDataRiskEntity> assembleData(List<WarningTaskResultModel> warningTaskResultModels, InsClosedRuleModel insClosedRuleModel) {
        List<ReportModelTagsResultDataRiskEntity> entityList = new ArrayList<>();

        List<ChannelInfoVo> infoVoList = iInsChannelInfoService.findAll(InsChannelInfoModel.builder().clientId(clientId).build());
        Map<String, ChannelInfoVo> channelByCodeMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getCode,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));

        Map<String, ChannelInfoVo> channelIdMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getId,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));
        InsDataResourceModel insDataResourceModel = insDataResourceService.queryById(InsDataResourceModel.builder().id(insClosedRuleModel.getCategoryType()).build());
        for (WarningTaskResultModel warningTaskResultModel : warningTaskResultModels) {
            ReportModelTagsResultDataRiskEntity entity = new ReportModelTagsResultDataRiskEntity();

            entity.setBrandCode(insClosedRuleModel.getBrandCode());
            entity.setBrandName(insClosedRuleModel.getBrandName());
            entity.setId(IdWorker.getId());
            entity.setSoundsId(warningTaskResultModel.getId());
            entity.setCarSeriesCode(warningTaskResultModel.getCarSeriesCode());
            entity.setCarSeriesName(warningTaskResultModel.getCarSeriesName());
            entity.setClosedRuleId(insClosedRuleModel.getRuleId());
            entity.setDataId(warningTaskResultModel.getDataId());
            entity.setChannelCode(warningTaskResultModel.getChannelCode());
            entity.setChannelName(warningTaskResultModel.getChannelName());
            if (channelByCodeMap.containsKey(warningTaskResultModel.getChannelCode())) {
                ChannelInfoVo channelInfoVo = channelIdMap.get(channelByCodeMap.get(warningTaskResultModel.getChannelCode()).getId());
                entity.setSecondChannelCode(channelInfoVo.getCode());
                entity.setSecondChannelName(channelInfoVo.getName());
            } else {
                entity.setSecondChannelCode(warningTaskResultModel.getChannelCode());
                entity.setSecondChannelName(warningTaskResultModel.getChannelName());
            }
            entity.setWarningTime(LocalDateTime.now());
            entity.setWarningEventNo(dailyResetCodeGenerator.generateCode());
            entity.setOneId(warningTaskResultModel.getOneIdRisk());
            entity.setPublishTime(warningTaskResultModel.getPublishTime());
            entity.setEventName(insClosedRuleModel.getRuleName());
            entity.setSubjectCategoryId(insDataResourceModel.getId());
            entity.setSubjectCategoryName(insDataResourceModel.getName());
            entity.setEventPriority(insClosedRuleModel.getProcessPriority());
            entity.setEventPriorityName(insClosedRuleModel.getProcessPriority());
            entity.setEventLevel(insClosedRuleModel.getEventLevel());
            entity.setEventLevelName(insClosedRuleModel.getEventLevel());
            entity.setConfirmationMethod(insClosedRuleModel.getAuditMethod());
            entity.setReviewMethod(insClosedRuleModel.getConfirmMethod());
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getAuditDepartment())) {
                entity.setReviewOrgId(insClosedRuleModel.getAuditDepartment().getId());
                entity.setReviewOrgNo(insClosedRuleModel.getAuditDepartment().getDeptNo());
                entity.setReviewOrgName(insClosedRuleModel.getAuditDepartment().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getAuditor())) {
                entity.setReviewUserId(insClosedRuleModel.getAuditor().getId());
                entity.setReviewUserEmpNo(insClosedRuleModel.getAuditor().getEmployeeId());
                entity.setReviewUserName(insClosedRuleModel.getAuditor().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getConfirmDepartment())) {
                entity.setConfirmOrgId(insClosedRuleModel.getConfirmDepartment().getId());
                entity.setConfirmOrgNo(insClosedRuleModel.getConfirmDepartment().getDeptNo());
                entity.setConfirmOrgName(insClosedRuleModel.getConfirmDepartment().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getConfirmer())) {
                entity.setConfirmUserId(insClosedRuleModel.getConfirmer().getId());
                entity.setConfirmUserEmpNo(insClosedRuleModel.getConfirmer().getEmployeeId());
                entity.setConfirmUserName(insClosedRuleModel.getConfirmer().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getMainDepartment())) {
                entity.setMainRespOrgId(insClosedRuleModel.getMainDepartment().getId());
                entity.setMainRespOrgNo(insClosedRuleModel.getMainDepartment().getDeptNo());
                entity.setMainRespOrgName(insClosedRuleModel.getMainDepartment().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getMainResponder())) {
                entity.setMainRespUserId(insClosedRuleModel.getMainResponder().getId());
                entity.setMainRespUserEmpNo(insClosedRuleModel.getMainResponder().getEmployeeId());
                entity.setMainRespUserName(insClosedRuleModel.getMainResponder().getName());
            }
            if (ObjectUtils.isNotEmpty(insClosedRuleModel.getCcPersonnel())) {
                entity.setCcUsers(JSON.toJSONString(insClosedRuleModel.getCcPersonnel()));
            }
            entity.setCarModel(warningTaskResultModel.getModelName());
            entity.setVinNo(warningTaskResultModel.getVhlVin());
            entity.setDealerName(warningTaskResultModel.getDlrOcName());
            entity.setDealerCode(warningTaskResultModel.getDlrOcCode());
            entity.setContent(warningTaskResultModel.getContent());
            entity.setContentType(warningTaskResultModel.getContentType());
            entity.setOriginalTextScene(warningTaskResultModel.getOriginalTextScene());
            entity.setSentiment(warningTaskResultModel.getSentiment());
            entity.setIntentionType(warningTaskResultModel.getIntention());
            entity.setTopic(warningTaskResultModel.getTopicText());
            entity.setDomTagFirstCode(warningTaskResultModel.getDomTagFirstCode());
            entity.setDomTagSecondCode(warningTaskResultModel.getDomTagSecondCode());
            entity.setDomTagThreeCode(warningTaskResultModel.getDomTagThreeCode());
            entity.setDomTagFourCode(warningTaskResultModel.getDomTagFourCode());
            entity.setDomTagFirst(warningTaskResultModel.getDomTagFirst());
            entity.setDomTagSecond(warningTaskResultModel.getDomTagSecond());
            entity.setDomTagThree(warningTaskResultModel.getDomTagThree());
            entity.setDomTagFour(warningTaskResultModel.getDomTagFour());
            entity.setIsMainPost(warningTaskResultModel.getIsMainPost());
            if (ObjectUtils.isNotEmpty(warningTaskResultModel.getIsMainPost()) && warningTaskResultModel.getIsMainPost().equals("N")) {
                entity.setMainPostTitle(warningTaskResultModel.getRetweetedTitle());
                entity.setMainPostDetails(warningTaskResultModel.getRetweetedContent());
                entity.setPostUserId(warningTaskResultModel.getRetweetedUserId());
                entity.setPostUserName(warningTaskResultModel.getRetweetedUserName());
                entity.setPostTime(convert(warningTaskResultModel.getRetweetedTime()));
                entity.setMainPostUrl(warningTaskResultModel.getRetweetedUrl());
                entity.setCommentUserId(warningTaskResultModel.getCommentUserId());
                entity.setCommentUserName(warningTaskResultModel.getCommentUserName());
                entity.setCommentTime(warningTaskResultModel.getPublishTime());
                entity.setCommentDetails(warningTaskResultModel.getContent());
            } else {
                entity.setMainPostTitle(warningTaskResultModel.getTitle());
                entity.setMainPostDetails(warningTaskResultModel.getContent());
                entity.setPostUserId(warningTaskResultModel.getOneIdRisk());
                entity.setPostUserName(warningTaskResultModel.getCustName());
                entity.setPostTime(warningTaskResultModel.getPublishTime());
                entity.setMainPostUrl(warningTaskResultModel.getOriginalLink());
            }
            entity.setSensitiveType(warningTaskResultModel.getSensitiveType());
            if (ObjectUtils.isNotEmpty(warningTaskResultModel.getTagEventClarity()) && !warningTaskResultModel.getTagEventClarity().equals("null")) {
                entity.setEventClarity(warningTaskResultModel.getTagEventClarity());
            }
            entity.setIsNeedReply(warningTaskResultModel.getTagComplaintFlagNeedingReply());
            entity.setIsNeedClosedLoop(warningTaskResultModel.getTagNeedForvclosedLoop());
            entity.setUpdateTime(LocalDateTime.now());
            entity.setCreateTime(LocalDateTime.now());
            entity.setTaskStatus("0");

            entityList.add(entity);
        }
        return entityList;
    }


    /**
     * 重载方法：支持自定义时区
     *
     * @param secondTimestamp 秒级时间戳
     * @return LocalDateTime 对象
     */
    public static LocalDateTime convert(String secondTimestamp) {
        if (StringUtils.isEmpty(secondTimestamp)) {
            return null;
        }
        try {
            // 1. 秒级时间戳 → Instant（UTC 时间点）
            Instant instant = Instant.ofEpochSecond(Long.parseLong(secondTimestamp));
            // 2. 绑定时区 → ZonedDateTime → 提取 LocalDateTime（不含时区信息）
            return instant.atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        } catch (Exception e) {
            System.err.println("时间戳转 LocalDateTime 失败，timestamp：" + secondTimestamp + "，异常：" + e.getMessage());
            return null;
        }
    }

}
