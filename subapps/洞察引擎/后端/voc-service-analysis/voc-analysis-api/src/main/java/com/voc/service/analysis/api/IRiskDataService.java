package com.voc.service.analysis.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.model.*;

import java.util.List;

public interface IRiskDataService {

    PageInfo getRiskResultList(String clientId, RiskDataParamModel paramModel);


    List<AllTypesRiskDataModel> exportRiskResultList(String clientId, RiskDataParamModel paramModel);

    Boolean saveBatchEmotion(String clientId, List<EmotionRiskDataModel> emotionRiskDataModelList);

    Boolean saveBatchQuality(String clientId, List<QualityRiskDataModel> qualityRiskDataModelList);

    Boolean saveBatchUser(String clientId, List<UserRiskDataModel> userRiskDataModelList);

}
