package com.voc.service.insights.engine.api;


import com.voc.service.insights.engine.api.model.WarningTaskRunModel;

import java.util.List;

public interface ICqCaRiskDataAnalysisService {

    Boolean createJob();

    Boolean delJob(List<String> ruleId);

    Boolean warningTaskRun(WarningTaskRunModel param);

    Boolean warningTestTaskRun(WarningTaskRunModel param);

    Boolean publicOpinionDistinct();

}
