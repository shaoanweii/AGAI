package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsDataSourceRequestModel;
import com.voc.service.insights.engine.vo.InsDataSourceResultVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/24 下午5:38
 * @描述:
 **/
//@FeignClient(name = "service.integertaion.v1", url = "http://localhost:8089")
@FeignClient(name = "service.integertaion.v1", url = "${service.integertaion.v1}")
public interface IIntegrationServiceClient {
    @PostMapping("/findVerificationResult")
    Result<InsDataSourceResultVo> getDataSourceResult(InsDataSourceRequestModel requestModel);
    @PostMapping("/findVerificationResultByCondition")
    Result<List<InsDataSourceResultVo>> findVerificationResultByCondition(InsDataSourceRequestModel dataRequestModel);

}
