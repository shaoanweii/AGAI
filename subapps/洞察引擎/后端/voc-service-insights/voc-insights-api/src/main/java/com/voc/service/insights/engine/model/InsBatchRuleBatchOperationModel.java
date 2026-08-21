package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * 批量规则操作模型
 * 用于批量启用/禁用规则
 */
@Data
public class InsBatchRuleBatchOperationModel {

    @Schema(description = "规则ID集合", required = true)
    @NotEmpty(message = "规则ID集合不能为空")
    private Set<String> ids;

    @Schema(description = "是否启用：enabled/disabled", required = true)
    @NotNull(message = "操作状态不能为空")
    private String isEnabled;
}
