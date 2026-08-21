package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsRiskKeywordsModel;
import com.voc.service.insights.engine.model.InsRiskKeywordsQueryModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/27 下午1:41
 * @描述:
 **/
@FeignClient(name = "service.ins.risk", url = "${service.ins.v1}/keywords", configuration = InsDataServiceClientConfig.class)
public interface InsRiskServiceClient {

    @PostMapping("/queryRiskList")
    Result<List<InsRiskKeywordsModel>> queryRiskList(@RequestBody InsRiskKeywordsQueryModel insRiskKeywordsQueryModel);
}
