package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.voc.service.analysis.risk.mapper.BatchRuleDataMapper;
import com.voc.service.analysis.risk.utils.*;
import com.voc.service.common.util.IdWorker;
import com.voc.service.risk.api.IBatchRiskDataAnalysisService;
import com.voc.service.risk.api.IVocAnalBatchDataRiskService;
import com.voc.service.risk.api.model.BatchTaskConditionsModel;
import com.voc.service.risk.api.model.BatchWarningTaskRunModel;
import com.voc.service.risk.api.model.VocAnalBatchDataRiskModel;
import com.voc.service.risk.api.vo.BatchRuleDataVo;
import com.voc.service.risk.api.vo.BatchTaskResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class IBatchRiskDataAnalysisServiceImpl implements IBatchRiskDataAnalysisService {

    @Autowired
    BatchRuleDataMapper batchRuleDataMapper;

    @Autowired
    ConditionAssignUtil conditionAssignUtil;

    @Autowired
    DailyResetCodeGenerator dailyResetCodeGenerator;

    @Autowired
    IVocAnalBatchDataRiskService vocAnalBatchDataRiskService;

    // 定义一个静态的汽车品牌集合
    public static final Map<String, String> MAP_BRANDS = Map.of(
            "A01", "长安引力",
            "A02", "长安凯程",
            "A03", "深蓝汽车",
            "A04", "阿维塔",
            "A05", "长安启源"
    );


    @Override
    public Boolean batchWarningTaskRun(BatchWarningTaskRunModel param) {

        List<BatchRuleDataVo> batchRuleDataVos = batchRuleDataMapper.queryBatchRuleData(param);
        log.info("批量规则数据:{}", batchRuleDataVos.size());
        if (CollectionUtil.isEmpty(batchRuleDataVos)) {
            return false;
        }
        for (BatchRuleDataVo batchRuleDataVo : batchRuleDataVos) {
            String dimensionConfig = batchRuleDataVo.getDimensionConfig();
            log.info("维度配置:{}", dimensionConfig);
            if (ObjectUtils.isEmpty(dimensionConfig)) {
                continue;
            }
            String indicatorConfig = batchRuleDataVo.getIndicatorConfig();
            log.info("指标配置:{}", indicatorConfig);
            if (ObjectUtils.isEmpty(indicatorConfig)) {
                continue;
            }
            BatchTaskConditionsModel batchTaskConditionsModel = conditionAssignUtil.buildConditionsModel(dimensionConfig);
            log.info("批量任务条件:{}", JSONUtil.toJsonStr(batchTaskConditionsModel));
            batchTaskConditionsModel.setBrandCode(batchRuleDataVo.getBrandCode());
            String alertType = batchRuleDataVo.getAlertType();
            String alertTime = batchRuleDataVo.getAlertTime();
            batchTaskConditionsModel.setAvgDayType(alertType);
            VocTimeUtil.buildTimeParam(alertType, alertTime, batchTaskConditionsModel);
            log.info("时间参数:{}", JSONUtil.toJsonStr(batchTaskConditionsModel));
            String whereClause = SqlConditionGenerator.buildWhereClause(indicatorConfig, batchTaskConditionsModel);
            TopRankParser.parseTopRank(indicatorConfig, batchTaskConditionsModel);
            log.info("SQL条件:{}", whereClause);
            batchTaskConditionsModel.setWhereClause(whereClause);
            List<BatchTaskResultVo> batchTaskResultVos = batchRuleDataMapper.queryBatchSoundsData(batchTaskConditionsModel);
            log.info("批量任务结果:{}", JSON.toJSONString(batchTaskResultVos));
            if (CollectionUtil.isEmpty(batchTaskResultVos)) {
                continue;
            }
            // 组装并发送数据
            List<VocAnalBatchDataRiskModel> vocAnalBatchDataRiskList = this.assembleData(batchTaskResultVos, batchRuleDataVo, batchTaskConditionsModel);
            log.info("组装并发送数据:{}", vocAnalBatchDataRiskList.size());
            if (CollectionUtil.isNotEmpty(vocAnalBatchDataRiskList)) {
                Boolean batchInsert = vocAnalBatchDataRiskService.batchInsert(vocAnalBatchDataRiskList);
                log.info("批量插入数据:{}", batchInsert);
                return batchInsert;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean createJob() {
        log.info("创建长安批量风险任务");
        List<BatchRuleDataVo> batchRuleDataVos = batchRuleDataMapper.queryBatchRuleData(new BatchWarningTaskRunModel());
        log.info("获取长安批量风险任务:{}", batchRuleDataVos.size());
        if (CollectionUtil.isEmpty(batchRuleDataVos)) {
            log.info("没有需要执行的批量风险任务");
            return Boolean.FALSE;
        }
        int deleted = batchRuleDataMapper.deleteJobIdList();
        log.info("删除批量风险任务条数:{}", deleted);
        Integer addJob = 0;
        for (BatchRuleDataVo batchRuleDataVo : batchRuleDataVos) {
            batchRuleDataMapper.addJob(batchRuleDataVo.getRuleId(), batchRuleDataVo.getAlertCron());
            addJob++;
        }
        log.info("添加长安批量风险任务完成:{}", addJob);
        return Boolean.TRUE;
    }

    private List<VocAnalBatchDataRiskModel> assembleData(List<BatchTaskResultVo> batchTaskResultVos,
                                                         BatchRuleDataVo batchRuleDataVo, BatchTaskConditionsModel batchTaskConditionsModel) {
        List<VocAnalBatchDataRiskModel> entityList = new ArrayList<>();
        for (BatchTaskResultVo batchTaskResultVo : batchTaskResultVos) {
            VocAnalBatchDataRiskModel entity = new VocAnalBatchDataRiskModel();

            entity.setBrandCode(batchTaskConditionsModel.getBrandCode());
            entity.setBrandName(MAP_BRANDS.get(batchTaskConditionsModel.getBrandCode()));
            entity.setMentionCount(batchTaskResultVo.getCurrTotal().toString());
            entity.setMentionCountRate(batchTaskResultVo.getCurrTotalMom() != null ? batchTaskResultVo.getCurrTotalMom().toString() : null);
            entity.setNegativeRate(batchTaskResultVo.getCurrNegativeRate() != null ? batchTaskResultVo.getCurrNegativeRate().toString() : null);
            entity.setNegativeRateR(batchTaskResultVo.getCurrNegMom() != null ? batchTaskResultVo.getCurrNegMom().toString() : null);
            entity.setId(IdWorker.getId());
            String avgDayType = batchTaskConditionsModel.getAvgDayType();
            String warningPeriod = switch (avgDayType) {
                case "daily" -> "日";
                case "weekly" -> "周";
                case "monthly" -> "月";
                default -> ""; // 未知值默认空，可根据业务改
            };
            entity.setWarningPeriod(warningPeriod);
            entity.setCarSeriesCode(batchTaskResultVo.getCarSeriesName());
            entity.setCarSeriesName(batchTaskResultVo.getCarSeriesName());
            entity.setTopic(batchTaskResultVo.getTopic());
            entity.setTopicName(batchTaskResultVo.getTopic());
            entity.setRuleId(batchRuleDataVo.getRuleId());
            entity.setIds(batchTaskResultVo.getCurrIdList());
            entity.setWarningTime(LocalDateTime.now());
            entity.setWarningEventNo(dailyResetCodeGenerator.generateCode());
            entity.setEventName(batchRuleDataVo.getRuleName());
            entity.setSubjectCategoryId(batchRuleDataVo.getCategoryId());
            entity.setSubjectCategoryName(batchRuleDataVo.getCategoryName());
            entity.setEventPriority(batchRuleDataVo.getProcessPriority());
            entity.setEventPriorityName(batchRuleDataVo.getProcessPriority());
            entity.setReviewMethod(batchRuleDataVo.getAuditMethod());
            if (StringUtils.isNotBlank(batchRuleDataVo.getAuditor())) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> auditorMap = JSON.parseObject(batchRuleDataVo.getAuditor(), Map.class);
                    entity.setReviewUserId((String) auditorMap.get("id"));
                    entity.setReviewUserEmpNo((String) auditorMap.get("employeeId"));
                    entity.setReviewUserName((String) auditorMap.get("name"));
                } catch (Exception e) {
                    log.error("解析审核人JSON失败: {}", batchRuleDataVo.getAuditor(), e);
                }
            }
            if (StringUtils.isNotBlank(batchRuleDataVo.getMainResponder())) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mainResponderMap = JSON.parseObject(batchRuleDataVo.getMainResponder(), Map.class);
                    entity.setMainRespUserId((String) mainResponderMap.get("id"));
                    entity.setMainRespUserEmpNo((String) mainResponderMap.get("employeeId"));
                    entity.setMainRespUserName((String) mainResponderMap.get("name"));
                } catch (Exception e) {
                    log.error("解析主响应人JSON失败: {}", batchRuleDataVo.getMainResponder(), e);
                }
            }
            if (ObjectUtils.isNotEmpty(batchRuleDataVo.getCcPersonnel()) && !batchRuleDataVo.getCcPersonnel().equals("[]")) {
                entity.setCcUsers(batchRuleDataVo.getCcPersonnel());
            }
            entity.setUpdateTime(LocalDateTime.now());
            entity.setCreateTime(LocalDateTime.now());
            entity.setTaskStatus("0");

            entityList.add(entity);
        }
        return entityList;
    }
}
