package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.api.model.ProjectRawDataParamModel;
import com.voc.service.insights.engine.api.model.RawDataParamModel;

public interface IAysMetaDataAnalysisService {

    String TYPE = "rawResultData";
    void exportRawDataResultTask(RawDataParamModel paramModel) throws Exception;


    void exportProjectRawDataResultTask(ProjectRawDataParamModel paramModel) throws Exception;



}
