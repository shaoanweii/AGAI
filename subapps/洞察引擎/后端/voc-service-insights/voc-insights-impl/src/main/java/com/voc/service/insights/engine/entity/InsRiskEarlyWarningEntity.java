package com.voc.service.insights.engine.entity;

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
 * @创建时间: 2024/9/25 上午10:40
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRiskEarlyWarningEntity {
    /**
     * 预警类型
     */
    private String warningType;
    /**
     * 基础信息设置
     */
    private List<RiskSettingEntity> riskSetting;
    /**
     * 风险等级
     */
    private List<RiskLevelEntity> riskLevel;
}
