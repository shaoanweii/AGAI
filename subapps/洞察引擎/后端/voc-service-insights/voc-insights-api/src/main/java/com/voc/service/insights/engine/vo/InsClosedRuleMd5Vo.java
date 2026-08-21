package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 闭环规则MD5值VO
 * @author: LiuQiang
 * @time: 2025/11/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsClosedRuleMd5Vo {

    @Schema(description = "规则ID")
    private String ruleId;

    @Schema(description = "规则MD5值")
    private String md5Str;
}
