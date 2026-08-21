package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AysProcessDataModel;

import java.util.List;

public interface IAysFinalDataService {

    List<AysProcessDataModel> saveBatch(final String workId, List<AysProcessDataModel> data) throws Exception;

}
