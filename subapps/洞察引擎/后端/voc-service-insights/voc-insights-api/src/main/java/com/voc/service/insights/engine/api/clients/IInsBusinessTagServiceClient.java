package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsBusinessTagModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

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
@FeignClient(name = "service.ins.buzTag", url = "${service.ins.v1}/businessTag")
public interface IInsBusinessTagServiceClient {
    @PostMapping("/findAll")
    Result<List<InsBusinessTagModel>> findAll();

}
