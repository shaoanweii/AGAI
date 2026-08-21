package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysMetaDataModel implements Serializable {
    /**
     * 主键
     */
    String id;
    /**
     * 客户标识
     */
    String clientId;
    /**
     * 渠道标识
     */
    String channelId;

    String contentType;
    String workId;
    /**
     * 原始数据
     */
    Object data;
    /**
     * 链路标识
     */
    String tid;
    /**
     * 消息来源
     * api,mq,file等
     */
    String source;

    /**
     * 数据校验
     * 失败：-1 成功：1
     * 进行中：2 遗弃：3
     */
    @Builder.Default
    Integer validate = 1;
    /**
     * 接收时间
     */
    LocalDateTime createTime;
    /**
     * 操作人 推送数据的人或系统名称
     */
    String operator;
    /**
     * 校验信息 默认为成功
     * 若校验失败，则为失败信息
     */
    @Builder.Default
    String validateMessage = "成功";

}
