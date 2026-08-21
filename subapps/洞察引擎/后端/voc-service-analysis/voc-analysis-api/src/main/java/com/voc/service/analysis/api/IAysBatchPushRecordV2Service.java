package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AysBatchPushRecordExceptionModel;
import com.voc.service.analysis.model.AysBatchPushRecordGroupByModel;
import com.voc.service.analysis.model.AysBatchPushRecordV2Model;

import java.util.List;
import java.util.Set;

/**
 * @Title: IAysBatchPushRecordService
 * @Package: com.voc.service.analysis.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/6/11 10:23
 * @Version:1.0
 */
public interface IAysBatchPushRecordV2Service {

    void save(final String clientId, String workId, String reqeustId, Set<String> ids,Integer modelType) throws Exception;

//    String findByReqeustId(String clientId, String reqeustId);

//    Set<String> findByNewId(String clientId, Set<String> newId);

    boolean modifyStatus(String clientId, final Set<String> ids, final String status, final String source) throws Exception;

    List<AysBatchPushRecordGroupByModel> findGroupByRequestId(String clientId, String workId);

    List<AysBatchPushRecordExceptionModel> findExceptionRecordList(String clientId, List<String> ids);

    long modifyStatusDB(String clientId, final Set<String> ids, final String status, final String source) throws Exception;

    Set<String> isExitsIds(String clientId,Set<String> paramIds);
}
