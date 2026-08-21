package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.model.InsRiskLevel;
import com.voc.service.insights.engine.model.InsRiskSetting;
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
public class InsRiskEarlyWarningVo {
    /**
     * 预警类型
     */
    @Schema(description = "预警类型")
    private String warningType;
    /**
     * 基础信息设置
     */
    @Schema(description = "基础信息设置")
    private List<InsRiskSettingVo> riskSetting;
    /**
     * 风险等级
     */
    @Schema(description = "风险等级")
    private List<InsRiskLevelVo> riskLevel;


}
