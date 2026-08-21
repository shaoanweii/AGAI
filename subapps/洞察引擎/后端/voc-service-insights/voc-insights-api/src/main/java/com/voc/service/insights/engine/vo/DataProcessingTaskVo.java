package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源批次聚合后的数据处理任务。
 */
@Data
public class DataProcessingTaskVo {
    private String batchId;
    private String dataSourceId;
    private String taskName;
    private String taskType;
    private String dataSourceName;
    private Long completedCount;
    private Long totalCount;
    private String createUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private String status;
}
