package com.voc.service.analysis.risk.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.voc.service.analysis.api.IRiskDataService;
import com.voc.service.analysis.api.IUserRiskDataService;
import com.voc.service.analysis.api.IUserRiskWarningService;
import com.voc.service.analysis.model.RiskStatisticModel;
import com.voc.service.analysis.model.UserRiskDataModel;
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
public class UserRiskWarningImpl implements IUserRiskWarningService {


    private static final Logger log = LoggerFactory.getLogger(UserRiskWarningImpl.class);
    @Resource
    InsProjectServiceClient insProjectServiceClient;
    @Resource
    IUserRiskDataService iUserRiskDataService;

    @Resource
    IRiskDataService iRiskDataService;

    @Resource
    ExtractTag extractTag;

    @Value("${ins.project.id}")
    private String projectId;

    @Override
    public Boolean riskUserFilter(RiskStatisticModel paramModel) {

        log.info("用户风险规则计算开始：{}",paramModel);
        ServiceContextHolder.setToken(extractTag.defaultToken);
        Result<List<ProjectInfoVo>> riskWarningInfo = insProjectServiceClient.findRiskWarningInfo(InsProjectInfoModel.builder().clientId(paramModel.getClientId()).id(projectId).build());
        log.info("获取风险规则:{}", JSON.toJSONString(riskWarningInfo));
        if (!riskWarningInfo.isSuccess() || ObjectUtils.isEmpty(riskWarningInfo.getResult())) {
            return Boolean.FALSE;
        }
        List<ProjectInfoVo> warningInfoResult = riskWarningInfo.getResult();
        List<UserRiskDataModel> userRiskDataModelList = new ArrayList<>();
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
                List<InsRiskEarlyWarningVo> riskEarlyWarningVos = riskEarlyWarningInfo.stream().filter(r -> r.getWarningType().equals("CM")).toList();
                if (ObjectUtils.isEmpty(riskEarlyWarningVos)) {
                    log.info("风险类型配置为空");
                    continue;
                }
                InsRiskEarlyWarningVo insRiskEarlyWarningVo = riskEarlyWarningVos.get(0);
                List<InsRiskSettingVo> riskSetting = insRiskEarlyWarningVo.getRiskSetting();
                List<InsRiskLevelVo> riskLevel = insRiskEarlyWarningVo.getRiskLevel();
                for (InsRiskSettingVo insRiskSettingVo : riskSetting) {
                    if(!insRiskSettingVo.getIsApply()){
                        log.info("风险配置没有启用");
                        continue;
                    }
                    if (!insRiskSettingVo.getPeriodType().equals(paramModel.getStatisticType())) {
                        continue;
                    }
                    List<UserRiskDataModel> userRiskDataModels = iUserRiskDataService.riskUserFilter(paramModel.getClientId(), insRiskSettingVo, brandVo,
                            paramModel.getBeginTime(), paramModel.getEndTime());
                    log.info("风险配置匹配统计数据:{}",userRiskDataModels.size());
                    if (CollUtil.isEmpty(userRiskDataModels)) {
                        log.info("风险配置匹配数据为空");
                        continue;
                    }
                    List<UserRiskDataModel> dataModels = this.getRiskLevel(userRiskDataModels, riskLevel, projectInfoVo);
                    log.info("风险配置匹配:{}",dataModels.size());
                    if (CollUtil.isNotEmpty(userRiskDataModels)) {
                        userRiskDataModelList.addAll(dataModels);
                    }

                }
            }
        }
        if (CollUtil.isNotEmpty(userRiskDataModelList)) {
            iRiskDataService.saveBatchUser(paramModel.getClientId(), userRiskDataModelList);
        }
        return Boolean.TRUE;
    }

    /**
     * 通过risk_index字段计算出风险等级
     *
     * @return
     */
    public List<UserRiskDataModel> getRiskLevel(List<UserRiskDataModel> userRiskDataModelList,
                                                List<InsRiskLevelVo> riskLevel, ProjectInfoVo projectInfoVo) {

        List<UserRiskDataModel> dataModelList = new ArrayList<>();
        for (UserRiskDataModel userRiskDataModel : userRiskDataModelList) {
            BigDecimal G = new BigDecimal(userRiskDataModel.getRiskIndex());
            for (InsRiskLevelVo insRiskLevelVo : riskLevel) {
                if(!insRiskLevelVo.getIsApply()){
                    continue;
                }
                if (G.compareTo(new BigDecimal(insRiskLevelVo.getStartValue())) >= 0 && G.compareTo(new BigDecimal(insRiskLevelVo.getEndValue())) <= 0) {
                    userRiskDataModel.setRiskLevel(insRiskLevelVo.getLevel());
                    userRiskDataModel.setProjectId(projectInfoVo.getId());
                    dataModelList.add(userRiskDataModel);
                }
            }
        }
        return dataModelList;
    }
}
