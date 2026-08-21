package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsAddRuleTestModel implements Serializable {

    private String id;

    @Schema(description = "规则类型")
    private String ruleType;

    @Schema(description = "规则类型")
    private String ruleTestName;

    @Schema(description = "规则ID")
    private List<String> ruleId;

    @Schema(description = "批次ID")
    private String batchId;

    private String fileName;

    private String fileBaseName;
}
