package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.AysProcessValidDataModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAysPostprocessValidDataService {

    List<AysProcessDataModel> saveBatch(String clientId, final String oldWorkId
            , List<AysProcessDataModel> data) throws Exception;

//    Map<String, String> getAttributes();

    List<AysProcessValidDataModel> getProcessValidData(String workId, String clientId,List<String> channelId);

    int modifyToDone(String clientId, Set<String> ids, String workId);

    long removeHistoryData(String clientId, int days);
}
