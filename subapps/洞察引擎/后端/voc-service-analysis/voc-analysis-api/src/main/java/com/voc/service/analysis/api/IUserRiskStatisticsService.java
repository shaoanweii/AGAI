package com.voc.service.analysis.api;

import com.voc.service.analysis.model.RiskStatisticModel;

public interface IUserRiskStatisticsService {

    Boolean userRiskStatistics(String clientId, RiskStatisticModel paramModel);

}
