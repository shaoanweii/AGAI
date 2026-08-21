package com.voc.service.trhird.canswer.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.trhird.api.CAnswerApi;
import com.voc.service.trhird.canswer.client.CAnswerBaseFeignClient;
import com.voc.service.trhird.canswer.client.CAnswerLlmFeignClient;
import com.voc.service.trhird.canswer.client.CAnswerOpenFeignClient;
import com.voc.service.trhird.canswer.client.CAnswerReportFeignClient;
import com.voc.service.trhird.canswer.config.CAnswerSSLIgnoreConfig;
import com.voc.service.trhird.model.canswer.CAnswerApiResponse;
import com.voc.service.trhird.model.canswer.CAnswerRowPermissionRequest;
import com.voc.service.trhird.model.canswer.CAnswerUpdateRowPermissionsRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/10/13
 */
@Service
@Slf4j
public class CAnswerApiImpl implements CAnswerApi {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    @Value("${third.canswer.api.apim.appcode.key:68d9009e44299e24d8f7f2d6}")
    private String apimAppcodeKey;

    @Value("${third.canswer.api.base.url:https://gaia.changan.com.cn/link/apig/out/changan/ai/v1}")
    private String baseUrl;

    @Value("${third.canswer.api.llm.key:oiqhyerfila;shdf08y5082hnasdo;h}")
    private String apiLLMKey;


    @Value("${third.canswer.api.base.llm.url:http://172.16.76.200:8801/api/v1}")
    private String baseLLMUrl;

    @Value("${third.canswer.api.base.report.url:http://172.16.76.200:8802/api/v1}")
    private String baseReportUrl;

    @Value("${third.canswer.api.workspace:86}")
    private String workspace;

    @Value("${third.canswer.api.data.effect:allow}")
    private String dataEffect;

    @Value("${third.canswer.api.brand.effect:allow}")
    private String brandEffect;

    @Value("${third.canswer.api.data.resource:d205}")
    private String dataResource;

    @Value("${third.canswer.api.brand.resource:d206}")
    private String brandResource;

    @Value("${third.canswer.api.data.operator:equal}")
    private String dataOperator;

    @Value("${third.canswer.api.brand.operator:equal}")
    private String brandOperator;

    @Autowired
    private CAnswerBaseFeignClient cAnswerBaseFeignClient;

    @Autowired
    private CAnswerOpenFeignClient cAnswerOpenFeignClient;

    @Autowired
    private CAnswerLlmFeignClient cAnswerLlmFeignClient;

    @Autowired
    private CAnswerReportFeignClient cAnswerReportFeignClient;

    @Override
    public CAnswerApiResponse saveUsersToWorkspaces(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return null;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("uids", userIds);

        log.info("保存用户到工作区: userIds={}", userIds);
        CAnswerApiResponse response = cAnswerBaseFeignClient.saveUsersToWorkspaces(
                apimAppcodeKey,
                DEFAULT_USER_AGENT,
                workspace,
                body
        );
        log.info("============>保存用户到工作区返回值为:{}", response);
        return response;
    }

    @Override
    public CAnswerApiResponse getUserAccounts(String username) {
        log.info("获取工作项请求: username={}", username);
        String q = StringUtils.isNotEmpty(username) ? username : null;
        return cAnswerBaseFeignClient.getUserAccounts(apimAppcodeKey, DEFAULT_USER_AGENT, q);
    }

