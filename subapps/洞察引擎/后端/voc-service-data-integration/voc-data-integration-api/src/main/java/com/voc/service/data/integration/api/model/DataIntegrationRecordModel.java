package com.voc.service.data.integration.api.model;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Title: ErrDataIntegrationRecordModel
 * @Package: com.voc.service.data.integration.api.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/25 11:26
 * @Version:1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = {"id"})
@ToString
public class DataIntegrationRecordModel implements Serializable {
    String id;
    String dataId;
    String workId;
    String channelType;
    @Builder.Default
    int retryCount = 0;
    String errorCode;
    String errorMsg;
    @Builder.Default
    LocalDateTime createTime = LocalDateTime.now();
    LocalDateTime lastExecTime;
    @Builder.Default
    int status = 0;
    String tid;
    Object data;

}
