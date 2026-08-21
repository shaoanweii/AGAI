package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.ICqCaRiskDataAnalysisService;
import com.voc.service.insights.engine.api.IInsClosedRuleService;
import com.voc.service.insights.engine.entity.*;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.*;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.ConditionConfigVo;
import com.voc.service.insights.engine.vo.InsClosedRuleCountVo;
import com.voc.service.insights.engine.vo.InsClosedRuleMd5Vo;
import com.voc.service.insights.engine.vo.InsRegulationConditionConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 闭环规则服务实现类
 */
@Slf4j
@Service
public class InsClosedRuleServiceImpl extends ServiceImpl<InsClosedRuleMapper, InsClosedRuleEntity> implements IInsClosedRuleService {

    public static final String MANUAL = "manual";
    public static final String SINGLE = "single";
    public static final String WECHAT = "wechat";
    public static final String ICHANGAN = "ichangan";
    public static final String STR_ZERO = "0";
    public static final String HOURLY = "hourly";
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";
    public static final String CONCAT_CHAR = "-";
    public static final String CONCAT_CHAR1 = ",";
    public static final String CONCAT_CHAR2 = ", ";

    @Resource
    private InsClosedRuleConditionMapper insClosedRuleConditionMapper;

    @Resource
    private InsClosedRuleAlertMapper insClosedRuleAlertMapper;

    @Resource
    private InsClosedRuleConditionHisMapper insClosedRuleConditionHisMapper;

    @Resource
    private InsConvertMapperService insConvertMapperService;

    @Autowired
    @Lazy
    private ICqCaRiskDataAnalysisService iCqCaRiskDataAnalysisService;

    @Autowired
    private InsRegulationConditionConfigMapper regulationConditionConfigMapper;

    @Override
    public PageInfo<InsClosedRuleModel> queryRulePage(InsClosedRuleQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        Page<InsClosedRuleEntity> vocPage = baseMapper.queryRulePage(model);
        List<InsClosedRuleModel> resultList = insConvertMapperService.closedRuleEntityConvertModelList(vocPage.getResult());
        PageInfo<InsClosedRuleModel> page = new PageInfo<>(resultList);
        page.setPages(vocPage.getPages());
        page.setTotal(vocPage.getTotal());
        page.setPageNum(vocPage.getPageNum());
        page.setPageSize(vocPage.getPageSize());
        page.setList(resultList);
        return page;
    }

    @Override
    public InsClosedRuleModel queryRuleDetail(String ruleId) {
        // 使用多线程异步查询各个部分
        CompletableFuture<InsClosedRuleEntity> ruleFuture = CompletableFuture.supplyAsync(() ->
                baseMapper.queryRuleById(ruleId)
        );

        CompletableFuture<List<InsClosedRuleConditionEntity>> conditionsFuture = CompletableFuture.supplyAsync(() ->
                selectRuleConditions(ruleId)
        );

        CompletableFuture<InsClosedRuleAlertEntity> alertFuture = CompletableFuture.supplyAsync(() ->
                selectRuleAlert(ruleId)
        );

        // 等待所有查询完成
        CompletableFuture.allOf(ruleFuture, conditionsFuture, alertFuture).join();
        InsClosedRuleEntity ruleEntity = ruleFuture.join();
        List<InsClosedRuleConditionEntity> conditionEntities = conditionsFuture.join();
        InsClosedRuleAlertEntity alertEntity = alertFuture.join();

        // 组装结果
        InsClosedRuleModel ruleModel = insConvertMapperService.closedRuleEntityConvertModel(ruleEntity);
        if (ObjectUtil.isNotNull(ruleModel)) {
            ruleModel.setConditions(insConvertMapperService.closedRuleConditionEntityConvertModelList(conditionEntities));
            ruleModel.setRuleAlert(insConvertMapperService.closedRuleAlertEntityConvertModel(alertEntity));
        }

        return ruleModel;
    }

    /**
     * 查询规则条件配置
     *
     * @param ruleId 规则ID
     * @return 规则条件配置实体列表
     */
    private List<InsClosedRuleConditionEntity> selectRuleConditions(String ruleId) {
        return insClosedRuleConditionMapper.listByRuleId(ruleId);
    }

