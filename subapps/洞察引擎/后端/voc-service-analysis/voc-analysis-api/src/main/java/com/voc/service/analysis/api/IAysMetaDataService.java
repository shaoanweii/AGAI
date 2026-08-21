package com.voc.service.analysis.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.voc.service.analysis.model.AysMetaDataModel;

import java.util.List;

public interface IAysMetaDataService {

    void save(final String workId, final String source, final String clientId, final String channelId, final String contentType, List<Object> data,String dataSource) throws Exception;

    void save(final String clientId,final String workId, String ins, List<Object> data,Integer modelType, String dataSource) throws Exception;

    void save(final String clientId,final String workId, String ins, List<Object> data, boolean isDone,Integer modelType,String dataSource) throws Exception;

//    List<AnalysisDataModel> findIds(final Set<String> ids) throws JsonProcessingException;

//    AysMetaDataModel findIincompleteData();

//    int modifyByWorkId(String workId);

//    String copySourceData(Set<String> metaDataIds);

//    void retryingRecords(List<String> list);

    long removeHistoryData(final String clientId,int days);

    List<AysMetaDataModel> findByWorkId(final String clientId,String workId);

    int updateStatus(final String clientId,String workId);
}
