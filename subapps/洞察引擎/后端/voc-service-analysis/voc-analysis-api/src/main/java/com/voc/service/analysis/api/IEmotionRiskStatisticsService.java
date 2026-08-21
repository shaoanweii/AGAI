package com.voc.service.analysis.api;

import com.voc.service.analysis.model.RiskStatisticModel;

public interface IEmotionRiskStatisticsService {

    Boolean emotionRiskStatistics(String clientId, RiskStatisticModel paramModel,String tagType);
}
