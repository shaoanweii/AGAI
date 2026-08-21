package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.api.model.ProjectResultDataParamModel;
import com.voc.service.insights.engine.api.model.RawDataParamModel;
import com.voc.service.insights.engine.api.model.ResultDataParamModel;

public interface IAysPostprocessDataService {

    String TYPE = "rawData";
    void exportResultDataTask(ResultDataParamModel paramModel) throws Exception;


    void exportProjectResultDataTask(ProjectResultDataParamModel paramModel) throws Exception;

}
