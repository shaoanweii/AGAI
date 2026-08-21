package com.voc.service.risk.api;

import com.voc.service.risk.api.model.BatchWarningTaskRunModel;

public interface IBatchRiskDataAnalysisService {

    Boolean batchWarningTaskRun(BatchWarningTaskRunModel param);

    Boolean createJob();
}
