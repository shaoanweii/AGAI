package com.voc.service.risk.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class BatchTaskConditionsModel implements Serializable {

    @Schema(description = "开始时间范围")
    private String startTime;

    @Schema(description = "结束时间范围")
    private String endTime;

    private String prevStartTime;
    private String prevEndTime;

    private String prevPrevStartTime;
    private String prevPrevEndTime;

    private String yearStartTime;
    private String monthStartTime;

    private String lastYearStartTime;
    private String lastYearEndTime;

    private String lastLastYearStartTime;
    private String lastLastYearEndTime;

    @Schema(description = "渠道数组")
    private List<String> channelIds;

    @Schema(description = "品牌code")
    private String brandCode;

    @Schema(description = "情感")
    private List<String> sentimentList;

    @Schema(description = "原文类型")
    private String contentType;

    @Schema(description = "原文类型二级")
    private String contentTypeMin;

    @Schema(description = "意图")
    private String intentionType;

    @Schema(description = "原文")
    private List<String> content;

    @Schema(description = "车系")
    private List<String> carSeriesCode;

    @Schema(description = "标题")
    private List<String> title;

    @Schema(description = "观点")
    private List<String> topicCodeList;

    @Schema(description = "标签等级")
    private String level;

    @Schema(description = "标签code")
    private List<String> tagCodeList    ;

    @Schema(description = "发布用户")
    private List<BatchRuleUserModel> mainPostUser;

    @Schema(description = "主贴用户")
    private List<BatchRuleUserModel> postUser;

    private String batchId;

    private Integer ruleType;

    @Schema(description = "查询拼接条件")
    private String whereClause;

    @Schema(description = "广告类型")
    private List<String> adTypeList;

    @Schema(description = "省份")
    private List<String> provinceList;

    @Schema(description = "性别")
    private String customerGender;

    @Schema(description = "是否水军")
    private String waterMan;

    @Schema(description = "是否大V")
    private String VMan;

    @Schema(description = "客户类型")
    private List<String> custClassifyList;

    @Schema(description = "一级标签")
    private List<String> firstCodeTag;

    @Schema(description = "二级标签")
    private List<String> secondCodeTag;

    @Schema(description = "三级级标签")
    private List<String> threeCodeTag;

    @Schema(description = "四级级标签")
    private List<String> fourCodeTag;

    private List<String> emotion;

    private List<String> standpoint;

    private List<String> contentResourceIdList;

    private List<String> contentRuleIdList;

    private List<String> titleResourceIdList;

    private List<String> titleRuleIdList;

    private String topCurrentWhere;

    private String topPreviousWhere;

    private String logicOperator;

    private Boolean groupByCarSeries;

    private Boolean groupByChannel;

    private String groupField;

    private String avgDayType;

}
