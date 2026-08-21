package com.voc.service.analysis.model;

import cn.hutool.core.date.LocalDateTimeUtil;
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
public class AysModelResltDataModel
        implements Serializable {
    /**
     * 主键
     */
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
    /**
     * 原始数据
     */
    String data;
    /**
     * 内容md5值
     */
    String dataMd5;
    /**
     * 接收时间
     */
    LocalDateTime createTime;
    /**
     * 是否完成计算 是：1，否：0"
     */
    @Builder.Default
    String done = "0";

    @Builder.Default
    String startTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");

    @Builder.Default
    String endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
}
