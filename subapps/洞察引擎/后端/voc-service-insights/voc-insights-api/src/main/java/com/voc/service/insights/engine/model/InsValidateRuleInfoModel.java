package com.voc.service.insights.engine.model;

import com.voc.service.insights.engine.vo.RegulationDetailsVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/28 16:10
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsValidateRuleInfoModel  implements Serializable {

    String clientId;
    @Schema(description = "本次执行校验规则的id集合")
    Set<String> validRuleIds;
    @Schema(description = "启用的规则的id集合")
    Set<String> enabledRuleIds;
    @Schema(description = "数据处理链路标识")
    private String workId;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;
    @Schema(description = "内容类型")
    private String contentType;
    @Schema(description = "数据渠道")
    private List<String> channel;
    @Schema(description = "匹配规则 满足全部条件：and,满足任意条件:or")
    private String matchingRule;
    @Schema(description = "筛选条件")
    private List<RegulationDetailsVo> attrs;
    @Schema(description = "本次条件范围内数据量")
    private  Long dataCount;
    @Schema(description = "校验状态 -1 未校验 0 校验中 1 校验成功 2 校验失败 默认为-1")
    private String validateStatus;
    private String ruleType;
}
