package com.voc.service.analysis.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.model.AysModelResltDataAnalysisValidModel;
import com.voc.service.analysis.model.AysPreprocessDataModel;
import com.voc.service.analysis.model.AysValidDataModel;
import com.voc.service.analysis.model.AysValidResltDataModel;

import java.util.List;
import java.util.Set;

public interface IAysModelResltAnalysisValidService {


    int modifyToDone(String clientId,Set<String> ids);

    List<AysModelResltDataAnalysisValidModel> readData(String clientId,AysValidDataModel validResltDataParam);


    AysModelResltDataAnalysisValidModel getClientIdByWorkId(String clientId,String workId);

    AysValidResltDataModel validDataCondition(AysValidDataModel param);


    PageInfo<AysModelResltDataAnalysisValidModel> find(String clientId,AysValidDataModel validResltDataParam, int size);

    long remove(String clientId,String workId);

    String getOldWorkByWorkId(String clientId,String workId);

    long removeHistoryData(String clientId,int days);

    Set<String> findIincompleteData(String clientId);

    List<AysModelResltDataAnalysisValidModel> findByIds(String clientId, Set<String> ids);
}
