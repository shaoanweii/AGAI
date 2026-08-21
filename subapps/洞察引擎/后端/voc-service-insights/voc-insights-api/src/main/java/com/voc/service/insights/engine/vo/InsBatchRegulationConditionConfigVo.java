package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/10 15:46
 * @描述: 批量规则条件配置VO
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsBatchRegulationConditionConfigVo {
    @Schema(description = "字段名称")
    private String name;
    @Schema(description = "字段名称编码")
    private String code;
    @Schema(description = "通配符")
    private Set<ConditionConfigVo> logicalOperator;
    @Schema(description = "值类型")
    private Set<ConditionConfigVo> condition;
    @Schema(description = "统计方式")
    private Set<ConditionConfigVo> countingMethod;
}
