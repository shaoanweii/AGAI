package com.voc.service.trhird.api;

import com.voc.service.trhird.model.ktm.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/10/13
 */
public interface KtmApi {

    /**
     * ktm登录接口，获取token信息
     * @param loginRequest 登录传参对象
     * @return
     */
    KtmApiResponse loginAndGetToken(KtmLoginRequest loginRequest);

    /**
     * ktm创建并启动流程
     * @param startProcRequest
     * @return
     */
    KtmApiResponse startProc(KtmStartProcRequest startProcRequest, String token);

    /**
     * 根据流程实例ID获取代办副本信息
     * @param procInstId 流程实例ID
     * @param ownerId 用户 //todo 用户这个ID如何获取
     * @param state 状态
     * @param token 认证信息
     * @return
     */
    KtmApiResponse getWorkItemByProcInstId(String procInstId, String ownerId, String state, String token);

    /**
     * 获取流程
     * @param procInstId
     * @param workItemId
     * @return
     */
    KtmApiResponse getFormInfo(String procInstId, String workItemId, String token);

    /**
     * 保存呈报件
     * @param saveMainInfoRequest
     * @param token
     * @return
     */
    KtmApiResponse saveMainInfo(KtmSaveMainInfoRequest saveMainInfoRequest, String token);

    /**
     * 上传附件
     * @param workItemId
     * @param fileName
     * @param file
     * @return
     */
    KtmApiResponse uploadFile(String workItemId, String fileName, MultipartFile file, String token);

    /**
     * 下一步
     * @param workItemId
     * @param token
     * @return
     */
    KtmApiResponse completeWorkItemAndGetNext(String workItemId, String token);

    /**
     * 通过文件输入流上传文件
     * @param workItemId
     * @param fileName
     * @param token
     * @param inputStream
     * @return
     */
    KtmApiResponse uploadFileWithInputStream(String workItemId, String fileName, String token, InputStream inputStream);
}
