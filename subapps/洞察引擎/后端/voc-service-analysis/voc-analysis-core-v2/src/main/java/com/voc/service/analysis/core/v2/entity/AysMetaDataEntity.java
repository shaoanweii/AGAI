package com.voc.service.analysis.core.v2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName(value = "ays_meta_data")
public class AysMetaDataEntity implements Serializable {
    String id;
    /**
     * 接收处理标识
     */
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
     * 接收时间
     */
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();
    /**
     * 操作人 推送数据的人或系统名称
     */
    String operator;

    Integer modelType;

    Object extFields;

    Object bizExtAttrs;

    Object bizExtAttrs2;

    Object bizExtAttrs3;

    String oneId;

    @Builder.Default
    String done = "0";

    /**
     * 重试次数
     */
    /*@Builder.Default
    String retry = "0";*/

}
