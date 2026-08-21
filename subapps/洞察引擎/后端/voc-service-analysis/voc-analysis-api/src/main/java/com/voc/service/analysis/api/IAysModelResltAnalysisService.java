package com.voc.service.analysis.api;

import com.voc.service.analysis.model.*;

import java.util.List;
import java.util.Set;

public interface IAysModelResltAnalysisService {
    Long moveBatch(String clientId, final String workId, AysValidDataModel validResltDataParam) throws Exception;


    void saveBatchExtAnalysis(String clientId, List<AysModelResltDataAnalysisModel> modelResltDataAnalysisModels) throws Exception;

    void saveBatchAnalysis(String clientId, List<AysModelResltDataAnalysisModel> modelResltDataAnalysisModels) throws Exception;

    int modifyToDone(String clientId, Set<String> ids);

    void moveModelResultDataToFinalTable(String clientId, Set<String> ids);

    List<AysModelResltDataAnalysisModel> findByWorkId(String clientId, String workId);

    long dataCount(AysValidDataModel validResltDataParam);

    long removeHistoryData(String clientId, int days);

    List<AysModelResltDataAnalysisModel> findByIds(String clientId, Set<String> ids);

    ResultConditionsModel conditions(String clientId, ResultConditionsParamModel paramModel);

    Set<String> isExitsIds(String clientId, Set<String> paramIds);

    Set<String> unprocessedIds(String clientId, Set<String> paramIds);

    void modifyUnmigratedDataScopToDone(String clientId, Set<String> ids);

    Set< String> findUnmigratedDataScop(String clientId);

    List<AysAnalFlowModelTagsResultDataExtModel> finchResultData(String clientId, Set<String> ids);
}
