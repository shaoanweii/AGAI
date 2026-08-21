package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AysPreprocessDataModel;
import com.voc.service.analysis.model.AysProcessDataModel;

import java.util.List;
import java.util.Set;

public interface IAysPreprocessDataService {

    Set<String> saveBatch(String clientId, List<AysProcessDataModel> data) throws Exception;


    int modifyToDone(String clientId, Set<String> ids);

//    List<AysProcessDataModel> readUnprocessedData(@Param("size") int size);

//    void retryingRecords(List<String> list);

//    long removeHistoryData(String clientId,int days);

    Set<String> unprocessedIds(String clientId, Set<String> paramIds);


    List<AysPreprocessDataModel> findByIds(String clientId, Set<String> ids);

//    Set<String> findIincompleteData();

    String findWorkId(String clientId);

    Set<String> isExitsIds(String clientId, Set<String> paramIds);

}
