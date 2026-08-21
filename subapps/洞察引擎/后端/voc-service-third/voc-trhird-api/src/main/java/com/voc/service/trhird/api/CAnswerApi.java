package com.voc.service.trhird.api;

import com.alibaba.fastjson.JSONObject;
import com.voc.service.trhird.model.canswer.CAnswerApiResponse;
import com.voc.service.trhird.model.canswer.CAnswerUpdateRowPermissionsRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/11/21
 */
public interface CAnswerApi {

    /**
     * 保存用户到工作区
     *
     * @param userIds 用户ID信息
     * @return
     */
    CAnswerApiResponse saveUsersToWorkspaces(List<String> userIds);

    /**
     * 获取用户的分页列表信息
     *
     * @return
     */
    CAnswerApiResponse getUserAccounts(String username);

    /**
     * 查询成员的行权限
     *
     * @param memberId
     * @return
     */
    CAnswerApiResponse getRowPermission(String memberId);

    /**
     * 开启该用户的行权限
     *
     * @param memberId
     * @return
     */
    CAnswerApiResponse enableRowPermission(String memberId);

    /**
     * 批量编辑行权限
     *
     * @param list
     * @return
     */
    CAnswerApiResponse updateRowPermissions(List<CAnswerUpdateRowPermissionsRequest> list);

    /**
     * 用户绑定角色或者角色修改时添加用户到工作区以及绑定权限信息操作
     *
     * @param params
     */
    void syncUserAndPermission(JSONObject params);

    /**
     * 根据用户编码获取CAnswer免登录信息
     *
     * @param userCode
     * @return
     */
    CAnswerApiResponse getAuthData(String userCode);

    /**
     * 根据用户编码查询所在工作区的memberId信息
     *
     * @param userCode
     * @return
     */
    CAnswerApiResponse getMemberByUserCode(String userCode);

    /**
     * 查询维度列表信息
     *
     * @return
     */
    CAnswerApiResponse getDimensions();

    /**
     * 查询模型信息
     *
     * @return
     */
    CAnswerApiResponse getModels();

    /**
     * 查询指标信息
     *
     * @return
     */
    CAnswerApiResponse getMeasures();

    /**
     * 问数查询结果
     *
     * @param params
     * @return
     */
    CAnswerApiResponse dataQuestion(JSONObject params);

    /**
     * 回答卡片
     *
     * @param params
     * @return
     */
    CAnswerApiResponse dataCard(JSONObject params);

    /**
     * 问题补全接口
     *
     * @param params
     * @return
     */
    JSONObject queryFix(JSONObject params);

    /**
     * 大模型总结
     *
     * @param params
     * @return
     */
    CompletableFuture<String> aiSummaryOrSuggestion(JSONObject params, SseEmitter emitter,
                                                    String suffixUrl, String type, String messageId);

    /**
     * think模式返回信息
     *
     * @param params
     * @param emitter
     * @param messageId
     * @param userMessageId
     * @param sessionId
     * @param beginTime
     * @return
     */
    CompletableFuture<String> aiQueryFix(JSONObject params, SseEmitter emitter, String messageId,
                                         String userMessageId, String sessionId, long beginTime, String url);

    JSONObject summarize(JSONObject params);

    /**
     * 分析类型
     *
     * @param params
     * @return
     */
    JSONObject sceneType(JSONObject params);

    /**
     * 报告--语义校验
     *
     * @param params
     * @return
     */
    JSONObject reportExtract(JSONObject params);

    /**
     * 报告-匹配
     *
     * @param params
     * @return
     */
    JSONObject reportMatch(JSONObject params);

    /**
     * 根据问题生成参数
     *
     * @param params
     * @return
     */
    JSONObject generateParams(JSONObject params);

    JSONObject chatBiHealthCheck();

    JSONObject reportHealthCheck();
}
