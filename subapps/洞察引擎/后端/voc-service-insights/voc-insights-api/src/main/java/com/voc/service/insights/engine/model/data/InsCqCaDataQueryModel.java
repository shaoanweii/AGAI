package com.voc.service.insights.engine.model.data;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsCqCaDataQueryModel extends Page implements Serializable {

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "渠道ID")
    private List<String> firstChannelCodeList;

    @Schema(description = "渠道ID")
    private List<String> secondChannelCodeList;

    @Schema(description = "渠道ID")
    private List<String> threeChannelCodeList;

    @Schema(description = "数据状态")
    private List<String> dataStatus;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "情感")
    private List<String> sentiment;

    @Schema(description = "意图")
    private List<String> intention;

    @Schema(description = "品牌")
    private List<String> brandCode;

    @Schema(description = "车系")
    private List<String> carSeries;

    @Schema(description = "用车场景一级集合")
    private List<String> usageScenarioFirstList;

    @Schema(description = "用车场景二级集合")
    private List<String> usageScenarioSecondList;

    @Schema(description = "一级标签")
    private List<String> firstCodeTag;

    @Schema(description = "二级标签")
    private List<String> secondCodeTag;

    @Schema(description = "三级级标签")
    private List<String> threeCodeTag;

    @Schema(description = "四级级标签")
    private List<String> fourCodeTag;

    @Schema(description = "标签类型")
    private String tagType;

    @Schema(description = "标签编码集合")
    private List<String> tagCodeList;

    @Schema(description = "标签层级")
    private Integer level;

    @Schema(description = "是否高质量")
    private String highQualityTag;

    @Schema(description = "内容类型")
    private String contentType;

    @Schema(description = "情感程度")
    private String sentimentScore;

    private List<String> labelType;

    private String JOURLabel;

    private String CALabel;

    private String topic;

    private List<String> topicList;

    private List<String> dataId;

    private List<String> dataIdList;

    private List<String> idList;

    @Schema(description = "ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "标题关键词集合")
    private List<String> titleKeywordList;

    @Schema(description = "原始声音")
    private String content;

    @Schema(description = "原始声音关键词集合")
    private List<String> contentKeywordList;

    @Schema(description = "原始观点")
    private String opinion;

    @Schema(description = "原始观点关键词集合")
    private List<String> opinionKeywordList;

    @Schema(description = "是否水军")
    private String isWsaterArmy;

    @Schema(description = "是否管理层关注")
    private String isManagerFocused;

    @Schema(description = "是否大V")
    private String isBigV;

    @Schema(description = "作者ID")
    private String authorId;

    @Schema(description = "用户昵称")
    private String authorNick;

    @Schema(description = "是否主贴")
    private String isMainPost;

    @Schema(description = "工单ID")
    private String workOrderId;

    @Schema(description = "问卷ID")
    private String questId;

    @Schema(description = "问卷类型")
    private String questType;

    @Schema(description = "原始数据ID")
    private String originalId;

    @Schema(description = "声音片段")
    private String originalTextScene;

    @Schema(description = "声音ID")
    private String soundsId;

    @Schema(description = "D2C负责部门")
    private String d2cCcDept;

    @Schema(description = "原文链接")
    private String originalLink;

    @Schema(description = "标签-需要闭环")
    private String tagNeedForvclosedLoop;

    @Schema(description = "标签-高质量VOC标识")
    private String tagHighValueFlag;

    @Schema(description = "标签-问题严重程度")
    private List<String> tagIssueSeverity;

    @Schema(description = "用户名")
    private String userName;

    private List<String> channelCodeList;


    private String batchId;

    private String appClient;

    private String taskId;

    @Schema(description = "车企")
    private String automark;

    @Schema(description = "车企集合")
    private List<String> automarkList;

}
