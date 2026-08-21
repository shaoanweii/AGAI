package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AnalysisQualityRiskModel;
import com.voc.service.analysis.model.QualityRiskDataModel;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsRiskSettingVo;

import java.util.List;

public interface IQualityRiskDataService {

    void saveBatch(String clientId, List<AnalysisQualityRiskModel> analysisQualityRiskModelList);


    List<QualityRiskDataModel> riskQualityFilter(String clientId, InsRiskSettingVo insRiskSettingVo, BrandVo brandVo, String beginTime, String endTime);

}
