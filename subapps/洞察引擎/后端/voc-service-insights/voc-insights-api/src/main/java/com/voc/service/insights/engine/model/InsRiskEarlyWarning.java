package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/23 下午3:58
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRiskEarlyWarning {
    /**
     * 预警类型
     */
    @Schema(description = "预警类型")
    private String warningType;
    /**
     * 基础信息设置
     */
    @Schema(description = "基础信息设置")
    private List<InsRiskSetting> riskSetting;
    /**
     * 风险等级
     */
    @Schema(description = "风险等级")
    private List<InsRiskLevel> riskLevel;


}
