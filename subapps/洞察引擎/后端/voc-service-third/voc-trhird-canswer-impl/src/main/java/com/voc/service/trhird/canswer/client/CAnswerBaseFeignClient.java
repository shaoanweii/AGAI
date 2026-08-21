package com.voc.service.trhird.canswer.client;

import com.voc.service.logs.annotation.FeignApiLog;
import com.voc.service.trhird.model.canswer.CAnswerApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        name = "canswer-base",
        contextId = "canswerBaseFeignClient",
        url = "${third.canswer.api.base.url:https://gaia.changan.com.cn/link/apig/out/changan/ai/v1}"
)
public interface CAnswerBaseFeignClient {

    @GetMapping("/accounts")
    @FeignApiLog
    CAnswerApiResponse getUserAccounts(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @RequestParam(value = "q", required = false) String username
    );

    @PostMapping("/workspaces/{workspace}/members")
    @FeignApiLog
    CAnswerApiResponse saveUsersToWorkspaces(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace,
            @RequestBody Map<String, Object> body
    );

    @GetMapping("/workspaces/{workspace}/members/{memberId}/enable_row_permission")
    @FeignApiLog
    CAnswerApiResponse getRowPermission(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace,
            @PathVariable("memberId") String memberId
    );

    @PutMapping("/workspaces/{workspace}/members/{memberId}/enable_row_permission")
    @FeignApiLog
    CAnswerApiResponse enableRowPermission(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace,
            @PathVariable("memberId") String memberId,
            @RequestBody Map<String, Object> body
    );

    @PutMapping("/workspaces/{workspace}/members/update_row_permissions")
    @FeignApiLog
    CAnswerApiResponse updateRowPermissions(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace,
            @RequestBody Map<String, Object> body
    );

    @PostMapping("/auth/auth_data")
    @FeignApiLog
    CAnswerApiResponse getAuthData(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @RequestBody Map<String, Object> body
    );

    @GetMapping("/workspaces/{workspace}/members")
    @FeignApiLog
    CAnswerApiResponse getMemberByUserCode(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace,
            @RequestParam("name") String userCode
    );

    @GetMapping("/workspaces/{workspace}/dimensions")
    @FeignApiLog
    CAnswerApiResponse getDimensions(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace
    );

    @GetMapping("/workspaces/{workspace}/models")
    @FeignApiLog
    CAnswerApiResponse getModels(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace
    );

    @GetMapping("/workspaces/{workspace}/measures")
    @FeignApiLog
    CAnswerApiResponse getMeasures(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace
    );

    @PostMapping("/workspaces/{workspace}/data/question")
    @FeignApiLog
    CAnswerApiResponse dataQuestion(
            @RequestHeader("apim-appcode-key") String apimAppcodeKey,
            @RequestHeader("User-Agent") String userAgent,
            @PathVariable("workspace") String workspace,
            @RequestBody Map<String, Object> body
    );
}
