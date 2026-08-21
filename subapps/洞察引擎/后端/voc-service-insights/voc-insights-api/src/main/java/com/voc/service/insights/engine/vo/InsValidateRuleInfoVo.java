package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/29 09:11
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsValidateRuleInfoVo  implements Serializable {
    @Schema(description = "规则id集合")
    private Set<String> ruleId;
    @Schema(description = "数据处理链路标识")
    private String workId;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;
    @Schema(description = "属性集合")
    private Map<String,String> attrs;
    @Schema(description = "本次条件范围内数据量")
    private  Long dataCount;
}
