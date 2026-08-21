package com.voc.service.insights.engine.api;


import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.UploadModel;
import com.voc.service.insights.engine.api.model.InsRuleTestListModel;
import com.voc.service.insights.engine.model.InsAddRuleTestModel;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public interface IInsRuleTestService {


    PageInfo<RuleTestListVo> ruleTestList(InsRuleTestListModel model);


    Boolean addRuleTestList(InsAddRuleTestModel model);


    PageInfo<InsRuleTestInfoVo> getRuleInfo(InsRuleTestListModel model);


    Boolean copyRuleTest(InsRuleTestListModel model);


    Boolean startRuleTest(InsRuleTestListModel model);

    Boolean delRuleTest(InsRuleTestListModel model);


    RuleTestListVo getInfoRuleId(InsRuleTestListModel model);


    void downloadRuleTest(HttpServletResponse response, Set<ConditionVo> async);


    UploadModel uploadRuleTest(MultipartFile file) throws IOException;

    Map<String, List<InsCategoryRuleVo>> ruleSelect();

    List<String> queryCreateUserList();

    InsRuleTestValidateVo checkUploadRuleTest(InsRuleTestListModel model) throws Exception;

    Map<String, Object> analyzeExcelData(List<InsRuleTestExcelVo> list, String batchId, AtomicInteger fail, AtomicInteger success, Map<String, Object> map, List<ChannelInfoVo> allChannelInfo);
}

