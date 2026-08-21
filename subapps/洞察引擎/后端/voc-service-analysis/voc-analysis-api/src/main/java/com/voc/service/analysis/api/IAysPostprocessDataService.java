package com.voc.service.analysis.api;

import cn.hutool.json.JSONObject;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.model.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAysPostprocessDataService {

    Set<String> saveBatch(String clientId,List<AysProcessDataModel> data) throws Exception;

    int modifyToDone(String clientId,Set<String> ids, String workId);

//    long removeHistoryData(String clientId,int days);

//    PageInfo getResultDataList(String clientId, ResultDataParamModel resultDataParamModel);

//    List<JSONObject> exportResultData(String clientId, ResultDataParamModel resultDataParamModel);

//    long remove(String clientId, Set<String> insertIds) throws Exception;
//    long removeDB(String clientId, Set<String> ids);

    List<AysPreprocessDataModel> findByIds(String clientId, Set<String> ids);

//    PageInfo getCommonDataList(String clientId, ResultDataParamModel resultDataParamModel);


//    PageInfo getProjectResultDataList(String clientId, ProjectResultDataParamModel dataParamModel);


    ResultConditionsModel projectConditions(String clientId,ProjectResultDataParamModel dataParamModel);

    Boolean testData();

    Boolean modifyResultdata(ModifyDataModel model) throws Exception;

}
