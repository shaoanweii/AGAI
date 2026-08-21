package com.voc.service.risk.api;

import com.voc.service.risk.api.model.VocAnalBatchDataRiskModel;

import java.util.List;

public interface IVocAnalBatchDataRiskService {

    Boolean batchInsert(List<VocAnalBatchDataRiskModel> vocAnalBatchDataRiskList);
}
