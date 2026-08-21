package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/23 下午4:17
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRiskLevel {
    /**
     * 风险等级
     * S A B C D
     */
    @Schema(description = "风险等级")
    private String level;
    /**
     * G值范围-开始值
     */
    @Schema(description = "G值范围-开始值")
    private Integer startValue;
    /**
     * G值范围-结束值
     */
    @Schema(description = "G值范围-结束值")
    private Integer endValue;
    /**
     * 颜色
     */
    @Schema(description = "颜色")
    private String color;
    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean isApply;
}
