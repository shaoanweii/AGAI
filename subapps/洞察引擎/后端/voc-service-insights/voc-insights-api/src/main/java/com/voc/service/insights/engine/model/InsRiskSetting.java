package com.voc.service.insights.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/23 下午4:08
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRiskSetting {
    /**
     * 洞察周期类型
     * 1. 日
     * 2. 周
     * 3. 月
     * 4. 季
     * 5. 年
     */
    private String periodType;
    /**
     * 负面提及量
     */
    private Integer negative;
    /**
     * 投诉提及量
     */
    private Integer complaint;
    /**
     * 风险词提及量
     */
    private Integer riskWords;
    /**
     * 发声渠道
     */
    private Integer channelNum;
    /**
     * 净情感值
     */
    private BigDecimal affective;
    /**
     * 是否应用
     */
    private Boolean isApply;
}
