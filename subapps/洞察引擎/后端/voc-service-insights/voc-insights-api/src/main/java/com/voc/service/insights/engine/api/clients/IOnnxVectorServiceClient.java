package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;


@FeignClient(name = "service.ai.onnx", url = "${service.onnx.v1}")
public interface IOnnxVectorServiceClient {

    @PostMapping("/getEmbedding" )
    Result<Map<String, List<Float>>>  getOnnxRuntimeEmbeddingData(@RequestBody List<String> opinionList);
}
