package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class WarningTaskConditionsModel implements Serializable {

    @Schema(description = "渠道数组")
    private List<String> channelIds;

    @Schema(description = "品牌code")
    private String brandCode;

    @Schema(description = "情感")
    private String sentiment;

    @Schema(description = "原文类型")
    private String contentType;

    @Schema(description = "原文类型二级")
    private String contentTypeMin;

    @Schema(description = "意图")
    private String intentionType;

    @Schema(description = "原文")
    private String content;

    @Schema(description = "车系")
    private List<String> carSeriesCode;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "观点")
    private String topicCode;

    @Schema(description = "标签等级")
    private String level;

    @Schema(description = "标签code")
    private String tagCode;

    @Schema(description = "发布用户")
    private List<WarningUserModel> mainPostUser;

    @Schema(description = "主贴用户")
    private List<WarningUserModel> postUser;

    @Schema(description = "开始时间范围")
    private String startTime;

    @Schema(description = "结束时间范围")
    private String endTime;

    private String batchId;

    private List<String> adTypeList;

}
