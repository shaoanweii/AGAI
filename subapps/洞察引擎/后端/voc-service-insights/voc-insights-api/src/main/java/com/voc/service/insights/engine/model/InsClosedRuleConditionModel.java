package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 规则条件配置模型
 */
@Data
public class InsClosedRuleConditionModel {

    /**
     * 条件类型，如"品牌车系""意图"
     */
    @Schema(description = "条件类型", required = true)
    @NotBlank(message = "条件类型不能为空")
    private String conditionType;

    /**
     * 操作符，如"equal=等于""in=包含"
     */
    @Schema(description = "操作符，数据字典-closedRuleConditionOperator", required = true)
    @NotBlank(message = "操作符不能为空")
    private String operator;

    /**
     * 选项类型，如"选项""值""词库"
     */
    @Schema(description = "选项类型，数据字典-closedRuleConditionOption", required = true)
    @NotBlank(message = "选项类型不能为空")
    private String option;

    /**
     * value类型：string=字符串，array=数组
     */
    @Schema(description = "值类型，数据字典-closedRuleConditionValueType", required = true)
    @NotBlank(message = "值类型不能为空")
    private String valueType;

    /**
     * 条件值（根据value_type存储不同结构）
     */
    @Schema(description = "条件值（根据value_type存储不同结构）", required = true)
    @NotBlank(message = "条件值不能为空")
    private String value;

    /**
     * 条件排序（控制多条件展示顺序）
     */
    private Integer sortOrder;
}