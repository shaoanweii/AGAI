package com.voc.service.analysis.api;

import cn.hutool.json.JSONObject;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.model.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAysMetaDataAnalysisService {

//    List<AysMetaDataAnalysisModel> saveBatch(final String workId, final String clientId, final String channelId, final String contentType, List<Object> data) throws Exception;

//    List<AysMetaDataAnalysisModel> saveBatch(final String clientId, final String workId, List<Object> data) throws Exception;

//    List<AysMetaDataAnalysisModel> saveBatch(final String clientId, String workId, List<Object> data, boolean isSave) throws Exception;

//    List<AysMetaDataAnalysisModel> findByWorkId(final String clientId, List<String> workIdList);

//    void saveErrorMsg(final String clientId, List<AysMetaDataAnalysisModel> errorList);

    int modifyToDone(final String clientId, Set<String> ids);

//    int modifyToDataStatus(final String clientId, Set<String> ids, String dataStatus);


    int modifyToDataStatus(String clientId, Map<String, Integer> dataStatusMap);

//    List<AysMetaDataAnalysisModel> readUnprocessedData(@Param("size") int size);

//    long removeHistoryData(final String clientId, int days);

//    Set<String> findIincompleteData(final String clientId);

//    Set<String> findByNewIdList(String clientId, Set<String> ids);

//    List<AysMetaDataAnalysisModel> findByIds(final String clientId, Set<String> ids);

    Set<String> findDataIdListByIds(final String clientId, Set<String> ids);

    List<AysMetaDataAnalysisModel> findByIds(final String clientId, Set<String> ids);

    Map<String, String> getMD5Values(AysMetaDataAnalysisModel data);

//    PageInfo getRawDataResult(final String clientId, RawDataParamModel rawDataListModel);


//    List<JSONObject> exportRawDataResult(final String clientId, RawDataParamModel rawDataListModel);

    Set<String> isExitsIds(final String clientId, Set<String> paramIds);

    Set<String> unprocessedIds(final String clientId, Set<String> paramIds);


    Set<String> saveBatchMq(String clientId, String workId, String reqeustId, String type, String dataSource, List<Object> param, Integer modelType, Integer showType) throws Exception;
    Set<String> saveBatchExtMq(String clientId, String workId, String reqeustId, String type, String dataSource, List<Object> param, Integer modelType, Integer showType) throws Exception;


//    PageInfo getProjectRawDataResult(final String clientId, ProjectRawDataParamModel paramModel);


//    List<JSONObject> getFailDataList(String clientId, RawDataParamModel rawDataListModel);

//    List<DataStatusModel> getDataResultStatus(String clientId, RawDataParamModel rawDataListModel);

    void modifyToDataStatusMq(String clientId, Map<String, Integer> dataStatusMap) throws Exception;
}
