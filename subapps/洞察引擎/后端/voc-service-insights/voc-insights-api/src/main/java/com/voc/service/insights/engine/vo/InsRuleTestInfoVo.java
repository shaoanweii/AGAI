package com.voc.service.insights.engine.vo;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/13 10:05
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsRuleTestInfoVo extends Page implements Serializable {

    @Schema(description = "id")
    private String id;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "车系名称")
    private String carSeriesName;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "是否有主贴")
    private String hasMainPost;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "情感")
    private String sentiment;

    @Schema(description = "意图")
    private String intention;

    @Schema(description = "dom标签")
    private String domTagFirst;
    @Schema(description = "dom标签")
    private String domTagSecond;
    @Schema(description = "dom标签")
    private String domTagThree;
    @Schema(description = "dom标签")
    private String domTagFour;

    @Schema(description = "主题")
    private String topic;

    @Schema(description = "发布人名称")
    private String publishUserName;

    @Schema(description = "发布人id")
    private String publishUserId;

    @Schema(description = "主贴发布人名称")
    private String mainUserId;

    @Schema(description = "主贴发布人名称")
    private String mainUserName;

    @Schema(description = "规则id")
    private String ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

}
