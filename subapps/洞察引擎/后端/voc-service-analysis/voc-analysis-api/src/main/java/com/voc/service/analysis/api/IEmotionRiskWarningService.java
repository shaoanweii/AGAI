package com.voc.service.analysis.api;

import com.voc.service.analysis.model.RiskStatisticModel;

public interface IEmotionRiskWarningService {


    Boolean riskEmotionFilter(RiskStatisticModel paramModel,String tagType);

}
