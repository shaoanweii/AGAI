package com.voc.service.analysis.api;

import com.voc.service.analysis.model.RiskStatisticModel;

public interface IQualityRiskWarningService {


    Boolean riskQualityFilter(RiskStatisticModel paramModel, String tagType);

}
