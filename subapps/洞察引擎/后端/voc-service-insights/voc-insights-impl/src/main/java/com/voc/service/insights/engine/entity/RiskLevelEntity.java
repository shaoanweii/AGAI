package com.voc.service.insights.engine.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 上午10:42
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskLevelEntity {
    /**
     * 风险等级
     * S A B C D
     */
    private String level;
    /**
     * G值范围-开始值
     */
    private Integer startValue;
    /**
     * G值范围-结束值
     */
    private Integer endValue;
    /**
     * 颜色
     */
    private String color;
    /**
     * 是否启用
     */
    private Boolean isApply;
}
