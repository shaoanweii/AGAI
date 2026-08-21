package com.voc.service.analysis.api;

import com.voc.service.analysis.model.RiskStatisticModel;

public interface IQualityRiskStatisticsService {

    Boolean qualityRiskStatistics(String clientId, RiskStatisticModel paramModel,String tagType);

}
