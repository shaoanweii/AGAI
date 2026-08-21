package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VOC_RISK_WARNING_RULES_DETAILED
 */
@Tag(name = "警示规则明细表")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRiskWarningRulesDetailedModel  implements Serializable {
    private String id;

    /**
     * 警示规则id
     */
    @Schema(name = "警示规则id")
    private String warnRuleId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
    private LocalDateTime lastWarningTime;

    private BigDecimal delFlag;

    /**
     * 洞察周期(d,w,m,q,y)
     */
    @Schema(name = "洞察周期(d,w,m,q,y)")
    private String insightCycle;

    /**
     * 提及量
     */
    @Schema(name = "提及量")
    private BigDecimal statistic;

    /**
     * 负面提及量
     */
    @Schema(name = "负面提及量")
    private BigDecimal negativeNum;

    /**
     * 投诉量
     */
    @Schema(name = "投诉量")
    private BigDecimal complaintNum;

    /**
     * 发声渠道
     */
    @Schema(name = "发声渠道")
    private BigDecimal channelNum;

    /**
     * 净情感值
     */
    @Schema(name = "净情感值")
    private BigDecimal emotionNum;

    /**
     * 发声用户
     */
    @Schema(name = "发声用户")
    private BigDecimal userNum;
    /**
     * 风险词提及量
     */
    @Schema(name = "风险词提及量")
    private BigDecimal riskWordsNum;
    /**
     * 推送条件
     */
    @Schema(name = "推送条件")
    private Integer pushCondition;

    /**
     * 严重性(高,较高,中,较低,低)
     */
    @Schema(name = "严重性(高,较高,中,较低,低)")
    private String seriousness;
}
