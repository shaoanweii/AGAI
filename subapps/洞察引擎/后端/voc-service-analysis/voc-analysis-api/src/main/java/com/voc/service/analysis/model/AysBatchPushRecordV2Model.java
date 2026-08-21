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
public class AysBatchPushRecordV2Model implements Serializable {
    String id;
    /**
     * 接收处理标识
     */
    String workId;

    /**
     * 原始数据
     */
    String reqeutId;

    /**
     * 数据状态
     */
    String status;
    /**
     * 数据状态
     */
    String source;
    /**
     * 接收时间
     */
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();
    /**
     * 更新时间
     */
    @Builder.Default
    LocalDateTime updateTime = LocalDateTime.now();

    String tid;
}
