package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsRuleHitInfoVo implements Serializable {

    @Schema(description = "规则ID")
    private String ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "维度配置")
    private String dimensionConfigList;

    @Schema(description = "事件级别")
    private String eventLevel;

    @Schema(description = "处理优先级")
    private String processingPriority;

    @Schema(description = "数据结果")
    private String dataResult;
}