    /**
     * 查询规则预警配置
     *
     * @param ruleId 规则ID
     * @return 规则预警配置实体
     */
    private InsClosedRuleAlertEntity selectRuleAlert(String ruleId) {
        return insClosedRuleAlertMapper.queryRuleAlertById(ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRule(InsClosedRuleModel ruleModel) {
        checkData(ruleModel);
        // 如果是启用状态，检查是否为重复条件
        checkIsRepetition(ruleModel, null);
        InsClosedRuleModel.InsClosedRuleUser operator = getOperator();

        // 1. 生成规则ID
        String ruleId = IdWorker.getId();
        ruleModel.setRuleId(ruleId);

        // 2. 设置默认版本号为1
        ruleModel.setVersion(1);

        // 3. 保存规则基础信息
        InsClosedRuleEntity ruleEntity = insConvertMapperService.closedRuleModelConvertEntity(ruleModel);
        ruleEntity.setCreator(operator);
        ruleEntity.setUpdater(operator);
        ruleEntity.setCreateTime(LocalDateTime.now());
        ruleEntity.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(ruleEntity);

        //保存条件配置
        saveRuleCondition(insConvertMapperService.closedRuleConditionModelConvertEntityList(ruleModel.getConditions()),
                ruleId, operator, null);

        //保存预警配置
        saveRuleAlert(insConvertMapperService.closedRuleAlertModelConvertEntity(ruleModel.getRuleAlert()),
                ruleId, false);

        return true;
    }

    private void checkIsRepetition(InsClosedRuleModel ruleModel, String ruleId) {
        // 如果是启用状态，检查是否为重复条件
        if (ENABLED.equals(ruleModel.getIsEnabled())) {
            //通过数据源、品牌、条件配置计算md5值
            String resultStr = JSONUtil.toJsonStr(ruleModel.getDataSource()).replaceAll(CONCAT_CHAR1, CONCAT_CHAR2);
            resultStr += CONCAT_CHAR + ruleModel.getBrandCode() + CONCAT_CHAR;
            List<InsClosedRuleConditionModel> conditions = new ArrayList<>(ruleModel.getConditions());
            conditions.sort(Comparator.comparing(InsClosedRuleConditionModel::getConditionType));
            for (int i = 0; i < conditions.size(); i++) {
                InsClosedRuleConditionModel conditionModel = conditions.get(i);
                resultStr += conditionModel.getConditionType() + CONCAT_CHAR + conditionModel.getOperator() + CONCAT_CHAR + conditionModel.getOption() + CONCAT_CHAR + conditionModel.getValueType() + CONCAT_CHAR + conditionModel.getValue() + CONCAT_CHAR1;
            }
            resultStr = resultStr.substring(0, resultStr.length() - 1);
            // 检查是否存在相同的条件配置
            String ruleName = baseMapper.selectRepetitionCount(ruleId, resultStr);
            if (CharSequenceUtil.isNotBlank(ruleName)) {
                throw new BussinessException(String.format("已存在重复并启用的闭环规则【%s】，无法启用", ruleName));
//                throw new IllegalArgumentException(String.format("已存在重复并启用的闭环规则【%s】，无法启用", ruleName));
            }
        }
    }

    /**
     * 生成操作人
     *
     * @author: LiuQiang
     * @date: 2025/11/6 17:50
     * @return: com.voc.service.insights.engine.model.InsClosedRuleModel.InsClosedRuleUser
     **/
    private InsClosedRuleModel.InsClosedRuleUser getOperator() {
        String username = ServiceContextHolder.getUsername();
        String userId = ServiceContextHolder.getUserId();
        String name = ServiceContextHolder.getUser().getFirstname();
        InsClosedRuleModel.InsClosedRuleUser operator = InsClosedRuleModel.InsClosedRuleUser.builder()
                .id(userId)
                .employeeId(username)
                .name(name)
                .build();
        return operator;
    }

    /**
     * 检查规则数据是否合法
     *
     * @param ruleModel 规则模型
     * @author: LiuQiang
     * @date: 2025/11/6 18:54
     * @return: void
     **/
    private void checkData(InsClosedRuleModel ruleModel) {
        if (ObjectUtil.isNull(ruleModel.getConfirmer()) || ObjectUtil.isNull(ruleModel.getConfirmDepartment())) {
            ruleModel.setConfirmer(ruleModel.getMainResponder());
            ruleModel.setConfirmDepartment(ruleModel.getMainDepartment());
        }

        //如果是单点且没有设置预警周期，默认一个预警周期
        if (SINGLE.equals(ruleModel.getRuleType()) && ObjectUtil.isNull(ruleModel.getRuleAlert())) {
            InsClosedRuleAlertModel alertModel = InsClosedRuleAlertModel.builder().alertChannel(List.of(WECHAT, ICHANGAN)).alertTime(STR_ZERO).alertFrequency(STR_ZERO).alertType(HOURLY).build();
            ruleModel.setRuleAlert(alertModel);
        }

        //检查抄送人
        if (CollUtil.isNotEmpty(ruleModel.getCcPersonnel())) {
            for (InsClosedRuleModel.InsClosedRuleCcPersonnel ccPersonnel : ruleModel.getCcPersonnel()) {
                //为false的时候判断用户的id、工号、姓名是否为空
                if (Boolean.FALSE.equals(ccPersonnel.getIsAll())) {
                    if (ObjectUtil.isNull(ccPersonnel.getUserId())) {
                        throw new IllegalArgumentException("抄送人ID不能为空");
                    }
                    if (ObjectUtil.isNull(ccPersonnel.getEmployeeId())) {
                        throw new IllegalArgumentException("抄送人工号不能为空");
                    }
                    if (ObjectUtil.isNull(ccPersonnel.getUserName())) {
                        throw new IllegalArgumentException("抄送人姓名不能为空");
                    }
                }
            }
        }

        // 数据源使用自然排序 后续匹配是否重复规则有用
        if (CollUtil.isNotEmpty(ruleModel.getDataSource())) {
            ruleModel.getDataSource().sort(null);
        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRule(InsClosedRuleModel ruleModel) {
        checkData(ruleModel);
        // 如果是启用状态，检查是否为重复条件
        checkIsRepetition(ruleModel, ruleModel.getRuleId());
        String ruleId = ruleModel.getRuleId();

        // 1. 查询当前规则信息
        InsClosedRuleEntity currentRule = baseMapper.selectById(ruleId);
        if (currentRule == null) {
            throw new RuntimeException("规则不存在: " + ruleId);
        }

        InsClosedRuleModel.InsClosedRuleUser operator = getOperator();
        InsClosedRuleEntity ruleEntity = insConvertMapperService.closedRuleModelConvertEntity(ruleModel);
        ruleEntity.setCreator(null);
        ruleEntity.setVersion(currentRule.getVersion() + 1);
        ruleEntity.setCreateTime(null);
        ruleEntity.setUpdater(operator);
        ruleEntity.setUpdateTime(LocalDateTime.now());


        // 2.更新规则基础信息
        baseMapper.updateById(ruleEntity);

        //保存条件配置
        saveRuleCondition(insConvertMapperService.closedRuleConditionModelConvertEntityList(ruleModel.getConditions()),
                ruleId, operator, currentRule);

        //保存预警配置
        saveRuleAlert(insConvertMapperService.closedRuleAlertModelConvertEntity(ruleModel.getRuleAlert()),
                ruleId, true);

        //调用定时删除定时任务接口
        if (DISABLED.equals(ruleModel.getIsEnabled())) {
            iCqCaRiskDataAnalysisService.delJob(List.of(ruleId));
        }

        return true;
    }

    @Override
    @SwitchClientDS
    public boolean copyRule(String ruleId) {
        // 1. 查询当前规则信息
        InsClosedRuleModel currentRule = queryRuleDetail(ruleId);
        if (currentRule == null) {
            throw new RuntimeException("规则不存在: " + ruleId);
        }
        InsClosedRuleEntity ruleEntity = insConvertMapperService.closedRuleModelConvertEntity(currentRule);

        String newId = IdWorker.getId();


        InsClosedRuleModel.InsClosedRuleUser operator = getOperator();
        ruleEntity.setRuleId(newId);
        ruleEntity.setRuleName(ruleEntity.getRuleName() + "复制");
        ruleEntity.setVersion(1);
        ruleEntity.setCreator(operator);
        ruleEntity.setUpdater(operator);
        ruleEntity.setCreateTime(LocalDateTime.now());
        ruleEntity.setUpdateTime(LocalDateTime.now());
        //所有复制出来的规则默认都是禁用的
        ruleEntity.setIsEnabled(DISABLED);

        baseMapper.insert(ruleEntity);

        //保存条件配置
        saveRuleCondition(insConvertMapperService.closedRuleConditionModelConvertEntityList(currentRule.getConditions()),
                newId, operator, null);

        //保存预警配置
        saveRuleAlert(insConvertMapperService.closedRuleAlertModelConvertEntity(currentRule.getRuleAlert()),
                newId, false);

        return true;
    }

    @Override
    public Map<String, Integer> countByCategoryIds(Set<String> categoryIds) {
        if (CollUtil.isEmpty(categoryIds)) {
            return MapUtil.empty();
        }
        List<InsClosedRuleCountVo> list = baseMapper.countByCategoryIds(categoryIds);
        return list.stream().collect(Collectors.toMap(InsClosedRuleCountVo::getCategoryType, InsClosedRuleCountVo::getCount));
    }

    @Override
    public List<InsRegulationConditionConfigVo> findConditionConfig() {
        QueryWrapper<InsRegulationConditionConfigEntity> queryWrapper = new QueryWrapper<>();
        // 只查询canuse包含1的记录（仅给单点用或两个都能用）
        queryWrapper.apply("FIND_IN_SET('1', canuse)");
        final List<InsRegulationConditionConfigEntity> insRegulationConditionConfigEntities = regulationConditionConfigMapper.selectList(queryWrapper);
        if (ObjectUtils.isEmpty(insRegulationConditionConfigEntities)) {
            log.info("暂无条件配置");
            return List.of();
        }
        Map<String, List<InsRegulationConditionConfigEntity>> collect = insRegulationConditionConfigEntities.stream().collect(Collectors.groupingBy(InsRegulationConditionConfigEntity::getCode));
        return collect.entrySet().stream().map(e -> {
            final List<InsRegulationConditionConfigEntity> value = e.getValue();
            InsRegulationConditionConfigEntity insRegulationConditionConfigEntity = value.stream().findAny().get();
            Set<ConditionConfigVo> logicalOperator = new HashSet<>();
            Set<ConditionConfigVo> condition = new HashSet<>();
            Set<String> keys = new HashSet<>();
            value.stream().forEach(k -> {
                if (keys.add(k.getLogicalOperatorCode())) {
                    logicalOperator.add(ConditionConfigVo.builder().name(k.getLogicalOperatorName()).code(k.getLogicalOperatorCode()).build());
                }
                if (keys.add(k.getTypeCode())) {
                    condition.add(ConditionConfigVo.builder().name(k.getTypeName()).code(k.getTypeCode()).build());
                }
            });
            return InsRegulationConditionConfigVo.builder()
                    .name(insRegulationConditionConfigEntity.getName())
                    .code(insRegulationConditionConfigEntity.getCode())
                    .logicalOperator(logicalOperator)
                    .condition(condition)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @SwitchClientDS
    public Integer batchOperation(InsClosedBatchOperationModel batchOperationModel) {
        Set<String> idSet = getBatchOperationIds(batchOperationModel);
        if (CollUtil.isEmpty(idSet)) {
            return 0;
        }
        boolean isEnabled = ENABLED.equals(batchOperationModel.getIsEnabled());
        Set<String> duplicateRuleIds = null;
        String isEnabledStr = DISABLED;
        if (isEnabled) {
            isEnabledStr = ENABLED;

            //校验是否有重复的已启用的规则，将其过滤掉
            List<InsClosedRuleMd5Vo> md5List = baseMapper.selectRepetitionCountBatch(idSet);

            // 过滤掉具有相同md5Str的ruleId
            // 1. 将md5List按md5Str分组
            Map<String, List<InsClosedRuleMd5Vo>> md5GroupMap = md5List.stream()
                    .collect(Collectors.groupingBy(InsClosedRuleMd5Vo::getMd5Str));

            // 2. 找出有重复md5Str的ruleId集合
            duplicateRuleIds = md5GroupMap.values().stream()
                    .filter(list -> list.size() > 1) // 只处理有重复的组
                    .flatMap(List::stream)
                    .map(InsClosedRuleMd5Vo::getRuleId)
                    .collect(Collectors.toSet());

            // 3. 从idSet中移除这些重复的ruleId
            idSet.removeAll(duplicateRuleIds);

            // 4. 如果过滤后idSet为空，则抛出异常
            if (CollUtil.isEmpty(idSet)) {
                throw new IllegalArgumentException("所选规则中存在重复配置，无法全部启用");
            }
        }

        LocalDateTime updateTime = LocalDateTime.now();
        InsClosedRuleModel.InsClosedRuleUser operator = getOperator();
        int updateCount = baseMapper.batchUpdateIsEnabled(isEnabledStr, idSet, JSONUtil.toJsonStr(operator), updateTime);
//        if (CollUtil.isNotEmpty(duplicateRuleIds)) {
//            throw new IllegalArgumentException(String.format("所选规则中存在%s条重复规则，本次为你启用了%s条数据", duplicateRuleIds.size(), updateCount));
//        }
        //调用定时删除定时任务接口
        if (!isEnabled) {
            iCqCaRiskDataAnalysisService.delJob(new ArrayList<>(idSet));
        }
        return updateCount;
    }

    @Override
    public List<String> findRuleIdsByCategoryIds(Set<String> categoryIds) {
        if (CollUtil.isEmpty(categoryIds)) {
            return List.of();
        }
        return this.baseMapper.findIdsByCategoryIds(categoryIds,ENABLED);
    }

    /**
     * 获取批量操作的规则ID集合
     *
     * @param batchOperationModel 批量操作模型
     * @return 规则ID集合
     */
    private Set<String> getBatchOperationIds(InsClosedBatchOperationModel batchOperationModel) {
        Set<String> idSet = batchOperationModel.getIds().stream().filter(CharSequenceUtil::isNotBlank).collect(Collectors.toSet());
        return idSet;
    }

    /**
     * 保存条件配置
     * 如果是更新规则，需要保存条件配置到历史表
     *
     * @param conditions  条件配置
     * @param ruleId      规则ID
     * @param operator    操作人
     * @param currentRule 库里的规则
     * @author: LiuQiang
     * @date: 2025/11/6 17:53
     * @return: void
     **/
    private void saveRuleCondition(List<InsClosedRuleConditionEntity> conditions, String ruleId, InsClosedRuleModel.InsClosedRuleUser operator, InsClosedRuleEntity currentRule) {
        if (ObjectUtil.isNotNull(currentRule)) {
            // 1. 查询当前规则的条件配置
            List<InsClosedRuleConditionEntity> currentConditions = selectRuleConditions(ruleId);
            LocalDateTime now = LocalDateTime.now();
            List<InsClosedRuleConditionHisEntity> hisEntities = BeanUtil.copyToList(currentConditions, InsClosedRuleConditionHisEntity.class);
            hisEntities.stream().forEach(item -> {
                item.setEditUser(operator);
                item.setVersion(currentRule.getVersion());
                item.setEditTime(now);
            });

            //保存到历史表
            insClosedRuleConditionHisMapper.insertBatch(hisEntities);

            //删除当前规则的条件配置
            insClosedRuleConditionMapper.delete(Wrappers.<InsClosedRuleConditionEntity>lambdaQuery()
                    .eq(InsClosedRuleConditionEntity::getRuleId, ruleId));
        }

        // 2. 保存新的条件配置
        for (int i = 0; i < conditions.size(); i++) {
            InsClosedRuleConditionEntity item = conditions.get(i);
            item.setRuleId(ruleId);
            item.setConditionId(IdWorker.getId());
            item.setSortOrder(i + 1);
        }

        insClosedRuleConditionMapper.insertBatch(conditions);
    }

    /**
     * 保存预警配置
     * 如果是更新规则，需要先删除后新增
     *
     * @param alertEntity 预警配置
     * @param ruleId      规则ID
     * @param isUpdate    是否更新
     * @author: LiuQiang
     * @date: 2025/11/6 18:09
     * @return: void
     **/
    private void saveRuleAlert(InsClosedRuleAlertEntity alertEntity, String ruleId, boolean isUpdate) {
        if (isUpdate) {
            //删除当前规则的预警配置
            insClosedRuleAlertMapper.delete(Wrappers.<InsClosedRuleAlertEntity>lambdaQuery()
                    .eq(InsClosedRuleAlertEntity::getRuleId, ruleId));
        }

        alertEntity.setRuleId(ruleId);
        alertEntity.setAlertPushId(IdWorker.getId());
        alertEntity.setAlertCron(convertAlertTimeToCron(alertEntity.getAlertType(), alertEntity.getAlertFrequency(), alertEntity.getAlertTime()));
        insClosedRuleAlertMapper.insert(alertEntity);
    }

    /**
     * 转换预警时间为cron表达式
     * 根据最新的描述重新实现该方法
     *
     * @param alertType      预警周期：hourly=时，daily=日，weekly=周，monthly=月
     * @param alertFrequency 预警频次，如“周期是时的 2 4 8 16，周期是日的 固定为0，周期是周的 1 2 3 ... 6 7，周期是月的 1 2 3 ... 30 31”
     * @param alertTime      预警时间，如“周期是时的直接为0，其余的都是时分秒 08:00:00”
     * @return cron表达式
     */
    private String convertAlertTimeToCron(String alertType, String alertFrequency, String alertTime) {
        if (alertType == null || alertFrequency == null || alertTime == null) {
            return CharSequenceUtil.EMPTY; // 默认为空
        }

        try {
            switch (alertType) {
                case "hourly":
                    // 小时级预警：alertFrequency为小时数（2,4,8,16），alertTime为0
                    // 转换为每N小时执行一次，格式：秒 分 时 * * ?
                    int hourInterval = Integer.parseInt(alertFrequency.trim());
                    if (alertFrequency.equals(STR_ZERO)) {
                        return "* * * * * ?"; // 实时，表示数据进来就行判断
                    } else if (hourInterval > 0 && hourInterval <= 24) {
                        return "0 0 0/" + hourInterval + " * * ?"; // 每N小时执行
                    }
                    throw new IllegalArgumentException("小时级预警周期的频次必须在2,4,8,16之间");

                case "daily":
                    // 每日预警：alertFrequency固定为0，alertTime为时分秒（08:00:00）
                    // 转换为每天固定时间执行，格式：秒 分 时 * * ?
                    if (alertTime.matches("\\d{2}:\\d{2}:\\d{2}")) {
                        String[] timeParts = alertTime.split(":");
                        return timeParts[2] + " " + timeParts[1] + " " + timeParts[0] + " * * ?"; // 每天固定时间执行
                    }
                    throw new IllegalArgumentException("每日预警周期的时间格式必须为HH:mm:ss");

                case "weekly":
                    // 每周预警：alertFrequency为周几（1-7），alertTime为时分秒（08:00:00）
                    // 转换为每周指定天执行，格式：秒 分 时 ? * 星期几
                    if (alertTime.matches("\\d{2}:\\d{2}:\\d{2}")) {
                        String[] timeParts = alertTime.split(":");
                        int dayOfWeek = Integer.parseInt(alertFrequency.trim());
                        if (dayOfWeek >= 1 && dayOfWeek <= 7) {
                            // 将1-7转换为cron表达式中的星期表示（1=周日，2=周一，...，7=周六）
                            String[] weekDays = {"2", "3", "4", "5", "6", "7", "1"};
                            return timeParts[2] + " " + timeParts[1] + " " + timeParts[0] + " ? * " + weekDays[dayOfWeek - 1];
                        }
                        throw new IllegalArgumentException("每周预警周期的周几必须在1-7之间");
                    }
                    throw new IllegalArgumentException("每周预警周期的时间格式必须为HH:mm:ss");


                case "monthly":
                    // 每月预警：alertFrequency为几号（1-31），alertTime为时分秒（08:00:00）
                    // 转换为每月指定日期执行，格式：秒 分 时 日 * ?
                    if (alertTime.matches("\\d{2}:\\d{2}:\\d{2}")) {
                        String[] timeParts = alertTime.split(":");
                        int dayOfMonth = Integer.parseInt(alertFrequency.trim());
                        if (dayOfMonth >= 1 && dayOfMonth <= 31) {
                            return timeParts[2] + " " + timeParts[1] + " " + timeParts[0] + " " + dayOfMonth + " * ?";
                        }
                        throw new IllegalArgumentException("每月预警周期的几号必须在1-31之间");
                    }
                    throw new IllegalArgumentException("每月预警周期的时间格式必须为HH:mm:ss");

                default:
                    return "0 0 * * * ?"; // 默认每小时执行
            }
        } catch (NumberFormatException e) {
            log.warn("预警参数格式错误 - alertType: {}, alertFrequency: {}, alertTime: {}", alertType, alertFrequency, alertTime);
            return "0 0 * * * ?"; // 错误时返回默认每小时执行
        }
    }


}