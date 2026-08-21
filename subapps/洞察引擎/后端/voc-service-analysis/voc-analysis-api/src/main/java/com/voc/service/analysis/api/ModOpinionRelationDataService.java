package com.voc.service.analysis.api;

import com.voc.service.analysis.model.OpinionRelationDataParamModel;

import java.util.List;

public interface ModOpinionRelationDataService {
    Boolean saveOpinionRelationData(List<OpinionRelationDataParamModel> paramModelList);

    OpinionRelationDataParamModel queryLastData(String clientId);

    Long queryDataCount(String opinionId,String clientId);
}
