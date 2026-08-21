package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.insights.engine.api.annotation.*;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 17:21
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegulationInfoVo  implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 规则名称
     */
//    @Client
    private String clientId;
    /**
     * 规则名称
     */
    private String name;
    /**
     * 规则描述
     */
    private String description;

    /**
     * 规则类型
     */
    @RuleType
    private String regulationType;

    /**
     * 内容类型 例如:文本、工单
     */
    @RuleContent
    private String contentType;

    /**
     * 数据渠道
     */
//    @Channel
    private List<String> channel;
    private List<String> channelText;

    @Stage
    private String processPhase;

    @Weight
    private String regulationWeight;
    /**
     * 匹配规则
     */
    @Schema(description = "匹配规则 满足全部条件：and,满足任意条件:or")
    @matchingRule
    private String matchingRule;

    @RuleCliassify
    private String regulationClassify;

    /**
     * 停用/启用状态 停用:0 启用:1
     */
    @Dict(code = InsightsConstants.REPOSITORY_STATYS)
    private String status;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @Dict(code = InsightsConstants.SINGLE_VALIDATE)
    private String singleValidateStatus;
    /**
     * 完全验证状态 -1 未测试 0 测试中 1 测试成功 2 测试失败 默认 -1
     */
    @Dict(code = InsightsConstants.FULLY_VALIDATE)
    private String fullyValidateStatus;

    List<RegulationDetailsVo> regulationConditions;

    List<RegulationDetailsVo> regulationPerformAction;

    private String statusCount;

}
