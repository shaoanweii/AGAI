package com.voc.service.analysis.api;

import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.AysValidDataModel;

import java.util.List;
import java.util.Set;

public interface IAysModelResltService {

    void saveBatch(String clientId ,List<AysProcessDataModel> data) throws Exception;

    int modifyToDone(String clientId ,Set<String> ids) throws Exception;
    int modifyToException(String clientId ,Set<String> ids);
    int modifyToDoneDB(String clientId, Set<String> ids);
    long removeHistoryData(String clientId ,int days);
    List<AysProcessDataModel> findByIds(String clientId, Set<String> ids);
}
