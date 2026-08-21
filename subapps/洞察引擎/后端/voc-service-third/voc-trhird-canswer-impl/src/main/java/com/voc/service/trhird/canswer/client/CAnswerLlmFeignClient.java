package com.voc.service.trhird.canswer.client;

import com.alibaba.fastjson.JSONObject;
import com.voc.service.logs.annotation.FeignApiLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "canswer-llm",
        contextId = "canswerLlmFeignClient",
        url = "${third.canswer.api.base.llm.url:http://172.16.76.200:8801/api/v1}"
)
public interface CAnswerLlmFeignClient {

    @PostMapping("/intelligentqa/fix_query")
    @FeignApiLog
    JSONObject queryFix(
            @RequestHeader("api-key") String apiKey,
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody JSONObject body
    );

    @PostMapping("/intelligentqa/scene_type")
    @FeignApiLog
    JSONObject sceneType(
            @RequestHeader("api-key") String apiKey,
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody JSONObject body
    );
}

