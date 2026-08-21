package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 闭环规则批量操作模型
 */
@Data
public class InsClosedBatchOperationModel {

    @Schema(description = "要操作的规则", required = true)
    private List<String> ids;

    @Schema(description = "规则状态", required = true)
    @NotEmpty(message = "规则状态不能为空")
    private String isEnabled;
}