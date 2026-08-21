package com.voc.service.insights.engine.entity;

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
@TableName(value = "ays_api_reslt_data")
public class AysModelResltDataEntity implements Serializable {
    /**
     * 主键
     */
    String newId;

    String id;
    /**
     * 接收处理标识
     */
    String workId;
    /**
     * 客户标识
     */
    String clientId;
    /**
     * 渠道标识
     */
    String channelId;
    String contentType;
    String originalId;
    LocalDateTime publishTime;
    /**
     * 原始数据
     */
    Object data;
    /**
     * 内容md5值
     */
    String dataMd5;
    /**
     * 接收时间
     */
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();

    Integer modelType;

    Object extFields;

    Object bizExtAttrs;

    Object bizExtAttrs2;

    Object bizExtAttrs3;

    String oneId;


    /**
     * 是否完成计算 是：1，否：0"
     */
    @Builder.Default
    String done = "0";
}
