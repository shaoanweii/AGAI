package com.voc.service.analysis.clients;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.AddHighFrequencyWordsModel;
import com.voc.service.insights.engine.model.InsBusinessTagModel;
import com.voc.service.insights.engine.vo.AysRegulationInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName IInsBusinessTagService
 * @Description
 * @createTime 2023年12月22日 19:15
 * @Copyright futong
 */
//@FeignClient(name = "", path = "/businessTag")
@FeignClient(name = "service.ins.words", url = "${service.ins.v1}")
public interface IInsHighFrequencyWordsClient {

    @PostMapping("/words/addHighFrequencyWords")
    Result<Boolean> addHighFrequencyWordsClient(@RequestBody AddHighFrequencyWordsModel wordsModel);

}
