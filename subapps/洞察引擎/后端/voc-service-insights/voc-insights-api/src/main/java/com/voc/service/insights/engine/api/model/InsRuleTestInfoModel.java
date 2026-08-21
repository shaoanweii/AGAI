package com.voc.service.insights.engine.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsRuleTestInfoModel implements Serializable {

    @Schema(description = "规则Id")
    private String ruleId;
}
