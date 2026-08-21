package com.voc.service.risk.api.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0
 * @date 2021/6/16 17:45
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {
    /**
     * 数据唯一标识
     */
    @Schema(description = "数据唯一标识")
    String id;
    /**
     * 可选参数：（工单/文本/对话）工单（400工单）文本（APP数据、评论、公域）对话（企微、dcc）
     */
    @Schema(description = "可选参数：（工单/文本/对话）工单（400工单）文本（APP数据、评论、公域）对话（企微、dcc）")
    String source;

    /**
     * 是否同步发送
     */
    @Schema(description = "是否同步发送")
    Integer sync;

    /**
     * 消息类型(短信、钉钉、微信等，可扩展)
     */
    @Schema(description = "消息类型(短信、钉钉、微信等，可扩展,api,mq)")
    String type;
    /**
     * 消息体
     */
    @Schema(description = "消息体")
    Object data;

    /**
     * 链路id，可为空
     */
    @Schema(description = "链路id")
    String requestId;

    /**
     * token令牌，可为空，若某个操作必须token，则需要做单独校验
     */
    @Schema(description = "token令牌，可为空，若某个操作必须token，则需要做单独校验")
    String token;
    /**
     * 扩展字段，若需其他字段，则放入扩展字段中
     */
    @Builder.Default
    @Schema(description = "扩展字段，若需其他字段，则放入扩展字段中")
    Set<MessageExt> ext = new HashSet<>();
}
