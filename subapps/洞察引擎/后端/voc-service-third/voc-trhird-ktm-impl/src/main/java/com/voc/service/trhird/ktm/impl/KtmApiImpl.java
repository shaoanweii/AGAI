package com.voc.service.trhird.ktm.impl;


import com.voc.service.trhird.api.KtmApi;
import com.voc.service.trhird.ktm.config.SSLIgnoreConfig;
import com.voc.service.trhird.model.ktm.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/10/13
 */
@Service
@Slf4j
public class KtmApiImpl implements KtmApi {

    @Value("${third.ktm.api.apim.appcode.key:68c91b5ff1993e2c605f0117}")
    private String apimAppcodeKey;

    @Value("${third.ktm.api.base.url.login:https://apitest.changan.com.cn:30598}")
    private String baseUrlLogin;

    @Value("${third.ktm.api.base.url:https://cmpuat.changan.com}")
    private String baseUrl;

    @Value("${third.ktm.api.clientIp:-1}")
    private String clientIp;

    @Value("${third.ktm.api.utcOffset:-1}")
    private String utcOffset;

    @Override
    public KtmApiResponse loginAndGetToken(KtmLoginRequest loginRequest) {
        try {
            // 构建URL参数
            String url = baseUrlLogin + "/changan/ichangan/rescenter/rest/resRestApi/v2/userLogin" +
                    "?loginId={loginId}" +
                    "&password={password}";
            if (StringUtils.isNotEmpty(clientIp) && !clientIp.equals("-1")) {
                url = url + "&clientIp=" + clientIp;
            }
            if (StringUtils.isNotEmpty(utcOffset) && !utcOffset.equals("-1")) {
                url = url + "&UTC_ZERO_OFFSET=" + utcOffset;
            }

            // 参数Map
            Map<String, String> params = new HashMap<>();
            params.put("loginId", loginRequest.getLoginId());
            params.put("password", loginRequest.getPassword());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("apim-appcode-key", apimAppcodeKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("调用长安登录接口: {}", url);

            // 发送POST请求
            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    KtmApiResponse.class,
                    params
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("登录接口客户端错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("服务端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("登录接口服务端错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用登录接口异常", e);
            throw new RuntimeException("登录接口调用异常: " + e.getMessage());
        }
    }

    @Override
    public KtmApiResponse startProc(KtmStartProcRequest startProcRequest, String token) {
        try {
            String url = baseUrl + "/bpm-rest/bpm/rest/v2/BPMExecuteService/createAndStartProcessInstance";

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("token", token);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<KtmStartProcRequest> entity = new HttpEntity<>(startProcRequest, headers);

            log.info("创建流程实例请求: procId={}, procInstName={}",
                    startProcRequest.getProcId(), startProcRequest.getProcInstName());

            // 发送POST请求
            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    KtmApiResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("创建流程实例客户端错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("服务端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("创建流程实例服务端错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("创建流程实例异常", e);
            throw new RuntimeException("创建流程实例调用异常: " + e.getMessage());
        }
    }

    @Override
    public KtmApiResponse getWorkItemByProcInstId(String procInstId, String ownerId, String state, String token) {
        try {
            // 构建URL和查询参数
            String url = baseUrl + "/bpm-rest/bpm/rest/v2/BPMExecuteService/getWorkItemList" +
                    "?procInstId={procInstId}" +
                    "&ownerId={ownerId}" +
                    "&state={state}";

            Map<String, Object> params = new HashMap<>();
            params.put("procInstId", procInstId);
            params.put("ownerId", ownerId);
            params.put("state", state);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("token", token);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("获取工作项请求: procInstId={}, ownerId={}, state={}",
                    procInstId, ownerId, state);

            // 发送GET请求
            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    KtmApiResponse.class,
                    params
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取工作项客户端错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("服务端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取工作项服务端错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("获取工作项异常", e);
            throw new RuntimeException("获取工作项调用异常: " + e.getMessage());
        }
    }

    @Override
    public KtmApiResponse getFormInfo(String procInstId, String workItemId, String token) {
        try {
            // 构建URL和查询参数
            String url = baseUrl + "/BPMApp/ws/rest/reportProcessing/getFormInfo" +
                    "?workItemId={workItemId}" +
                    "&procInstId={procInstId}";

            Map<String, Object> params = new HashMap<>();
            params.put("workItemId", workItemId);
            params.put("procInstId", procInstId);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            // token认证
            headers.set("IdentityToken", token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("获取表单信息请求: workItemId={}, procInstId={}",
                    workItemId, procInstId);

            // 发送GET请求
            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    KtmApiResponse.class,
                    params
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取表单信息客户端错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("服务端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取表单信息服务端错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("获取表单信息异常", e);
            throw new RuntimeException("获取表单信息调用异常: " + e.getMessage());
        }
    }

    @Override
    public KtmApiResponse saveMainInfo(KtmSaveMainInfoRequest saveMainInfoRequest, String token) {
        try {
            String url = baseUrl + "/BPMApp/ws/rest/reportProcessing/saveMainInfo";

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("IdentityToken", token);

            HttpEntity<KtmSaveMainInfoRequest> entity = new HttpEntity<>(saveMainInfoRequest, headers);

            log.debug("完整请求数据: {}", saveMainInfoRequest);

            // 发送POST请求
            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    KtmApiResponse.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("保存主要信息客户端错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("服务端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("保存主要信息服务端错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("保存主要信息异常", e);
            throw new RuntimeException("保存主要信息调用异常: " + e.getMessage());
        }
    }

    @Override
    public KtmApiResponse uploadFile(String workItemId, String fileName, MultipartFile file, String token) {
        try {
            String url = baseUrl + "/BPMApp/ws/rest/reportProcessing/uploadFile" +
                    "?workItemId={workItemId}&fileName={fileName}";

            // 参数Map
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("workItemId", workItemId);
            uriVariables.put("fileName", fileName);

            // 检查文件
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            // 创建多部分请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("IdentityToken", token);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 添加文件部分
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // 发送POST请求
            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    KtmApiResponse.class,
                    uriVariables
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("文件上传客户端错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("服务端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("文件上传服务端错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new RuntimeException("文件上传调用异常: " + e.getMessage());
        }
    }

    @Override
    public KtmApiResponse completeWorkItemAndGetNext(String workItemId, String token) {
        try {
            String url = baseUrl + "/bpm-rest/bpm/rest/v2/BPMExecuteService/completeWorkItemAndGetNext" +
                    "?workItemId={workItemId}";

            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("workItemId", workItemId);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("token", token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送POST请求
            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    KtmApiResponse.class,
                    uriVariables
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("完成工作项客户端错误: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("服务端错误: 状态码={}, 响应={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("完成工作项服务端错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("完成工作项异常", e);
            throw new RuntimeException("完成工作项调用异常: " + e.getMessage());
        }
    }

    @Override
    public KtmApiResponse uploadFileWithInputStream(String workItemId, String fileName, String token, InputStream inputStream) {
        try {
            String url = baseUrl + "/BPMApp/ws/rest/reportProcessing/uploadFile" +
                    "?workItemId={workItemId}&fileName={fileName}";

            // 参数Map
            Map<String, String> uriVariables = new HashMap<>();
            uriVariables.put("workItemId", workItemId);
            uriVariables.put("fileName", fileName);

            RestTemplate restTemplate = SSLIgnoreConfig.createIgnoreSSLRestTemplate();

            // 设置请求头 - 使用 application/octet-stream
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("IdentityToken", token);

            // 直接读取 InputStream 为字节数组
            byte[] fileBytes = inputStream.readAllBytes();
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, headers);

            // 发送请求
            ResponseEntity<KtmApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    KtmApiResponse.class,
                    uriVariables
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new RuntimeException("文件上传调用异常: " + e.getMessage());
        }
    }
}
