package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 批量规则模型
 * 批量事件闭环规则的核心模型类
 */
@Data
public class InsBatchRuleModel {

    @Schema(description = "规则ID")
    private String ruleId;

    @Schema(description = "规则名称", required = true)
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "品牌编码", required = true)
    @NotBlank(message = "品牌编码不能为空")
    private String brandCode;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "预警周期：hourly/daily/weekly/monthly", required = true)
    @NotBlank(message = "预警周期不能为空")
    private String alertType;

    @Schema(description = "预警频次", required = true)
    @NotBlank(message = "预警频次不能为空")
    private String alertFrequency;

    @Schema(description = "预警时间", required = true)
    @NotBlank(message = "预警时间不能为空")
    private String alertTime;

    @Schema(description = "预警cron表达式")
    private String alertCron;

    @Schema(description = "维度配置（JSON格式）", required = true)
    @NotBlank(message = "维度配置不能为空")
    private String dimensionConfig;

    @Schema(description = "指标配置（JSON格式）", required = true)
    @NotBlank(message = "指标配置不能为空")
    private String indicatorConfig;

    @Schema(description = "处理优先级", required = true)
    @NotBlank(message = "处理优先级不能为空")
    private String processPriority;

    @Schema(description = "审核人员（JSON格式）")
    private Object auditor;

    @Schema(description = "审核方式：auto/manual", required = true)
    @NotBlank(message = "审核方式不能为空")
    private String auditMethod;

    @Schema(description = "业务责任人（JSON格式）")
    private Object mainResponder;

    @Schema(description = "抄送人员（JSON格式）")
    private Object ccPersonnel;

    @Schema(description = "是否启用：enabled/disabled", required = true, defaultValue = "enabled")
    private String isEnabled;

    @Schema(description = "版本号", defaultValue = "1")
    private Integer version;

    @Schema(description = "创建人（JSON格式）")
    private String creator;

    @Schema(description = "更新人（JSON格式）")
    private String updater;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
