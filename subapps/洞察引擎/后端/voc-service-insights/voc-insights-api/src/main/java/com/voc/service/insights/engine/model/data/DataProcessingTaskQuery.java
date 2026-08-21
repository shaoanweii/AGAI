package com.voc.service.insights.engine.model.data;

import com.voc.service.common.pagination.Page;
import lombok.Data;

/**
 * 数据处理任务列表查询条件。
 */
@Data
public class DataProcessingTaskQuery extends Page {
    private String clientId;
    private String taskName;
    private String taskType;
    private String dataSourceName;
    private String status;
    private String createUser;
}
