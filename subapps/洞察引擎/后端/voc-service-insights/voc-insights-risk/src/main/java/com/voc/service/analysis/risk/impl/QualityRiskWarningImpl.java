package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.voc.service.analysis.api.IQualityRiskDataService;
import com.voc.service.analysis.api.IQualityRiskWarningService;
import com.voc.service.analysis.api.IRiskDataService;
import com.voc.service.analysis.model.QualityRiskDataModel;
import com.voc.service.analysis.model.RiskStatisticModel;
import com.voc.service.analysis.risk.component.ExtractTag;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.clients.InsProjectServiceClient;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.vo.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class QualityRiskWarningImpl implements IQualityRiskWarningService {


    private static final Logger log = LoggerFactory.getLogger(QualityRiskWarningImpl.class);
    @Resource
    InsProjectServiceClient insProjectServiceClient;

    @Resource
    IQualityRiskDataService iQualityRiskDataService;

    @Resource
    IRiskDataService iRiskDataService;

    @Resource
    ExtractTag extractTag;

    @Value("${ins.project.id}")
    private String projectId;

    @Override
    public Boolean riskQualityFilter(RiskStatisticModel paramModel, String tagType) {

        log.info("质量风险规则计算开始：{}", paramModel);
        ServiceContextHolder.setToken(extractTag.defaultToken);
        Result<List<ProjectInfoVo>> riskWarningInfo = insProjectServiceClient.findRiskWarningInfo(InsProjectInfoModel.builder().clientId(paramModel.getClientId()).id(projectId).build());
        log.info("获取风险规则:{}", JSON.toJSONString(riskWarningInfo));
        if (!riskWarningInfo.isSuccess() || ObjectUtils.isEmpty(riskWarningInfo.getResult())) {
            return Boolean.FALSE;
        }
        List<ProjectInfoVo> warningInfoResult = riskWarningInfo.getResult();
        List<QualityRiskDataModel> qualityRiskDataModelList = new ArrayList<>();
        for (ProjectInfoVo projectInfoVo : warningInfoResult) {
            List<BrandVo> brand = projectInfoVo.getBrand();
            if (ObjectUtils.isEmpty(brand)) {
                continue;
            }
            for (BrandVo brandVo : brand) {
                log.info("品牌:{}", JSON.toJSONString(brandVo));
                List<InsRiskEarlyWarningVo> riskEarlyWarningInfo = brandVo.getRiskEarlyWarning();
                if (ObjectUtils.isEmpty(riskEarlyWarningInfo)) {
                    log.info("风险配置为空");
                    continue;
                }
                List<InsRiskEarlyWarningVo> riskEarlyWarningVos = riskEarlyWarningInfo.stream().filter(r -> r.getWarningType().equals(tagType)).toList();
                if (ObjectUtils.isEmpty(riskEarlyWarningVos)) {
                    log.info("风险类型配置为空");
                    continue;
                }
                InsRiskEarlyWarningVo insRiskEarlyWarningVo = riskEarlyWarningVos.get(0);
                List<InsRiskSettingVo> riskSetting = insRiskEarlyWarningVo.getRiskSetting();
                List<InsRiskLevelVo> riskLevel = insRiskEarlyWarningVo.getRiskLevel();
                for (InsRiskSettingVo insRiskSettingVo : riskSetting) {
                    if (!insRiskSettingVo.getIsApply()) {
                        log.info("风险配置没有启用");
                        continue;
                    }
                    if (!insRiskSettingVo.getPeriodType().equals(paramModel.getStatisticType())) {
                        continue;
                    }
                    List<QualityRiskDataModel> qualityRiskDataModels = iQualityRiskDataService.riskQualityFilter(paramModel.getClientId(), insRiskSettingVo,
                            brandVo, paramModel.getBeginTime(), paramModel.getEndTime());
                    log.info("风险配置匹配统计数据:{}",qualityRiskDataModels.size());
                    if (CollUtil.isEmpty(qualityRiskDataModels)) {
                        log.info("风险配置匹配数据为空");
                        continue;
                    }
                    List<QualityRiskDataModel> dataModels = this.getRiskLevel(qualityRiskDataModels, riskLevel, projectInfoVo);
                    log.info("风险配置匹配:{}",dataModels.size());
                    if (CollUtil.isNotEmpty(dataModels)) {
                        qualityRiskDataModelList.addAll(dataModels);
                    }

                }
            }
        }
        if (CollUtil.isNotEmpty(qualityRiskDataModelList)) {
            iRiskDataService.saveBatchQuality(paramModel.getClientId(), qualityRiskDataModelList);
        }
        return Boolean.TRUE;
    }

    /**
     * 通过risk_index字段计算出风险等级
     *
     * @return
     */
    public List<QualityRiskDataModel> getRiskLevel(List<QualityRiskDataModel> emotionRiskDataModels,
                                                   List<InsRiskLevelVo> riskLevel, ProjectInfoVo projectInfoVo) {

        List<QualityRiskDataModel> dataModelList = new ArrayList<>();
        for (QualityRiskDataModel qualityRiskDataModel : emotionRiskDataModels) {
            BigDecimal G = new BigDecimal(qualityRiskDataModel.getRiskIndex());
            for (InsRiskLevelVo insRiskLevelVo : riskLevel) {
                if (!insRiskLevelVo.getIsApply()) {
                    continue;
                }
                if (G.compareTo(new BigDecimal(insRiskLevelVo.getStartValue())) >= 0 && G.compareTo(new BigDecimal(insRiskLevelVo.getEndValue())) <= 0) {
                    qualityRiskDataModel.setRiskLevel(insRiskLevelVo.getLevel());
                    qualityRiskDataModel.setProjectId(projectInfoVo.getId());
                    dataModelList.add(qualityRiskDataModel);
                }
            }
        }
        return dataModelList;
    }
}
