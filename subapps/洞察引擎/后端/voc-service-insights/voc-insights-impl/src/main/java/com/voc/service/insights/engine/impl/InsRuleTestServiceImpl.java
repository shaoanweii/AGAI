package com.voc.service.insights.engine.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.UploadModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.minio.service.UploadFileService;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.*;
import com.voc.service.insights.engine.api.constants.ContentTypeEnum;
import com.voc.service.insights.engine.api.model.InsRuleTestListModel;
import com.voc.service.insights.engine.api.model.WarningTaskRunModel;
import com.voc.service.insights.engine.common.util.ExcelUtil;
import com.voc.service.insights.engine.dao.InsChannelInfoDao;
import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataEntity;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataInfoEntity;
import com.voc.service.insights.engine.entity.InsReportRuleTestDataResultEntity;
import com.voc.service.insights.engine.enums.ChannelType;
import com.voc.service.insights.engine.listener.RuleTestListener;
import com.voc.service.insights.engine.mapper.InsReportRuleTestDataMapper;
import com.voc.service.insights.engine.model.InsAddRuleTestModel;
import com.voc.service.insights.engine.model.InsCarSeriesInfoModel;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
public class InsRuleTestServiceImpl extends ServiceImpl<InsReportRuleTestDataMapper, InsReportRuleTestDataEntity>
        implements IInsRuleTestService {
    private static final Logger log = LoggerFactory.getLogger(InsRuleTestServiceImpl.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    InsChannelInfoDao insChannelInfoDao;

    @Autowired
    UploadFileService uploadFileService;

    @Autowired
    IInsChannelInfoService iInsChannelInfoService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    InsRuleTestDataInfoServiceImpl insRuleTestDataInfoService;

    @Autowired
    ICqCaRiskDataAnalysisService iCqCaRiskDataAnalysisService;

    @Autowired
    IInsCarSeriesInfoService iInsCarSeriesInfoService;

    @Autowired
    IInsTagLibClientService iInsTagLibClientService;

    // 定义一个静态的汽车品牌集合
    public static final Map<String, String> CHANGAN_BRANDS = Map.of(
            "A01", "长安引力",
            "A02", "长安凯程",
            "A03", "深蓝汽车",
            "A04", "阿维塔",
            "A05", "长安启源"
    );

    // 在类中添加静态反向映射
    private static final Map<String, String> BRAND_NAMES_TO_CODES = CHANGAN_BRANDS.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));


    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public PageInfo<RuleTestListVo> ruleTestList(InsRuleTestListModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsReportRuleTestDataEntity> entityList = this.baseMapper.selectPageList(model);
        PageInfo page = new PageInfo<>(entityList);
        if (CollectionUtil.isEmpty(entityList)) {
            return page;
        }
        List<RuleTestListVo> pageVoList = this.convertPageVo(entityList);
        page.setList(pageVoList);
        return page;
    }

    private List<RuleTestListVo> convertPageVo(List<InsReportRuleTestDataEntity> entityList) {
        List<RuleTestListVo> pageVoList = new ArrayList<>();
        for (InsReportRuleTestDataEntity entity : entityList) {
            RuleTestListVo pageVo = RuleTestListVo.builder()
                    .id(entity.getId())
                    .ruleTestInfo(entity.getRuleTestInfo())
                    .ruleType(entity.getRuleType())
                    .ruleTypeText("闭环规则")
                    .createUser(entity.getCreateUser())
                    .ruleCount(entity.getRuleCount())
                    .sampleCount(entity.getSampleCount())
                    .createTime(ObjectUtils.isNotEmpty(entity.getCreateTime()) ? entity.getCreateTime().format(FORMATTER) : "")
                    .finishTime(ObjectUtils.isNotEmpty(entity.getFinishTime()) ? entity.getFinishTime().format(FORMATTER) : "")
                    .testStatus(entity.getTestStatus())
                    .build();
            pageVo.setTestStatusStr(entity.getTestStatusText());
            pageVoList.add(pageVo);
        }
        return pageVoList;
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public Boolean addRuleTestList(InsAddRuleTestModel model) {
        log.info("规则测试新增:{}", model);
        if (ObjectUtils.isEmpty(model.getRuleId())) {
            return false;
        }
        List<InsReportRuleTestDataInfoEntity> ruleTestCache = getRuleTestCache(model.getBatchId());
        log.info("规则测试缓存:{}", ruleTestCache);
        if (CollectionUtil.isEmpty(ruleTestCache)) {
            return false;
        }
        InsReportRuleTestDataEntity insReportRuleTestData = InsReportRuleTestDataEntity.builder()
                .id(ObjectUtils.isNotEmpty(model.getId()) ? model.getId() : IdWorker.getId())
                .ruleId(JSON.toJSONString(model.getRuleId()))
                .ruleTestInfo("闭环规则" + LocalDateTime.now().format(FORMATTER))
                .batchId(ruleTestCache.get(0).getBatchId())
                .ruleType(model.getRuleType())
                .createUser(ServiceContextHolder.getUser().getFirstname())
                .ruleCount(model.getRuleId().size() + "")
                .sampleCount(ruleTestCache.size() + "")
                .createTime(LocalDateTime.now())
                .fileName(model.getFileName())
                .fileBaseName(model.getFileBaseName())
                .testStatus("0")
                .build();
        int insert;
        if (ObjectUtils.isEmpty(model.getId())) {
            insert = this.baseMapper.insert(insReportRuleTestData);
        } else {
            insert = this.baseMapper.updateById(insReportRuleTestData);
        }
        if (insert > 0) {
            Boolean b = insRuleTestDataInfoService.batchRuleTest(ruleTestCache);
            log.info("规则新增结果:{}", b);
            return b;
        }
        return Boolean.TRUE;
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public PageInfo<InsRuleTestInfoVo> getRuleInfo(InsRuleTestListModel model) {
        InsReportRuleTestDataEntity reportRuleTestDataEntity = this.getById(model.getId());
        log.info("获取测试详情:{}", reportRuleTestDataEntity);
        if (ObjectUtils.isEmpty(reportRuleTestDataEntity)) {
            return null;
        }
        model.setBatchId(reportRuleTestDataEntity.getBatchId());
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsReportRuleTestDataResultEntity> entityList = insRuleTestDataInfoService.selectPageList(model);
        PageInfo page = new PageInfo<>(entityList);
        if (CollectionUtil.isEmpty(entityList)) {
            return page;
        }
        page.setList(entityList);
        return page;
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public Boolean copyRuleTest(InsRuleTestListModel model) {
        InsReportRuleTestDataEntity reportRuleTestDataEntity = this.getById(model.getId());
        reportRuleTestDataEntity.setId(IdWorker.getId());
        reportRuleTestDataEntity.setCreateTime(LocalDateTime.now());
        reportRuleTestDataEntity.setTestStatus("0");
        return this.save(reportRuleTestDataEntity);
    }

    @Override
    public Boolean startRuleTest(InsRuleTestListModel model) {
        WarningTaskRunModel warningTaskRunModel = new WarningTaskRunModel();
        InsReportRuleTestDataEntity reportRuleTestDataEntity = this.baseMapper.getRuleTestData(model.getId());
        log.info("规则测试开始:{}", reportRuleTestDataEntity);
        if (ObjectUtils.isEmpty(reportRuleTestDataEntity)) {
            return false;
        }
        warningTaskRunModel.setBatchId(reportRuleTestDataEntity.getBatchId());
        List<String> ruleIdList = JSON.parseArray(reportRuleTestDataEntity.getRuleId(), String.class);
        log.info("规则ID:{}", ruleIdList);
        for (String ruleId : ruleIdList) {
            warningTaskRunModel.setRuleId(ruleId);
            Boolean b = iCqCaRiskDataAnalysisService.warningTestTaskRun(warningTaskRunModel);
            log.info("规则测试结果返回:{}", b);
        }
        boolean b = this.baseMapper.updateTaskStatus(model.getId());
        if (!b) {
            return false;
        }
        return Boolean.TRUE;
    }


    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public Boolean delRuleTest(InsRuleTestListModel model) {
        log.info("规则测试删除:{}", model);
        return this.removeById(model.getId());
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public RuleTestListVo getInfoRuleId(InsRuleTestListModel model) {
        InsReportRuleTestDataEntity reportRuleTestDataEntity = this.getById(model.getId());
        final String url = uploadFileService.getObjectUrl(reportRuleTestDataEntity.getFileName(), 30);
        RuleTestListVo ruleTestListVo = RuleTestListVo.builder()
                .id(reportRuleTestDataEntity.getId())
                .ruleTestInfo(reportRuleTestDataEntity.getRuleTestInfo())
                .ruleType(reportRuleTestDataEntity.getRuleType())
                .ruleTypeText("闭环规则")
                .batchId(reportRuleTestDataEntity.getBatchId())
                .fileName(reportRuleTestDataEntity.getFileName())
                .fileBaseName(reportRuleTestDataEntity.getFileBaseName())
                .url(url)
                .ruleTestList(JSON.parseArray(reportRuleTestDataEntity.getRuleId(), String.class))
                .createUser(reportRuleTestDataEntity.getCreateUser())
                .ruleCount(reportRuleTestDataEntity.getRuleCount())
                .sampleCount(reportRuleTestDataEntity.getSampleCount())
                .build();
        return ruleTestListVo;
    }


    @SneakyThrows
    @Override
    public void downloadRuleTest(HttpServletResponse response, Set<ConditionVo> async) {
        List<InsRuleTestExcelVo> list = new ArrayList<>(); // 空数据，仅生成模板
        try {
            // ====================== 1. 准备所有下拉框的数据 ======================

            // 1.1 准备普通下拉框的数据
            Map<Integer, List<String>> normalDropdownMap = new HashMap<>();

            // 渠道 (列索引 0)
            InsChannelInfoModel channelModel = new InsChannelInfoModel();
            channelModel.setType(ChannelType.CHANNEL.getCode());
            channelModel.setClientId("764547797eb2e192763f5334028d49c9");
            List<InsChannelInfoEntity> allChannelCategory = insChannelInfoDao.findChannel(channelModel);
            if (!ObjectUtils.isEmpty(allChannelCategory)) {
                List<String> channelNames = allChannelCategory.stream()
                        .map(InsChannelInfoEntity::getName)
                        .collect(Collectors.toList());
                normalDropdownMap.put(0, channelNames);
            }

            // 品牌 (列索引 1)
            Collection<String> values = CHANGAN_BRANDS.values();
            normalDropdownMap.put(1, new ArrayList<>(values));

            // 车系 (列索引 2)
            List<InsCarSeriesInfoModel> allCarSeries = iInsCarSeriesInfoService.findAll();
            if (!ObjectUtils.isEmpty(allCarSeries)) {
                List<String> carSeriesNames = allCarSeries.stream()
                        .map(InsCarSeriesInfoModel::getName)
                        .collect(Collectors.toList());
                normalDropdownMap.put(2, carSeriesNames);
            }

            // 内容类型 (列索引 3)
            if (async != null) {
                Optional<ConditionVo> contentTypeOpt = async.stream().filter(e -> "contentType".equals(e.getKey())).findFirst();
                contentTypeOpt.ifPresent(vo -> {
                    List<String> contentTypes = vo.getDetails().stream()
                            .map(ConditionDetailsVo::getValue)
                            .collect(Collectors.toList());
                    normalDropdownMap.put(3, contentTypes);
                });
            }

            // 情感倾向 (列索引 8)
            if (async != null) {
                Optional<ConditionVo> sentimentOpt = async.stream().filter(e -> "vocSentiment".equals(e.getKey())).findFirst();
                sentimentOpt.ifPresent(vo -> {
                    List<String> sentiments = vo.getDetails().stream()
                            .map(ConditionDetailsVo::getValue)
                            .collect(Collectors.toList());
                    normalDropdownMap.put(6, sentiments);
                });
            }

            // 用户意图 (列索引 9)
            if (async != null) {
                Optional<ConditionVo> intentionOpt = async.stream().filter(e -> "vocIntention".equals(e.getKey())).findFirst();
                intentionOpt.ifPresent(vo -> {
                    List<String> intentions = vo.getDetails().stream()
                            .map(ConditionDetailsVo::getValue)
                            .collect(Collectors.toList());
                    normalDropdownMap.put(7, intentions);
                });
            }

            int[] cascadeColumnIndices = new int[0]; // 默认空数组

            List<TagLibCategoryVo> tagTree = iInsTagLibClientService.findTagLibClientTree(
                    InsTagLibClientModel.builder()
                            .appClient("764547797eb2e192763f5334028d49c9")
                            .level(4)
                            .tagType("CA")
                            .build()
            );

            List<TagLibClientTreeVo> finalTagLibClientVoList = iInsTagLibClientService.findAllFinalTagLibClientVoList(InsTagLibClientModel.builder().appClient("764547797eb2e192763f5334028d49c9").tagType("CA").build());

            if (!ObjectUtils.isEmpty(tagTree)) {
                Map<Integer, List<TagLibCategoryVo>> integerListMap = collectTagsByLevel(tagTree);
                if (!ObjectUtils.isEmpty(integerListMap) && integerListMap.containsKey(1)) {
                    normalDropdownMap.put(8, integerListMap.get(1).stream().map(TagLibCategoryVo::getTagName).toList());
                }
                if (!ObjectUtils.isEmpty(integerListMap) && integerListMap.containsKey(2)) {
                    normalDropdownMap.put(9, integerListMap.get(2).stream().map(TagLibCategoryVo::getTagName).toList());
                }
                if (!ObjectUtils.isEmpty(integerListMap) && integerListMap.containsKey(3)) {
                    normalDropdownMap.put(10, integerListMap.get(3).stream().map(TagLibCategoryVo::getTagName).toList());
                }
                if (!ObjectUtils.isEmpty(integerListMap) && integerListMap.containsKey(4)) {
                    normalDropdownMap.put(11, integerListMap.get(4).stream().map(TagLibCategoryVo::getTagName).toList());
                }

            }
            if (!ObjectUtils.isEmpty(finalTagLibClientVoList)) {
                normalDropdownMap.put(12, finalTagLibClientVoList.stream().map(TagLibClientTreeVo::getTagName).collect(Collectors.toList()));
            }
            // ====================== 3. 调用工具类生成并下载模板 ======================
            String fileName = "数据模板.xlsx";
            ExcelUtil.writeExcelRuleTestExcel(response, list, fileName, fileName.substring(0, fileName.lastIndexOf(".")), InsRuleTestExcelVo.class, normalDropdownMap, tagTree, cascadeColumnIndices);


        } catch (Exception e) {
            log.error("下载Excel模板失败", e);
            // 可以在这里添加对前端的错误响应
            response.getWriter().write("下载失败：" + e.getMessage());
        }
    }


    /**
     * 递归遍历树结构，按层级收集所有标签名称
     *
     * @param rootNodes 树的顶层节点列表（如 tagParentId="0" 的节点）
     * @return 按层级分类的标签名称：key=层级（1开始），value=该层级所有标签名称
     */
    public static Map<Integer, List<TagLibCategoryVo>> collectTagsByLevel(List<TagLibCategoryVo> rootNodes) {
        Map<Integer, List<TagLibCategoryVo>> levelTagsMap = new TreeMap<>(); // TreeMap保证层级有序
        if (CollectionUtils.isEmpty(rootNodes)) {
            return levelTagsMap;
        }

        // 从第1级开始递归遍历
        recursiveCollect(rootNodes, 1, levelTagsMap);
        return levelTagsMap;
    }

    /**
     * 递归收集标签的核心方法
     *
     * @param currentNodes 当前层级的节点列表
     * @param currentLevel 当前层级（1开始）
     * @param levelTagsMap 存储结果的Map
     */
    private static void recursiveCollect(List<TagLibCategoryVo> currentNodes, int currentLevel,
                                         Map<Integer, List<TagLibCategoryVo>> levelTagsMap) {
        if (CollectionUtils.isEmpty(currentNodes)) {
            return;
        }

        // 收集当前层级的所有标签名称
        List<TagLibCategoryVo> currentLevelTags = levelTagsMap.computeIfAbsent(currentLevel, k -> new ArrayList<>());
        for (TagLibCategoryVo node : currentNodes) {
            // 避免空指针：标签名称不为null才添加
            if (node.getTagName() != null) {
                currentLevelTags.add(node);
            }

            // 递归处理下一层级（子节点）
            recursiveCollect(node.getChild(), currentLevel + 1, levelTagsMap);
        }
    }


    @Override
    public UploadModel uploadRuleTest(MultipartFile file) throws IOException {
        Assert.isTrue(!file.isEmpty(), "文件未上传");

        //获取文件后缀
        final String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if (!Arrays.asList("xlsx", "xls").contains(suffix.toLowerCase())) {
            return null;
        }
        //重新拼接文件名称
        final String fileName_ = IdWorker.getId().concat(".").concat(suffix);
        UploadModel model = new UploadModel();
        model.setKey(fileName_);
        model.setName(file.getName());
        //开启文件输入流
        @Cleanup
        InputStream fileIs = file.getInputStream();
        //将文件上传至minio指定的桶中
        uploadFileService.putObject(this.getFileName(fileName_), fileIs);
        log.debug("文件上传地址：{}", this.getFileName(fileName_));
        return model;
    }


    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public List<String> queryCreateUserList() {
        List<InsReportRuleTestDataEntity> labelCorrectionList = this.baseMapper.selectPageList(new InsRuleTestListModel());
        if (CollectionUtil.isEmpty(labelCorrectionList)) {
            return null;
        }
        return labelCorrectionList.stream().map(InsReportRuleTestDataEntity::getCreateUser).distinct().collect(Collectors.toList());
    }


    @Override
    public Map<String, List<InsCategoryRuleVo>> ruleSelect() {
        List<InsCategoryRuleVo> categoryRuleList = this.baseMapper.getCategoryRuleList();
        Map<String, List<InsCategoryRuleVo>> categoryRuleMap = categoryRuleList.stream().collect(Collectors.groupingBy(InsCategoryRuleVo::getCategoryName));
        return categoryRuleMap;
    }

    private String getFileName(String name) {
        return ServiceContextHolder.getSystemId().concat("/").concat("file-temp").concat("/").concat(name);
    }

    @Override
    public InsRuleTestValidateVo checkUploadRuleTest(InsRuleTestListModel model) throws Exception {
        Assert.hasLength(model.getFileName(), "文件名称不允许为空");
        InsRuleTestValidateVo dataSourceValidateVo = new InsRuleTestValidateVo();
        String message = "";
        Map<String, Object> map = new ConcurrentHashMap<>();
        final String batchId = IdWorker.getId();
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        List<ChannelInfoVo> allChannelInfo = iInsChannelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId("764547797eb2e192763f5334028d49c9").build());
        log.debug("获取全部渠道:{}", allChannelInfo.size());
        try {
            log.debug("开始读取minio中的文件");
            @Cleanup
            InputStream objectInputStream = uploadFileService.getObjectInputStream(this.getFileName(model.getFileName()));
            long start = System.currentTimeMillis();
            log.debug("开始解析文件");
            EasyExcel.read(objectInputStream, InsRuleTestExcelVo.class, new RuleTestListener(this, map, batchId, fail, success, allChannelInfo)).sheet().doRead();
            long end = System.currentTimeMillis();
            log.info("读取耗时：{}", TimeUnit.MILLISECONDS.toSeconds(end - start) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end - start) + "秒" : (end - start) + "毫秒");
            long start1 = System.currentTimeMillis();
            Integer failTotal = ObjectUtils.isNotEmpty(map.get("fail")) ? Integer.valueOf(map.get("fail").toString()) : 0;
            Integer successTotal = ObjectUtils.isNotEmpty(map.get("success")) ? Integer.valueOf(map.get("success").toString()) : 0;
            List<InsReportRuleTestDataInfoEntity> insRuleTestExcelVos = ObjectUtils.isNotEmpty(map.get("dataSource")) ? (List<InsReportRuleTestDataInfoEntity>) map.get("dataSource") : List.of();
            Integer total = failTotal + successTotal;
            dataSourceValidateVo.setBatchId(batchId);
            dataSourceValidateVo.setSuccess(String.valueOf(successTotal));
            dataSourceValidateVo.setTotal(String.valueOf(total));
            if (successTotal.equals(total)) {
                message = "校验完成，共：" + total + "条，有效数据：" + successTotal + "条";
            } else if (successTotal < total && successTotal > 0) {
                message = "校验完成，共：" + total + "条，有效数据：" + successTotal + "条，系统仅导入有效数据";
            } else if (successTotal == 0) {
                message = "校验完成，共：" + total + "条，有效数据：" + success + "条,请重新上传";
            }
            dataSourceValidateVo.setMessage(message);
            long end1 = System.currentTimeMillis();
            log.info("数据组装耗时：{}", TimeUnit.MILLISECONDS.toSeconds(end1 - start1) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end1 - start1) + "秒" : (end1 - start1) + "毫秒");
            setRuleTestCache(batchId, insRuleTestExcelVos);
        } catch (Exception e) {
            log.error("{}", e);
            throw new Exception("校验失败，请重新校验");
        }
        return dataSourceValidateVo;
    }

    public void setRuleTestCache(String batchId, List<InsReportRuleTestDataInfoEntity> insRuleTestExcelVos) {
        String cacheKey = "rule-test:set_".concat(batchId);
        // 将结果存入缓存为Set类型，设置过期时间（例如30分钟）
        try {
            Set<String> dataSet = insRuleTestExcelVos.stream()
                    .map(dto -> JSON.toJSONString(dto))
                    .collect(Collectors.toSet());
            stringRedisTemplate.opsForSet().add(cacheKey, dataSet.toArray(new String[0]));
            stringRedisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("缓存资源分组数据失败", e);
        }
    }

    private List<InsReportRuleTestDataInfoEntity> getRuleTestCache(String batchId) {
        String cacheKey = "rule-test:set_".concat(batchId);
        // 尝试从缓存获取
        try {
            Set<String> cachedDataSet = stringRedisTemplate.opsForSet().members(cacheKey);
            if (CollectionUtil.isNotEmpty(cachedDataSet)) {
                return cachedDataSet.stream()
                        .map(data -> JSON.parseObject(data, InsReportRuleTestDataInfoEntity.class))
                        .collect(Collectors.toList());
            }
            log.info("从缓存中获取数据源分组成功:{}", cachedDataSet.size());
        } catch (Exception e) {
            log.warn("解析缓存数据失败，将从数据库重新加载", e);
        }
        return new ArrayList<>();
    }


    @Override
    public Map<String, Object> analyzeExcelData(List<InsRuleTestExcelVo> list, String batchId, AtomicInteger fail, AtomicInteger success, Map<String, Object> map, List<ChannelInfoVo> allChannelInfo) {
        if (ObjectUtils.isNotEmpty(list)) {
            log.debug("获取数据源信息");
            List<InsReportRuleTestDataInfoEntity> dataInfoEntities;
            if (map.containsKey("dataSource")) {
                dataInfoEntities = (List<InsReportRuleTestDataInfoEntity>) map.get("dataSource");
            } else {
                dataInfoEntities = new ArrayList<>();
            }
            List<InsCarSeriesInfoModel> allCarSeries = iInsCarSeriesInfoService.findAll();
            List<TagLibCategoryVo> tagTree = iInsTagLibClientService.findTagLibClientTree(
                    InsTagLibClientModel.builder()
                            .appClient("764547797eb2e192763f5334028d49c9")
                            .level(4)
                            .tagType("CA")
                            .build()
            );

            List<TagLibClientTreeVo> finalTagLibClientVoList = iInsTagLibClientService.findAllFinalTagLibClientVoList(InsTagLibClientModel.builder().appClient("764547797eb2e192763f5334028d49c9").tagType("CA").build());
            Map<String, List<ChannelInfoVo>> collect = allChannelInfo.stream().collect(Collectors.groupingBy(ChannelInfoVo::getName));
            log.debug("根据渠道名称进行分组:{}", collect);
            long start1 = System.currentTimeMillis();
            list.stream().forEach(e -> {
                InsReportRuleTestDataInfoEntity entity = new InsReportRuleTestDataInfoEntity();
                entity.setId(IdWorker.getId());
                entity.setBatchId(batchId);
                entity.setDataId(IdWorker.getId());
                entity.setChannelName(e.getChannelName());
                entity.setCarSeriesName(e.getCarSeriesName());
                entity.setBrandName(e.getBrandName());
                entity.setBrandCode(BRAND_NAMES_TO_CODES.get(e.getBrandName()));
                List<InsCarSeriesInfoModel> carSeriesInfoModels = allCarSeries.stream().filter(c -> c.getName().equals(e.getCarSeriesName())).toList();
                if (ObjectUtils.isNotEmpty(carSeriesInfoModels)) {
                    entity.setCarSeriesCode(carSeriesInfoModels.get(0).getCode());
                }
                List<TagLibClientTreeVo> topicTagList = finalTagLibClientVoList.stream().filter(c -> c.getTagName().equals(e.getTopic())).toList();
                if (ObjectUtils.isNotEmpty(topicTagList)) {
                    entity.setTopicId(topicTagList.get(0).getTagCode());
                }
                // 替换原代码块为以下优化版本
                if (!ObjectUtils.isEmpty(tagTree)) {
                    Map<Integer, List<TagLibCategoryVo>> levelTagsMap = collectTagsByLevel(tagTree);

                    // 处理各级标签代码获取
                    setTagCodeIfMatch(entity, e.getDomTagFirst(), levelTagsMap, 1,
                            InsReportRuleTestDataInfoEntity::setDomTagFirstCode);
                    setTagCodeIfMatch(entity, e.getDomTagSecond(), levelTagsMap, 2,
                            InsReportRuleTestDataInfoEntity::setDomTagSecondCode);
                    setTagCodeIfMatch(entity, e.getDomTagThree(), levelTagsMap, 3,
                            InsReportRuleTestDataInfoEntity::setDomTagThreeCode);
                    setTagCodeIfMatch(entity, e.getDomTagFour(), levelTagsMap, 4,
                            InsReportRuleTestDataInfoEntity::setDomTagFourCode);
                }
                ContentTypeEnum typeEnum = ContentTypeEnum.getByText(e.getContentType());
                if (ObjectUtils.isNotEmpty(typeEnum)) {
                    entity.setContentType(typeEnum.getCode());
                }
                entity.setPublishUserId(e.getPublishUserId());
                entity.setPublishUserNickname(e.getPublishUserName());
                entity.setMainPostUserId(e.getMainUserId());
                entity.setMainPostUserName(e.getMainUserName());
                entity.setTitle(e.getTitle());
                entity.setTopicText(e.getTopic());
                entity.setContent(e.getContent());
                entity.setSentiment(e.getSentiment());
                entity.setIntention(e.getIntention());
                entity.setDomTagFirst(e.getDomTagFirst());
                entity.setDomTagSecond(e.getDomTagSecond());
                entity.setDomTagThree(e.getDomTagThree());
                entity.setDomTagFour(e.getDomTagFour());
                entity.setCreateTime(LocalDateTime.now());
                if (ObjectUtils.isEmpty(e.getChannelName())) {
                    log.info("当前数据无效,必填项:[{}]为空", e.getChannelName());
                    fail.getAndIncrement();
                } else {
                    if (collect.containsKey(e.getChannelName())) {
                        final List<ChannelInfoVo> channelInfoVos = collect.get(e.getChannelName());
                        final String channelId = channelInfoVos.get(0).getCode();
                        entity.setChannelCode(channelId);
                        success.getAndIncrement();
                    } else {
                        log.error("当前数据无效,渠道[{}]不存在与当前客户中", e.getChannelName());
                        fail.getAndIncrement();
                    }
                }
                dataInfoEntities.add(entity);
            });
            log.info("数据源数据处理完成，共：{}条，有效数据：{}条", list.size(), success.get());
            long end1 = System.currentTimeMillis();
            log.info("数据源数据批量处理完成，总耗时:{}", TimeUnit.MILLISECONDS.toSeconds(end1 - start1) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end1 - start1) + "秒" : (end1 - start1) + "毫秒");
            map.put("dataSource", dataInfoEntities);
        }
        map.put("fail", fail.get());
        map.put("success", success.get());
        return map;
    }

    /**
     * 通用标签代码设置方法
     *
     * @param entity       数据实体
     * @param tagName      标签名称
     * @param levelTagsMap 按层级分类的标签Map
     * @param level        目标层级
     * @param codeSetter   代码设置回调函数
     */
    private void setTagCodeIfMatch(InsReportRuleTestDataInfoEntity entity,
                                   String tagName,
                                   Map<Integer, List<TagLibCategoryVo>> levelTagsMap,
                                   int level,
                                   BiConsumer<InsReportRuleTestDataInfoEntity, String> codeSetter) {
        if (StringUtils.isNotEmpty(tagName) &&
                levelTagsMap.containsKey(level)) {
            levelTagsMap.get(level).stream()
                    .filter(tag -> tagName.equals(tag.getTagName()))
                    .findFirst()
                    .ifPresent(tag -> codeSetter.accept(entity, tag.getTagCode()));
        }
    }

}
