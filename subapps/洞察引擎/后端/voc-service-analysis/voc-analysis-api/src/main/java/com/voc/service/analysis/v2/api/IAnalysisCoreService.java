package com.voc.service.analysis.v2.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.model.AiBatchPushModel;
import com.voc.service.analysis.model.AysValidDataModel;
import com.voc.service.analysis.model.AysValidResltDataModel;
import com.voc.service.analysis.model.ValidDataModel;
import com.voc.service.analysis.v2.dto.PushParamDto;

import java.util.List;
import java.util.Set;

public interface IAnalysisCoreService {

    /**
     * 获取分析核心服务
     *
     * @return
     */

    String push(List<Object> param) throws Exception;

    String push(String clientId, List<Object> param) throws Exception;

    String push(String reqeustId, String workId, String clientId, String type,String dataSource, List<Object> param,Integer modelType,Integer showType) throws Exception;

    String preRulesProcess() throws Exception;

    String postRulesProcess() throws Exception;

    String callModel() throws Exception;

    AysValidResltDataModel valid(AysValidDataModel param) throws Exception;

    String validateFlow(AysValidDataModel param) throws Exception;

    PageInfo getValidateList(ValidDataModel validDataModel);



//    Object templateProcess(String clientId, List<Object> param);

    String batchPushData(String clientId, String reqeustId, String type, AiBatchPushModel param) throws Exception;

    String testModelMq() throws Exception;

    Set<String> pauseAction(final Set<String> topics);

    Set<String> resumeAction(final Set<String> topics);
}
