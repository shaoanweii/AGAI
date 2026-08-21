package com.voc.service.data.integration.entity;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Title: ErrDataIntegrationRecordEntity
 * @Package: com.voc.service.data.integration.entity
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/25 11:19
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_anal_di_stg_mate_data_finished_record")
public class ChannelExecutionResultEntity implements Serializable {
    String id;
    String dataId;
    String workId;
    String channelType;
    int retryCount;
    String errorCode;
    String errorMsg;
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    LocalDateTime createTime;
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    LocalDateTime lastExecTime;
    int status;
    String tid;

    Object data;

}
