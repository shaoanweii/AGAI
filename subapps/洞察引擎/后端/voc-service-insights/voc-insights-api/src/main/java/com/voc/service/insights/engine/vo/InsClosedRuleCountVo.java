package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: LiuQiang
 * @time: 2025/11/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsClosedRuleCountVo {

    @Schema(description = "分类类型")
    private String categoryType;

    @Schema(description = "规则数量")
    private Integer count;
}
