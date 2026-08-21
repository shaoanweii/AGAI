package com.voc.service.trhird.canswer.client;

import com.voc.service.logs.annotation.FeignApiLog;
import com.voc.service.trhird.model.canswer.CAnswerApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
        name = "canswer-open",
        contextId = "canswerOpenFeignClient",
        url = "${third.canswer.api.base.open.url:https://gaia.changan.com.cn/link/apig/out/changan/openapi/ai/v1}"
)
public interface CAnswerOpenFeignClient {

    @PostMapping("/workspaces/{workspace}/data/card")
    @FeignApiLog
    CAnswerApiResponse dataCard(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace,
            @RequestBody Map<String, Object> body
    );
}

