package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 规则预警配置模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsClosedRuleAlertModel {

    /**
     * 预警周期：hourly=时，daily=日，weekly=周，monthly=月
     */
    @Schema(description = "预警周期：hourly=时，daily=日，weekly=周，monthly=月", required = true)
    @NotBlank(message = "预警周期不能为空")
    private String alertType;
    /**
     * 预警频次，如“周期是时的 2 4 8 16，周期是日的 固定为0，周期是周的 1 2 3 ... 6 7，周期是月的 1 2 3 ... 30 31”
     */
    @Schema(description = "预警频次，如“周期是时的 2 4 8 16，周期是日的 固定为0，周期是周的 1 2 3 ... 6 7，周期是月的 1 2 3 ... 30 31”", required = true)
    @NotBlank(message = "预警频次不能为空")
    private String alertFrequency;

    /**
     * 预警时间，如"2小时 实时""08:00:00"
     */
    @Schema(description = "预警时间，如“周期是时的直接为0，其余的都是时分秒 08:00:00”", required = true)
    @NotBlank(message = "预警时间不能为空")
    private String alertTime;

    /**
     * 预警的cron表达式，如：0 0 0/2 * * ?
     */
    private String alertCron;

    /**
     * 预警渠道（多选，存储渠道标识数组）
     */
    @Schema(description = "预警渠道（多选，存储渠道标识数组）,数据字典-closedRuleAlertChannel", required = true)
    @NotEmpty(message = "预警渠道不能为空")
    private List<String> alertChannel;
}