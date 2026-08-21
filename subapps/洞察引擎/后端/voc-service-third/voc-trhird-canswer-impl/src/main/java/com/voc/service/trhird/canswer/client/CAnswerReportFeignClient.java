package com.voc.service.trhird.canswer.client;

import com.alibaba.fastjson.JSONObject;
import com.voc.service.logs.annotation.FeignApiLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "canswer-report",
        contextId = "canswerReportFeignClient",
        url = "${third.canswer.api.base.report.url:http://172.16.76.200:8802/api/v1}"
)
public interface CAnswerReportFeignClient {

    @PostMapping("/summarize")
    @FeignApiLog
    JSONObject summarize(
            @RequestHeader("api-key") String apiKey,
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody JSONObject body
    );

    @PostMapping("/extract")
    @FeignApiLog
    JSONObject reportExtract(
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody JSONObject body
    );

    @PostMapping("/match")
    @FeignApiLog
    JSONObject reportMatch(
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody JSONObject body
    );

    @PostMapping("/generate_params")
    @FeignApiLog
    JSONObject generateParams(
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody JSONObject body
    );
}

