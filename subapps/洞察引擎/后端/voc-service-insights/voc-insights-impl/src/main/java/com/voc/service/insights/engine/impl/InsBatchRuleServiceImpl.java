package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsBatchRuleService;
import com.voc.service.insights.engine.entity.InsBatchRuleEntity;
import com.voc.service.insights.engine.entity.InsBatchRuleHisEntity;
import com.voc.service.insights.engine.entity.InsBatchRuleCategoryEntity;
import com.voc.service.insights.engine.entity.InsIndicatorConditionConfigEntity;
import com.voc.service.insights.engine.mapper.InsBatchRuleCategoryMapper;
import com.voc.service.insights.engine.mapper.InsBatchRuleHisMapper;
import com.voc.service.insights.engine.mapper.InsBatchRuleMapper;
import com.voc.service.insights.engine.mapper.InsIndicatorConditionConfigMapper;
import com.voc.service.insights.engine.model.InsBatchRuleBatchOperationModel;
import com.voc.service.insights.engine.model.InsBatchRuleHisModel;
import com.voc.service.insights.engine.model.InsBatchRuleModel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.voc.service.insights.engine.model.InsBatchRuleQueryModel;
import com.voc.service.insights.engine.vo.InsBatchRegulationConditionConfigVo;
import com.voc.service.insights.engine.vo.InsBatchRuleCountVo;
import com.voc.service.insights.engine.vo.ConditionConfigVo;
import com.voc.service.insights.engine.vo.InsRegulationConditionConfigVo;
import com.voc.service.insights.engine.vo.InsIndicatorConfigVo;
import com.voc.service.insights.engine.vo.InsIndicatorTypeVo;
import com.voc.service.insights.engine.vo.InsIndicatorConditionVo;
import com.voc.service.insights.engine.mapper.InsRegulationConditionConfigMapper;
import com.voc.service.insights.engine.entity.InsRegulationConditionConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量规则服务实现
 * 提供规则的增删改查、复制、批量操作等功能
 */
@Slf4j
@Service
public class InsBatchRuleServiceImpl extends ServiceImpl<InsBatchRuleMapper, InsBatchRuleEntity> implements IInsBatchRuleService {

    @Resource
    private InsBatchRuleHisMapper insBatchRuleHisMapper;

    @Resource
    private InsBatchRuleCategoryMapper insBatchRuleCategoryMapper;

    @Resource
    private InsRegulationConditionConfigMapper regulationConditionConfigMapper;

    @Resource
    private InsIndicatorConditionConfigMapper indicatorConditionConfigMapper;

    /**
     * 常量定义
     */
    private static final String ENABLED = "enabled";
    private static final String DISABLED = "disabled";
    private static final String AUTO = "auto";
    private static final String MANUAL = "manual";
    private static final String BATCH = "batch";
    private static final String HOURLY = "hourly";
    private static final String DAILY = "daily";
    private static final String WEEKLY = "weekly";
    private static final String MONTHLY = "monthly";

    @Override
    public PageInfo<InsBatchRuleModel> queryRulePage(InsBatchRuleQueryModel queryModel) {
        // 1. 分页查询
        PageHelper.startPage(queryModel.getPageNum(), queryModel.getPageSize());
        Page<InsBatchRuleEntity> page = baseMapper.queryRulePage(queryModel);
        
        // 2. 转换为模型
        List<InsBatchRuleModel> resultList = page.getResult().stream()
                .map(entity -> {
                    InsBatchRuleModel model = BeanUtil.copyProperties(entity, InsBatchRuleModel.class);
                    // 3. 补充分类名称
                    if (ObjectUtil.isNotEmpty(model.getCategoryId())) {
                        String categoryName = getCategoryName(model.getCategoryId());
                        model.setCategoryName(categoryName);
                    }
                    return model;
                })
                .collect(Collectors.toList());
        
        // 4. 构建分页结果
        PageInfo<InsBatchRuleModel> pageInfo = new PageInfo<>(resultList);
        pageInfo.setPages(page.getPages());
        pageInfo.setTotal(page.getTotal());
        pageInfo.setPageNum(page.getPageNum());
        pageInfo.setPageSize(page.getPageSize());
        pageInfo.setList(resultList);
        
        return pageInfo;
    }

