package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AnalysisUserRiskModel;
import com.voc.service.analysis.model.UserRiskDataModel;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsRiskSettingVo;

import java.util.List;

public interface IUserRiskDataService {

    void saveBatch(String clientId, List<AnalysisUserRiskModel> analysisUserRiskModelList);

    void deleteRisk();


    List<UserRiskDataModel> riskUserFilter(String clientId, InsRiskSettingVo insRiskSettingVo, BrandVo brandVo,String beginTime, String endTime);

}
