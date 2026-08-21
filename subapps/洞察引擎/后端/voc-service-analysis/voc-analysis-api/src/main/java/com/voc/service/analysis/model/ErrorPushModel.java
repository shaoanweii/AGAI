package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Title: ErrorPushModel
 * @Package: com.voc.service.analysis.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/25 14:16
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorPushModel implements Serializable {
    String id;
    String table;
    String action;
    String clientId;
    Object data;
    String workId;
    /**
     * 接收时间
     */
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();

    String tid;
}
