package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AnalysisEmotionRiskModel;
import com.voc.service.analysis.model.EmotionRiskDataModel;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.InsRiskSettingVo;

import java.util.List;

public interface IEmotionRiskDataService {

    void saveBatch(String clientId, List<AnalysisEmotionRiskModel> analysisEmotionRiskModelList);


    List<EmotionRiskDataModel> riskEmotionFilter(String clientId, InsRiskSettingVo insRiskSettingVo, BrandVo brandVo,String beginTime, String endTime);

}