    @Override
    public CAnswerApiResponse getRowPermission(String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return null;
        }
        return cAnswerBaseFeignClient.getRowPermission(apimAppcodeKey, DEFAULT_USER_AGENT, workspace, memberId);
    }

    @Override
    public CAnswerApiResponse enableRowPermission(String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return null;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("enableRowPermission", true);
        CAnswerApiResponse response = cAnswerBaseFeignClient.enableRowPermission(
                apimAppcodeKey,
                DEFAULT_USER_AGENT,
                workspace,
                memberId,
                body
        );
        log.info("========>开启用户行权限的返回值:{}", response);
        return response;
    }

    @Override
    public CAnswerApiResponse updateRowPermissions(List<CAnswerUpdateRowPermissionsRequest> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("memberRowPermissions", list);

        log.info("获取工作项请求: list={}", list);

        CAnswerApiResponse response = cAnswerBaseFeignClient.updateRowPermissions(
                apimAppcodeKey,
                DEFAULT_USER_AGENT,
                workspace,
                body
        );

        log.info("========>批量编辑行权限返回值信息为:{}", response);
        return response;
    }

    @Override
    public void syncUserAndPermission(JSONObject params) {
        log.info("=========>同步用户以及编辑权限信息时的参数信息为:{}", params);
        // 添加空值检查
        if (params == null) {
            log.warn("参数不能为空");
            return;
        }

        // 安全获取数组并设置默认值
        List<String> userCodes = new ArrayList<>();
        List<String> brandCodes = new ArrayList<>();
        List<String> dataCodes = new ArrayList<>();

        if (params.getJSONArray("userCodes") != null) {
            userCodes = params.getJSONArray("userCodes").toJavaList(String.class);
        }
        if (params.getJSONArray("brandCodes") != null) {
            brandCodes = params.getJSONArray("brandCodes").toJavaList(String.class);
        }
        if (params.getJSONArray("dataCodes") != null) {
            dataCodes = params.getJSONArray("dataCodes").toJavaList(String.class);
        }
        if (CollectionUtils.isEmpty(userCodes)) {
            return;
        }
        // 第一步，通过用户Code获取UUID的list
        List<String> uuids = new ArrayList<>();
        for (String userCode : userCodes) {
            CAnswerApiResponse userAccounts = getUserAccounts(userCode);
            Object data = userAccounts.getData();
            if (userAccounts.getErrcode() != 200 || !userAccounts.getErrmsg().equals("success")
                    || ObjectUtils.isEmpty(data)) {
                continue;
            }
            JSONArray dataArray = (JSONArray) JSONArray.toJSON(data);
            uuids.add(dataArray.getJSONObject(0).getString("uid"));
        }
        if (CollectionUtils.isEmpty(uuids)) {
            return;
        }
        // 第二步，将用户添加到工作区，并获取memberIds信息
        List<String> memberIds = new ArrayList<>();
        CAnswerApiResponse saveUsersToWorkspaces = saveUsersToWorkspaces(uuids);
        Object data = saveUsersToWorkspaces.getData();
        if (saveUsersToWorkspaces.getErrcode() != 200 || !saveUsersToWorkspaces.getErrmsg().equals("success")
                || ObjectUtils.isEmpty(data)) {
            log.info("===========>用户添加工作区返回值信息为:{}", saveUsersToWorkspaces);
            return;
        }
        JSONArray saveUsersToWorkspacesReturnArray = (JSONArray) JSONArray.toJSON(data);
        if (CollectionUtils.isEmpty(saveUsersToWorkspacesReturnArray)) {
            return;
        }
        for (int i = 0; i < saveUsersToWorkspacesReturnArray.size(); i++) {
            memberIds.add(saveUsersToWorkspacesReturnArray.getJSONObject(i).getString("uid"));
        }
        // 第三步，开启用户行权限
        if (!CollectionUtils.isEmpty(memberIds)) {
            for (String memberId : memberIds) {
                // 查询是否开启行权限
                CAnswerApiResponse rowPermissionReturn = getRowPermission(memberId);
                Object rowPermissionReturnData = rowPermissionReturn.getData();
                if (rowPermissionReturn.getErrcode() == 200
                        && rowPermissionReturn.getErrmsg().equals("success")
                        && !ObjectUtils.isEmpty(rowPermissionReturnData)) {
                    JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(rowPermissionReturnData));
                    Boolean enableRowPermission = jsonObject.getBoolean("enableRowPermission");
                    if (!ObjectUtils.isEmpty(enableRowPermission) || !enableRowPermission) {
                        enableRowPermission(memberId);
                    }
                }
            }
        }
        // 第四步，通过数据Codes以及品牌Codes组建编辑行权限参数
        List<CAnswerUpdateRowPermissionsRequest> list = new ArrayList<>(memberIds.size());
        List<CAnswerRowPermissionRequest> rowPermission = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dataCodes)) {
            CAnswerRowPermissionRequest dataParams = CAnswerRowPermissionRequest.builder()
                    .effect(dataEffect).operator(dataOperator).resource(dataResource).values(dataCodes).build();
            rowPermission.add(dataParams);
        }
        if (!CollectionUtils.isEmpty(brandCodes)) {
            CAnswerRowPermissionRequest brandParams = CAnswerRowPermissionRequest.builder()
                    .effect(brandEffect).operator(brandOperator).resource(brandResource).values(brandCodes).build();
            rowPermission.add(brandParams);
        }
        for (String memberId : memberIds) {
            list.add(CAnswerUpdateRowPermissionsRequest.builder()
                    .rowPermission(rowPermission).rowPermissionMode(1).memberUid(memberId).build());
        }
        // 第五步，通过拿到的参数信息请求批量编辑行权限接口
        updateRowPermissions(list);
    }

    @Override
    public CAnswerApiResponse getAuthData(String userCode) {
        if (StringUtils.isEmpty(userCode)) {
            return null;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("identity", userCode);
        body.put("project_id", workspace);
        log.info("=======>获取用户免登录信息的参数为:{}", body);
        return cAnswerBaseFeignClient.getAuthData(apimAppcodeKey, DEFAULT_USER_AGENT, body);
    }

    @Override
    public CAnswerApiResponse getMemberByUserCode(String userCode) {
        if (StringUtils.isEmpty(userCode)) {
            return null;
        }
        return cAnswerBaseFeignClient.getMemberByUserCode(apimAppcodeKey, DEFAULT_USER_AGENT, workspace, userCode);
    }

    @Override
    public CAnswerApiResponse getDimensions() {
        return cAnswerBaseFeignClient.getDimensions(apimAppcodeKey, DEFAULT_USER_AGENT, workspace);
    }

    @Override
    public CAnswerApiResponse getModels() {
        return cAnswerBaseFeignClient.getModels(apimAppcodeKey, DEFAULT_USER_AGENT, workspace);
    }

    @Override
    public CAnswerApiResponse getMeasures() {
        return cAnswerBaseFeignClient.getMeasures(apimAppcodeKey, DEFAULT_USER_AGENT, workspace);
    }

    @Override
    public CAnswerApiResponse dataQuestion(JSONObject params) {
        log.info("=======>问题查数的参数信息为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerBaseFeignClient.dataQuestion(
                apimAppcodeKey,
                DEFAULT_USER_AGENT,
                workspace,
                params
        );
    }

    @Override
    public CAnswerApiResponse dataCard(JSONObject params) {
        log.info("=======>问题查数的参数信息为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerOpenFeignClient.dataCard(
                apimAppcodeKey,
                DEFAULT_USER_AGENT,
                workspace,
                params
        );
    }

    @Override
    public JSONObject queryFix(JSONObject params) {
        log.info("=======>问题补全的参数为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerLlmFeignClient.queryFix(apiLLMKey, DEFAULT_USER_AGENT, params);
    }

    @Override
    public CompletableFuture<String> aiSummaryOrSuggestion(JSONObject params, SseEmitter emitter,
                                                           String suffixUrl, String type, String messageId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        // 用于收集流式数据
        StringBuilder contentBuilder = new StringBuilder();

        try {
            String url = baseLLMUrl + suffixUrl;

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiLLMKey);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // 确保stream参数为true
            params.put("stream", true);

            HttpEntity<JSONObject> entity = new HttpEntity<>(params, headers);

            log.info("=======>AI摘要请求参数为:{}", params);

            // 创建异步任务处理SSE流
            CompletableFuture.runAsync(() -> {
                Thread currentThread = Thread.currentThread();
                try {
                    RestTemplate restTemplate = CAnswerSSLIgnoreConfig.createIgnoreSSLRestTemplate();

                    // 使用 InputStream 处理流式响应
                    ResponseEntity<String> response = restTemplate.execute(
                            url,
                            HttpMethod.POST,
                            requestCallback -> {
                                // 检查线程中断状态
                                if (currentThread.isInterrupted()) {
                                    return;
                                }

                                requestCallback.getHeaders().addAll(headers);
                                if (entity.getBody() != null) {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    String body = objectMapper.writeValueAsString(entity.getBody());
                                    requestCallback.getBody().write(body.getBytes(StandardCharsets.UTF_8));
                                }
                            },
                            responseExtractor -> {
                                InputStream body = responseExtractor.getBody();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8));
                                String line;
                                try {
                                    // 实时读取并推送每一行数据
                                    while ((line = reader.readLine()) != null && !currentThread.isInterrupted()) {
                                        if (!line.isEmpty()) {
                                            // 解析原始数据并添加type信息
                                            if (line.startsWith("data: ")) {
                                                String jsonData = line.substring(6); // 移除 "data: " 前缀
                                                try {
                                                    if (jsonData.startsWith("{")) {
                                                        // 解析JSON数据
                                                        JSONObject jsonObject = JSON.parseObject(jsonData);
                                                        // 提取content字段内容并累加
                                                        if (jsonObject.containsKey("choices") &&
                                                                jsonObject.getJSONArray("choices").size() > 0) {
                                                            JSONObject choice = jsonObject.getJSONArray("choices").getJSONObject(0);
                                                            if (choice.containsKey("delta") &&
                                                                    choice.getJSONObject("delta").containsKey("content")) {
                                                                String content = choice.getJSONObject("delta").getString("content");
                                                                contentBuilder.append(content);
                                                            }
                                                        }
                                                        // 添加type信息
                                                        jsonObject.put("type", type);
                                                        jsonObject.put("id", messageId);
                                                        // 重新构造带type的SSE数据行
                                                        String newDataLine = jsonObject.toJSONString();
                                                        emitter.send(SseEmitter.event().name("message").data(newDataLine));
                                                    }
                                                } catch (Exception e) {
                                                    // 如果解析失败，仍然发送原始数据
                                                    future.complete(contentBuilder.toString());
                                                }
                                            }
                                        }
                                    }

                                    // 检查是否因中断而退出循环
                                    if (currentThread.isInterrupted()) {
                                        handleInterruption(emitter, contentBuilder.toString(), "线程被中断");
                                    }
                                } catch (IOException e) {
                                    log.error("读取流数据异常", e);
                                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                                }
                                return null;
                            }
                    );

                    // 正常完成
                    future.complete(contentBuilder.toString());
                } catch (Exception e) {
                    log.error("AI摘要调用异常", e);
                    try {
                        emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                    } catch (IOException ioException) {
                        log.error("SSE发送错误失败", ioException);
                    }
                    // 即使异常也返回已收集的内容
                    future.complete(contentBuilder.toString());
                }
            }, ServiceContextHolder.getExecutor());
        } catch (Exception e) {
            log.error("创建SSE emitter失败", e);
            // 返回已收集的内容（如果有）
            future.complete(contentBuilder.toString());
        }

        return future;
    }

    @Override
    public CompletableFuture<String> aiQueryFix(JSONObject params, SseEmitter emitter, String messageId,
                                                String userMessageId, String sessionId, long beginTime, String url) {
        CompletableFuture<String> future = new CompletableFuture<>();
        // 用于收集流式数据
        StringBuilder contentBuilder = new StringBuilder();

        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiLLMKey);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // 确保stream参数为true
            params.put("stream", true);

            HttpEntity<JSONObject> entity = new HttpEntity<>(params, headers);

            log.info("=======>AI摘要请求参数为:{}", params);

            // 创建异步任务处理SSE流
            CompletableFuture.runAsync(() -> {
                Thread currentThread = Thread.currentThread();
                try {
                    RestTemplate restTemplate = CAnswerSSLIgnoreConfig.createIgnoreSSLRestTemplate();

                    // 使用 InputStream 处理流式响应
                    restTemplate.execute(
                            url,
                            HttpMethod.POST,
                            requestCallback -> {

                                requestCallback.getHeaders().addAll(headers);
                                if (entity.getBody() != null) {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    String body = objectMapper.writeValueAsString(entity.getBody());
                                    requestCallback.getBody().write(body.getBytes(StandardCharsets.UTF_8));
                                }
                            },
                            responseExtractor -> {
                                InputStream body = responseExtractor.getBody();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8));
                                String line;
                                try {
                                    // 实时读取并推送每一行数据
                                    while ((line = reader.readLine()) != null && !currentThread.isInterrupted()) {
                                        if (!line.isEmpty()) {
                                            // 解析原始数据并添加type信息
                                            if (line.startsWith("data: ")) {
                                                String jsonData = line.substring(6); // 移除 "data: " 前缀
                                                try {
                                                    if (jsonData.startsWith("{")) {
                                                        // 解析JSON数据
                                                        JSONObject jsonObject = JSON.parseObject(jsonData);
                                                        // 提取content字段内容并累加
                                                        if (jsonObject.containsKey("choices") &&
                                                                jsonObject.getJSONArray("choices").size() > 0) {
                                                            JSONObject choice = jsonObject.getJSONArray("choices").getJSONObject(0);
                                                            if (choice.containsKey("delta")) {
                                                                JSONObject delta = choice.getJSONObject("delta");
                                                                if (delta.containsKey("reasoning_content")) {
                                                                    delta.put("content", delta.get("reasoning_content"));
                                                                }
                                                                choice.put("delta", delta);
                                                                String content = choice.getJSONObject("delta").getString("content");
                                                                contentBuilder.append(content);
                                                            }
                                                        }
                                                        // 添加type信息
                                                        jsonObject.put("type", "aiAnalysis");
                                                        jsonObject.put("id", messageId);
                                                        jsonObject.put("userId", userMessageId);
                                                        jsonObject.put("sessionId", sessionId);
                                                        // 重新构造带type的SSE数据行
                                                        String newDataLine = jsonObject.toJSONString();
                                                        emitter.send(SseEmitter.event().name("message").data(newDataLine));
                                                    }
                                                } catch (Exception e) {
                                                    future.complete(contentBuilder.toString());
                                                }
                                            }
                                        }
                                    }

                                    // 检查是否因中断而退出循环
                                    if (currentThread.isInterrupted()) {
                                        handleInterruption(emitter, contentBuilder.toString(), "线程被中断");
                                    }
                                } catch (IOException e) {
                                    log.error("读取流数据异常", e);
                                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                                }
                                return null;
                            }
                    );

                    // 正常完成
                    future.complete(contentBuilder.toString());
                } catch (Exception e) {
                    log.error("AI摘要调用异常", e);
                    try {
                        emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                    } catch (IOException ioException) {
                        log.error("SSE发送错误失败", ioException);
                    }
                    // 即使异常也返回已收集的内容
                    future.complete(contentBuilder.toString());
                }
            }, ServiceContextHolder.getExecutor());
        } catch (Exception e) {
            log.error("创建SSE emitter失败", e);
            // 返回已收集的内容（如果有）
            future.complete(contentBuilder.toString());
        }

        return future;
    }

    @Override
    public JSONObject summarize(JSONObject params) {
        log.info("=======>AI总结的参数为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerReportFeignClient.summarize(apiLLMKey, DEFAULT_USER_AGENT, params);
    }

    @Override
    public JSONObject sceneType(JSONObject params) {
        log.info("=======>分析查询参数信息为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerLlmFeignClient.sceneType(apiLLMKey, DEFAULT_USER_AGENT, params);
    }

    @Override
    public JSONObject reportExtract(JSONObject params) {
        log.info("=======>报告-语义校验的参数为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerReportFeignClient.reportExtract(DEFAULT_USER_AGENT, params);
    }

    @Override
    public JSONObject reportMatch(JSONObject params) {
        log.info("=======>报告-匹配模板的参数为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerReportFeignClient.reportMatch(DEFAULT_USER_AGENT, params);
    }

    @Override
    public JSONObject generateParams(JSONObject params) {
        log.info("=======>生成报告的参数为:{}", params);
        if (params == null) {
            return null;
        }
        return cAnswerReportFeignClient.generateParams(DEFAULT_USER_AGENT, params);
    }

    @Override
    public JSONObject chatBiHealthCheck() {
        return healthCheck(baseLLMUrl);
    }

    @Override
    public JSONObject reportHealthCheck() {
        return healthCheck(baseReportUrl);
    }

    private void handleInterruption(SseEmitter emitter, String collectedContent, String reason) {
        try {
            JSONObject interruptionMessage = new JSONObject();
            interruptionMessage.put("type", "interruption");
            interruptionMessage.put("reason", reason);
            interruptionMessage.put("partialContent", collectedContent);

            emitter.send(SseEmitter.event().name("message").data(interruptionMessage.toJSONString()));
            emitter.complete();
        } catch (IOException e) {
            log.error("发送中断消息失败", e);
        }
    }

    private JSONObject healthCheck(String originUrl) {
        // 构建URL和查询参数
        String url;
        try {
            URL parsedUrl = new URL(originUrl);
            url = parsedUrl.getProtocol() + "://" + parsedUrl.getHost() + ":" + parsedUrl.getPort() + "/health";
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        JSONObject body = new JSONObject();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("apim-appcode-key", apimAppcodeKey);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);

        // 发送PUT请求
        RestTemplate restTemplate = CAnswerSSLIgnoreConfig.createIgnoreSSLRestTemplate();
        ResponseEntity<JSONObject> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                JSONObject.class
        );
        return response.getBody();
    }
}
