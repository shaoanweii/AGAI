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
@TableName(value = "dws_voc2_batch_push_record")
public class AysBatchPushRecordV2Entity implements Serializable {
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

    Integer modelType;

    Object extFields;
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
