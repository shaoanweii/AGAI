package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 批量规则历史记录模型
 * 用于记录规则的变更历史
 */
@Data
public class InsBatchRuleHisModel {

    @Schema(description = "历史记录ID")
    private String hisId;

    @Schema(description = "规则ID")
    private String ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "品牌编码")
    private String brandCode;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "预警周期")
    private String alertType;

    @Schema(description = "预警频次")
    private String alertFrequency;

    @Schema(description = "预警时间")
    private String alertTime;

    @Schema(description = "预警cron表达式")
    private String alertCron;

    @Schema(description = "维度配置（JSON格式）")
    private String dimensionConfig;

    @Schema(description = "指标配置（JSON格式）")
    private String indicatorConfig;

    @Schema(description = "处理优先级")
    private String processPriority;

    @Schema(description = "审核人员（JSON格式）")
    private Object auditor;

    @Schema(description = "审核方式")
    private String auditMethod;

    @Schema(description = "业务责任人（JSON格式）")
    private Object mainResponder;

    @Schema(description = "抄送人员（JSON格式）")
    private Object ccPersonnel;

    @Schema(description = "是否启用")
    private String isEnabled;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "编辑人（JSON格式）")
    private String editUser;

    @Schema(description = "编辑时间")
    private Date editTime;
}
