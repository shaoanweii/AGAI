package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 批量规则查询模型
 * 用于规则列表的分页查询
 */
@Data
public class InsBatchRuleQueryModel {

    @Schema(description = "分页页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "分页大小", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "品牌编码")
    private String brandCode;

    @Schema(description = "是否启用：enabled/disabled")
    private String isEnabled;

    @Schema(description = "预警周期：hourly/daily/weekly/monthly")
    private String alertType;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "排序方向：asc/desc")
    private String orderDirection;
}
