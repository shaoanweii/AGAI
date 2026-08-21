package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AysModelResltDataAnalysisMissModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.MissDataParamModel;
import com.voc.service.analysis.model.OpinionsModel;

import java.util.List;

public interface IAysModelResltAnalysisMissService {
    void saveBatch(String clientId, List<AysModelResltDataAnalysisMissModel> modelNotLabelDataList) throws Exception;

    List<Object> getMissDataList(String clientId, MissDataParamModel model);
}