    @Override
    public InsBatchRuleModel queryRuleDetail(String ruleId) {
        // 1. 查询规则详情
        InsBatchRuleEntity entity = baseMapper.queryRuleById(ruleId);
        if (ObjectUtil.isEmpty(entity)) {
            throw new BussinessException("规则不存在");
        }
        
        // 2. 转换为模型
        InsBatchRuleModel model = BeanUtil.copyProperties(entity, InsBatchRuleModel.class);
        
        // 3. 将 JSON 字符串转换为对象
        if (ObjectUtil.isNotEmpty(model.getAuditor())) {
            model.setAuditor(JSONUtil.parse(model.getAuditor().toString()));
        }
        if (ObjectUtil.isNotEmpty(model.getMainResponder())) {
            model.setMainResponder(JSONUtil.parse(model.getMainResponder().toString()));
        }
        if (ObjectUtil.isNotEmpty(model.getCcPersonnel())) {
            model.setCcPersonnel(JSONUtil.parse(model.getCcPersonnel().toString()));
        }
        
        // 4. 补充分类名称
        if (ObjectUtil.isNotEmpty(model.getCategoryId())) {
            String categoryName = getCategoryName(model.getCategoryId());
            model.setCategoryName(categoryName);
        }
        
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertRule(InsBatchRuleModel ruleModel) {
        // 1. 生成规则ID
        ruleModel.setRuleId(IdWorker.getId());
        
        // 2. 设置默认值
        ruleModel.setVersion(1);
        if (ObjectUtil.isEmpty(ruleModel.getIsEnabled())) {
            ruleModel.setIsEnabled(ENABLED);
        }
        
        // 3. 生成预警cron表达式
        String alertCron = convertAlertTimeToCron(ruleModel.getAlertType(), ruleModel.getAlertFrequency(), ruleModel.getAlertTime());
        ruleModel.setAlertCron(alertCron);
        
        // 4. 检查重复规则
        if (checkDuplicateRule(ruleModel)) {
            throw new BussinessException("存在相同配置的规则");
        }
        
        // 5. 检查规则数量是否超过上限
        if (ENABLED.equals(ruleModel.getIsEnabled())) {
            checkRuleCountLimit(ruleModel.getDimensionConfig());
        }
        
        // 6. 获取操作人信息
        Object operator = getOperator();
        
        // 7. 将 Object 类型字段转换为 JSON 字符串
        if (ObjectUtil.isNotEmpty(ruleModel.getAuditor())) {
            ruleModel.setAuditor(JSONUtil.toJsonStr(ruleModel.getAuditor()));
        }
        if (ObjectUtil.isNotEmpty(ruleModel.getMainResponder())) {
            ruleModel.setMainResponder(JSONUtil.toJsonStr(ruleModel.getMainResponder()));
        }
        if (ObjectUtil.isNotEmpty(ruleModel.getCcPersonnel())) {
            ruleModel.setCcPersonnel(JSONUtil.toJsonStr(ruleModel.getCcPersonnel()));
        }
        if (ObjectUtil.isNotEmpty(operator)) {
            ruleModel.setCreator(JSONUtil.toJsonStr(operator));
            ruleModel.setUpdater(JSONUtil.toJsonStr(operator));
        }
        
        // 8. 构建实体
        InsBatchRuleEntity entity = BeanUtil.copyProperties(ruleModel, InsBatchRuleEntity.class);
        
        // 8. 保存规则
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRule(InsBatchRuleModel ruleModel) {
        // 1. 检查规则是否存在
        InsBatchRuleEntity existingRule = getById(ruleModel.getRuleId());
        if (ObjectUtil.isEmpty(existingRule)) {
            throw new BussinessException("规则不存在");
        }
        
        // 2. 保存历史记录
        saveRuleHistory(existingRule);
        
        // 3. 版本号递增
        ruleModel.setVersion(existingRule.getVersion() + 1);
        
        // 4. 生成预警cron表达式
        String alertCron = convertAlertTimeToCron(ruleModel.getAlertType(), ruleModel.getAlertFrequency(), ruleModel.getAlertTime());
        ruleModel.setAlertCron(alertCron);
        
        // 5. 检查重复规则
        if (checkDuplicateRule(ruleModel)) {
            throw new BussinessException("存在相同配置的规则");
        }
        
        // 6. 检查规则数量是否超过上限（当规则从禁用改为启用时）
        if (ENABLED.equals(ruleModel.getIsEnabled()) && !ENABLED.equals(existingRule.getIsEnabled())) {
            checkRuleCountLimit(ruleModel.getDimensionConfig());
        }
        
        // 7. 获取操作人信息
        Object operator = getOperator();
        
        // 8. 将 Object 类型字段转换为 JSON 字符串
        if (ObjectUtil.isNotEmpty(ruleModel.getAuditor())) {
            ruleModel.setAuditor(JSONUtil.toJsonStr(ruleModel.getAuditor()));
        }
        if (ObjectUtil.isNotEmpty(ruleModel.getMainResponder())) {
            ruleModel.setMainResponder(JSONUtil.toJsonStr(ruleModel.getMainResponder()));
        }
        if (ObjectUtil.isNotEmpty(ruleModel.getCcPersonnel())) {
            ruleModel.setCcPersonnel(JSONUtil.toJsonStr(ruleModel.getCcPersonnel()));
        }
        if (ObjectUtil.isNotEmpty(operator)) {
            ruleModel.setUpdater(JSONUtil.toJsonStr(operator));
        }
        
        // 9. 构建实体
        InsBatchRuleEntity entity = BeanUtil.copyProperties(ruleModel, InsBatchRuleEntity.class);
        
        // 8. 更新规则
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean copyRule(String ruleId) {
        // 1. 检查规则是否存在
        InsBatchRuleEntity existingRule = getById(ruleId);
        if (ObjectUtil.isEmpty(existingRule)) {
            throw new BussinessException("规则不存在");
        }
        
        // 2. 复制规则
        InsBatchRuleEntity newRule = BeanUtil.copyProperties(existingRule, InsBatchRuleEntity.class);
        newRule.setRuleId(IdWorker.getId());
        newRule.setRuleName(newRule.getRuleName() + "（复制）");
        newRule.setVersion(1);
        newRule.setIsEnabled(DISABLED); // 复制的规则默认禁用
        newRule.setCreateTime(new Date());
        newRule.setUpdateTime(new Date());
        
        // 3. 保存新规则
        return save(newRule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(String ruleId) {
        // 1. 检查规则是否存在
        InsBatchRuleEntity rule = getById(ruleId);
        if (ObjectUtil.isEmpty(rule)) {
            throw new BussinessException("规则不存在");
        }
        
        // 2. 删除规则
        return removeById(ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchOperation(InsBatchRuleBatchOperationModel batchOperationModel) {
        // 1. 如果是启用操作，检查规则数量是否超过上限
        if (ENABLED.equals(batchOperationModel.getIsEnabled())) {
            // 计算批量启用的规则数量
            int newRuleCount = batchOperationModel.getIds().size();
            // 查询当前已启用的规则数量
            int enabledRuleCount = baseMapper.countEnabledRules();
            // 检查总数是否超过5000
            if (enabledRuleCount + newRuleCount > 5000) {
                throw new BussinessException("当前系统可配置规则数量已达上限");
            }
            // 检查重复规则
            checkDuplicateRules(batchOperationModel.getIds());
        }
        
        // 2. 批量更新状态
        String updater = JSONUtil.toJsonStr(ServiceContextHolder.getUser());
        LocalDateTime updateTime = LocalDateTime.now();
        
        int result = baseMapper.batchUpdateIsEnabled(
                batchOperationModel.getIsEnabled(),
                batchOperationModel.getIds(),
                updater,
                updateTime
        );
        
        return result;
    }

    @Override
    public Map<String, Integer> countByCategoryIds(Set<String> categoryIds) {
        if (CollUtil.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        
        // 查询每个分类的规则数量
        List<InsBatchRuleCountVo> countVos = baseMapper.countByCategoryIds(categoryIds);
        return countVos.stream()
                .collect(Collectors.toMap(InsBatchRuleCountVo::getCategoryId, InsBatchRuleCountVo::getCount));
    }

    @Override
    public List<InsBatchRuleHisModel> queryRuleHistory(String ruleId) {
        // 查询规则历史记录
        List<InsBatchRuleHisEntity> hisEntities = insBatchRuleHisMapper.selectListByRuleId(ruleId);
        return hisEntities.stream()
                .map(this::convertHisEntityToModel)
                .collect(Collectors.toList());
    }

    @Override
    public InsBatchRuleHisModel queryRuleHistoryDetail(String hisId) {
        // 查询历史记录详情
        InsBatchRuleHisEntity entity = insBatchRuleHisMapper.selectByHisId(hisId);
        if (ObjectUtil.isEmpty(entity)) {
            throw new BussinessException("历史记录不存在");
        }
        return convertHisEntityToModel(entity);
    }
    
    /**
     * 将历史记录实体转换为模型，同时处理 JSON 字符串转换
     */
    private InsBatchRuleHisModel convertHisEntityToModel(InsBatchRuleHisEntity entity) {
        InsBatchRuleHisModel model = BeanUtil.copyProperties(entity, InsBatchRuleHisModel.class);
        
        // 将 JSON 字符串转换为对象
        if (ObjectUtil.isNotEmpty(model.getAuditor())) {
            model.setAuditor(JSONUtil.parse(model.getAuditor().toString()));
        }
        if (ObjectUtil.isNotEmpty(model.getMainResponder())) {
            model.setMainResponder(JSONUtil.parse(model.getMainResponder().toString()));
        }
        if (ObjectUtil.isNotEmpty(model.getCcPersonnel())) {
            model.setCcPersonnel(JSONUtil.parse(model.getCcPersonnel().toString()));
        }
        
        return model;
    }

    /**
     * 保存规则历史记录
     */
    private void saveRuleHistory(InsBatchRuleEntity rule) {
        // 构建历史记录实体
        InsBatchRuleHisEntity hisEntity = BeanUtil.copyProperties(rule, InsBatchRuleHisEntity.class);
        hisEntity.setHisId(IdWorker.getId());
        hisEntity.setEditUser(JSONUtil.toJsonStr(ServiceContextHolder.getUser()));
        hisEntity.setEditTime(new Date());
        
        // 保存历史记录
        insBatchRuleHisMapper.insert(hisEntity);
    }

    /**
     * 转换预警时间为cron表达式
     */
    private String convertAlertTimeToCron(String alertType, String alertFrequency, String alertTime) {
        // 根据预警类型生成不同的cron表达式
        switch (alertType) {
            case HOURLY:
                // 每N小时执行
                int hourInterval = Integer.parseInt(alertFrequency);
                return "0 0 0/" + hourInterval + " * * ?";
            
            case DAILY:
                // 每天固定时间执行
                String[] timeParts = alertTime.split(":");
                if (timeParts.length == 2) {
                    int hour = Integer.parseInt(timeParts[0]);
                    int minute = Integer.parseInt(timeParts[1]);
                    return "0 " + minute + " " + hour + " * * ?";
                }
                break;
            
            case WEEKLY:
                // 每周固定天固定时间执行
                String[] weeklyParts = alertTime.split(" ");
                if (weeklyParts.length == 2) {
                    String dayOfWeek = weeklyParts[0];
                    String time = weeklyParts[1];
                    String[] timeParts2 = time.split(":");
                    if (timeParts2.length == 2) {
                        int hour = Integer.parseInt(timeParts2[0]);
                        int minute = Integer.parseInt(timeParts2[1]);
                        // 转换星期几为数字（1-7，1表示周日）
                        int dayNum = getDayOfWeekNumber(dayOfWeek);
                        return "0 " + minute + " " + hour + " ? * " + dayNum;
                    }
                }
                break;
            
            case MONTHLY:
                // 每月固定日期固定时间执行
                String[] monthlyParts = alertTime.split(" ");
                if (monthlyParts.length == 2) {
                    String dayOfMonth = monthlyParts[0].replace("日", "");
                    String time = monthlyParts[1];
                    String[] timeParts3 = time.split(":");
                    if (timeParts3.length == 2) {
                        int hour = Integer.parseInt(timeParts3[0]);
                        int minute = Integer.parseInt(timeParts3[1]);
                        return "0 " + minute + " " + hour + " " + dayOfMonth + " * ?";
                    }
                }
                break;
        }
        return null;
    }

    /**
     * 检查重复规则
     */
    private boolean checkDuplicateRule(InsBatchRuleModel ruleModel) {
        // 构建规则配置的MD5值
        String configMd5 = generateConfigMd5(ruleModel.getDimensionConfig(), ruleModel.getIndicatorConfig());
        
        // 查询是否存在相同配置的启用规则
        // 这里需要实现具体的查询逻辑
        // 暂时返回false，实际项目中需要实现
        return false;
    }

    /**
     * 检查批量规则是否重复
     */
    private void checkDuplicateRules(Set<String> ruleIds) {
        // 实现批量规则的重复检查
        // 暂时不实现，实际项目中需要实现
    }

    /**
     * 生成配置的MD5值
     */
    private String generateConfigMd5(String dimensionConfig, String indicatorConfig) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((dimensionConfig + indicatorConfig).getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("生成MD5失败", e);
            return null;
        }
    }

    /**
     * 检查规则数量是否超过上限
     *
     * @param dimensionConfig 维度配置
     * @throws BussinessException 规则数量超过上限时抛出
     */
    private void checkRuleCountLimit(String dimensionConfig) throws BussinessException {
        // 1. 计算本次规则数量
        int newRuleCount = calculateRuleCount(dimensionConfig);
        
        // 2. 查询当前已启用的规则数量
        int enabledRuleCount = baseMapper.countEnabledRules();
        
        // 3. 检查总数是否超过5000
        if (enabledRuleCount + newRuleCount > 5000) {
            throw new BussinessException("当前系统可配置规则数量已达上限");
        }
    }

    /**
     * 计算规则数量
     *
     * @param dimensionConfig 维度配置
     * @return 规则数量
     */
    private int calculateRuleCount(String dimensionConfig) {
        try {
            // 解析维度配置JSON
            Map<String, Object> configMap = JSONUtil.parseObj(dimensionConfig);
            
            // 初始化规则数量为1
            int ruleCount = 1;
            
            // 遍历所有维度，计算独立计算的维度选项数量的乘积
            for (Map.Entry<String, Object> entry : configMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Map) {
                    Map<String, Object> dimensionMap = (Map<String, Object>) value;
                    // 检查是否为独立计算
                    if ("independent".equals(dimensionMap.get("calculationType"))) {
                        // 获取选项列表
                        Object optionsObj = dimensionMap.get("options");
                        if (optionsObj instanceof List) {
                            List<?> options = (List<?>) optionsObj;
                            // 乘以选项数量
                            ruleCount *= options.size();
                        }
                    }
                }
            }
            
            return ruleCount;
        } catch (Exception e) {
            log.error("计算规则数量失败", e);
            // 解析失败时默认返回1
            return 1;
        }
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(String categoryId) {
        if (ObjectUtil.isEmpty(categoryId)) {
            return null;
        }
        
        InsBatchRuleCategoryEntity category = insBatchRuleCategoryMapper.selectById(categoryId);
        return ObjectUtil.isNotEmpty(category) ? category.getName() : null;
    }

    /**
     * 转换星期几为数字
     */
    private int getDayOfWeekNumber(String dayOfWeek) {
        Map<String, Integer> dayMap = MapUtil.<String, Integer>builder()
                .put("周日", 1)
                .put("周一", 2)
                .put("周二", 3)
                .put("周三", 4)
                .put("周四", 5)
                .put("周五", 6)
                .put("周六", 7)
                .build();
        return dayMap.getOrDefault(dayOfWeek, 1);
    }

    @Override
    public List<InsBatchRegulationConditionConfigVo> findConditionConfig() {
        QueryWrapper<InsRegulationConditionConfigEntity> queryWrapper = new QueryWrapper<>();
        // 只查询canuse包含2的记录（仅给批量用或两个都能用）
        queryWrapper.apply("FIND_IN_SET('2', canuse)");
        // 按 sort_order 字段升序排序
        queryWrapper.orderByAsc("sort_order");
        final List<InsRegulationConditionConfigEntity> insRegulationConditionConfigEntities = regulationConditionConfigMapper.selectList(queryWrapper);
        if (ObjectUtil.isEmpty(insRegulationConditionConfigEntities)) {
            log.info("暂无条件配置");
            return List.of();
        }
        Map<String, List<InsRegulationConditionConfigEntity>> collect = insRegulationConditionConfigEntities.stream().collect(Collectors.groupingBy(InsRegulationConditionConfigEntity::getCode, LinkedHashMap::new, Collectors.toList()));
        return collect.entrySet().stream().map(e -> {
            final List<InsRegulationConditionConfigEntity> value = e.getValue();
            InsRegulationConditionConfigEntity insRegulationConditionConfigEntity = value.stream().findAny().get();
            Set<ConditionConfigVo> logicalOperator = new LinkedHashSet<>();
            Set<ConditionConfigVo> condition = new LinkedHashSet<>();
            Set<ConditionConfigVo> countingMethod = new LinkedHashSet<>();
            Set<String> keys = new HashSet<>();
            value.stream().forEach(k -> {
                if (keys.add(k.getLogicalOperatorCode())) {
                    logicalOperator.add(ConditionConfigVo.builder()
                            .name(trimWhitespace(k.getLogicalOperatorName()))
                            .code(trimWhitespace(k.getLogicalOperatorCode()))
                            .build());
                }
                if (keys.add(k.getTypeCode())) {
                    condition.add(ConditionConfigVo.builder()
                            .name(trimWhitespace(k.getTypeName()))
                            .code(trimWhitespace(k.getTypeCode()))
                            .build());
                }
                if (ObjectUtil.isNotEmpty(k.getCountingCode()) && keys.add(k.getCountingCode())) {
                    countingMethod.add(ConditionConfigVo.builder()
                            .name(trimWhitespace(k.getCountingName()))
                            .code(trimWhitespace(k.getCountingCode()))
                            .build());
                }
            });
            return InsBatchRegulationConditionConfigVo.builder()
                    .name(trimWhitespace(insRegulationConditionConfigEntity.getName()))
                    .code(trimWhitespace(insRegulationConditionConfigEntity.getCode()))
                    .logicalOperator(logicalOperator)
                    .condition(condition)
                    .countingMethod(countingMethod)
                    .build();
        }).collect(Collectors.toList());
    }
    
    /**
     * 去除字符串中的空白字符（包括换行符、空格、制表符等）
     * @param str 原始字符串
     * @return 去除空白字符后的字符串
     */
    private String trimWhitespace(String str) {
        if (ObjectUtil.isEmpty(str)) {
            return str;
        }
        return str.replaceAll("\\s+", "");
    }

    @Override
    public List<InsIndicatorConfigVo> findIndicatorConditionConfig() {
        // 查询批量规则可用的指标条件配置
        List<InsIndicatorConditionConfigEntity> entities = indicatorConditionConfigMapper.selectByCanuse("2");
        if (ObjectUtil.isEmpty(entities)) {
            log.info("暂无指标条件配置");
            return List.of();
        }

        // 按指标编码分组，保持按ID排序的顺序
        Map<String, List<InsIndicatorConditionConfigEntity>> indicatorMap = entities.stream()
                .collect(Collectors.groupingBy(InsIndicatorConditionConfigEntity::getIndicatorCode,
                        LinkedHashMap::new, Collectors.toList()));

        return indicatorMap.entrySet().stream().map(entry -> {
            String indicatorCode = entry.getKey();
            List<InsIndicatorConditionConfigEntity> indicatorEntities = entry.getValue();

            // 获取指标名称
            String indicatorName = indicatorEntities.get(0).getIndicatorName();

            // 按类型编码分组，保持按ID排序的顺序
            Map<String, List<InsIndicatorConditionConfigEntity>> typeMap = indicatorEntities.stream()
                    .collect(Collectors.groupingBy(InsIndicatorConditionConfigEntity::getTypeCode,
                            LinkedHashMap::new, Collectors.toList()));

            List<InsIndicatorTypeVo> types = typeMap.entrySet().stream().map(typeEntry -> {
                String typeCode = typeEntry.getKey();
                List<InsIndicatorConditionConfigEntity> typeEntities = typeEntry.getValue();

                // 获取类型名称
                String typeName = typeEntities.get(0).getTypeName();

                // 构建条件列表，按ID排序
                List<InsIndicatorConditionVo> conditions = typeEntities.stream()
                        .sorted(Comparator.comparing(InsIndicatorConditionConfigEntity::getId))
                        .map(entity ->
                        InsIndicatorConditionVo.builder()
                                .operatorName(entity.getOperatorName())
                                .operatorCode(entity.getOperatorCode())
                                .valueTypeName(entity.getValueTypeName())
                                .valueTypeCode(entity.getValueTypeCode())
                                .valueFormat(entity.getValueFormat())
                                .build()
                ).collect(Collectors.toList());

                return InsIndicatorTypeVo.builder()
                        .name(typeName)
                        .code(typeCode)
                        .conditions(conditions)
                        .build();
            }).collect(Collectors.toList());

            return InsIndicatorConfigVo.builder()
                    .name(indicatorName)
                    .code(indicatorCode)
                    .types(types)
                    .build();
        }).collect(Collectors.toList());
    }
    
    /**
     * 生成操作人信息
     * @return 操作人信息对象
     */
    private Object getOperator() {
        try {
            Map<String, Object> operator = new HashMap<>();
            operator.put("id", ServiceContextHolder.getUserId());
            operator.put("employeeId", ServiceContextHolder.getUsername());
            operator.put("name", ServiceContextHolder.getUser().getFirstname());
            return operator;
        } catch (Exception e) {
            log.warn("获取操作人信息失败: {}", e.getMessage());
            return null;
        }
    }
}
