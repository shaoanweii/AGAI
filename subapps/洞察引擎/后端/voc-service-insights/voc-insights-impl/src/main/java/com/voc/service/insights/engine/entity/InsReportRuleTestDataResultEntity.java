package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "report_rule_test_data_risk_result")
public class InsReportRuleTestDataResultEntity implements Serializable {

    private String id;
    private String dataId;

    private String batchId;

    private String ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "渠道名称")
    private String channelName;

    private String channelCode;

    @Schema(description = "品牌名称")
    private String brandName;

    private String brandCode;

    @Schema(description = "车系名称")
    private String carSeriesName;

    private String carSeriesCode;

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

    @Schema(description = "dom标签")
    private String domTagFirstCode;
    @Schema(description = "dom标签")
    private String domTagSecondCode;
    @Schema(description = "dom标签")
    private String domTagThreeCode;
    @Schema(description = "dom标签")
    private String domTagFourCode;

    @Schema(description = "主题")
    private String topicId;

    private String topicText;

    @Schema(description = "发布人名称")
    private String publishUserNickname;

    @Schema(description = "发布人id")
    private String publishUserId;

    @Schema(description = "发布用户昵称")
    private String mainPostUserId;

    @Schema(description = "主贴发布人名称")
    private String mainPostUserName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
