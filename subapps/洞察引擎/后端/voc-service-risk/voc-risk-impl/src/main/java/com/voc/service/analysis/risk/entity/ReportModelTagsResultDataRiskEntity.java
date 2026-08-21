package com.voc.service.analysis.risk.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "report_model_tags_result_data_risk")
public class ReportModelTagsResultDataRiskEntity {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "闭环规则ID")
    private String closedRuleId;

    @Schema(description = "预警时间")
    private LocalDateTime warningTime;

    @Schema(description = "预警事件编号")
    private String warningEventNo;

    @Schema(description = "声音数据ID")
    private String soundsId;

    @Schema(description = "源数据ID")
    private String dataId;

    @Schema(description = "统一ID")
    private String oneId;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "事件名称")
    private String eventName;

    @Schema(description = "主题分类ID")
    private String subjectCategoryId;

    @Schema(description = "主题分类名称")
    private String subjectCategoryName;

    @Schema(description = "事件优先级（如P1/P2/P3）")
    private String eventPriority;

    @Schema(description = "事件优先级名称（如紧急/重要/普通）")
    private String eventPriorityName;

    @Schema(description = "事件级别（如L1/L2/L3）")
    private String eventLevel;

    @Schema(description = "事件级别名称（如高风险/中等风险/低风险）")
    private String eventLevelName;

    @Schema(description = "确认方式（如系统自动确认/人工确认）")
    private String confirmationMethod;

    @Schema(description = "确认部门ID")
    private String confirmOrgId;

    @Schema(description = "确认部门编号")
    private String confirmOrgNo;

    @Schema(description = "确认部门名称")
    private String confirmOrgName;

    @Schema(description = "确认人ID")
    private String confirmUserId;

    @Schema(description = "确认人工号")
    private String confirmUserEmpNo;

    @Schema(description = "确认人姓名")
    private String confirmUserName;

    @Schema(description = "审核方式（如人工审核/自动审核）")
    private String reviewMethod;

    @Schema(description = "审核部门ID")
    private String reviewOrgId;

    @Schema(description = "审核部门编号")
    private String reviewOrgNo;

    @Schema(description = "审核部门名称")
    private String reviewOrgName;

    @Schema(description = "审核人ID")
    private String reviewUserId;

    @Schema(description = "审核人工号")
    private String reviewUserEmpNo;

    @Schema(description = "审核人姓名")
    private String reviewUserName;

    @Schema(description = "主责部门ID")
    private String mainRespOrgId;

    @Schema(description = "主责部门编号")
    private String mainRespOrgNo;

    @Schema(description = "主责部门名称")
    private String mainRespOrgName;

    @Schema(description = "主责人ID")
    private String mainRespUserId;

    @Schema(description = "主责人工号")
    private String mainRespUserEmpNo;

    @Schema(description = "主责人姓名")
    private String mainRespUserName;

    @Schema(description = "抄送用户列表（JSON数组字符串）")
    private String ccUsers;

    @Schema(description = "事件处理开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventProcessStartTime;

    @Schema(description = "事件处理结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventProcessEndTime;

    @Schema(description = "是否重复事件（0=否，1=是）")
    private String isEventDuplicate;

    @Schema(description = "是否已处理（0=否，1=是）")
    private String isProcessed;

    @Schema(description = "未处理原因")
    private String unprocessedReason;

    @Schema(description = "处理描述")
    private String processDescription;

    @Schema(description = "复核进度编码")
    private String reviewProgressCode;

    @Schema(description = "复核进度名称")
    private String reviewProgressName;

    @Schema(description = "复核人ID")
    private String reviewHandlerId;

    @Schema(description = "复核人姓名")
    private String reviewHandlerName;

    @Schema(description = "复核内容")
    private String reviewContent;

    @Schema(description = "私信进度编码")
    private String privateMsgProgressCode;

    @Schema(description = "私信进度名称")
    private String privateMsgProgressName;

    @Schema(description = "私信数量")
    private String privateMsgCount;

    @Schema(description = "私信渠道")
    private String privateMsgChannel;

    @Schema(description = "私信详情（JSON数组字符串）")
    private String privateMsgDetails;

    @Schema(description = "关联工单号列表（JSON数组字符串）")
    private String relatedWorkOrderNos;

    @Schema(description = "工单负责人")
    private String workOrderResponder;

    @Schema(description = "原工单负责人")
    private String oldWorkOrderResponder;

    @Schema(description = "渠道编码")
    private String channelCode;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "二级渠道code")
    private String secondChannelCode;

    @Schema(description = "二级渠道名称")
    private String secondChannelName;

    @Schema(description = "品牌编码")
    private String brandCode;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "车系编码")
    private String carSeriesCode;

    @Schema(description = "车系名称")
    private String carSeriesName;

    @Schema(description = "车型")
    private String carModel;

    @Schema(description = "发动机号")
    private String engineNo;

    @Schema(description = "车牌号")
    private String licensePlateNo;

    @Schema(description = "车架号（VIN）")
    private String vinNo;

    @Schema(description = "购车时间")
    private LocalDateTime carPurchaseTime;

    @Schema(description = "经销商名称")
    private String dealerName;

    @Schema(description = "经销商编码")
    private String dealerCode;

    @Schema(description = "核心内容")
    private String content;

    @Schema(description = "内容类型（如用户反馈/工单/评论）")
    private String contentType;

    @Schema(description = "原文场景")
    private String originalTextScene;

    @Schema(description = "情感倾向（正面/负面/中性）")
    private String sentiment;

    @Schema(description = "意图类型（如故障反馈/咨询/投诉）")
    private String intentionType;

    @Schema(description = "主题")
    private String topic;

    @Schema(description = "全领域一级标签编码")
    private String domTagFirstCode;

    @Schema(description = "全领域二级标签编码")
    private String domTagSecondCode;

    @Schema(description = "全领域三级标签编码")
    private String domTagThreeCode;

    @Schema(description = "全领域四级标签编码")
    private String domTagFourCode;

    @Schema(description = "全领域一级标签名称")
    private String domTagFirst;

    @Schema(description = "全领域二级标签名称")
    private String domTagSecond;

    @Schema(description = "全领域三级标签名称")
    private String domTagThree;

    @Schema(description = "全领域四级标签名称")
    private String domTagFour;

    @Schema(description = "评论用户ID")
    private String commentUserId;

    @Schema(description = "评论用户姓名")
    private String commentUserName;

    @Schema(description = "评论时间")
    private LocalDateTime commentTime;

    @Schema(description = "评论详情")
    private String commentDetails;

    @Schema(description = "是否主帖（0=否，1=是）")
    private String isMainPost;

    @Schema(description = "主帖标题")
    private String mainPostTitle;

    @Schema(description = "主帖详情")
    private String mainPostDetails;

    @Schema(description = "发帖用户ID")
    private String postUserId;

    @Schema(description = "发帖用户姓名")
    private String postUserName;

    @Schema(description = "发帖时间")
    private LocalDateTime postTime;

    @Schema(description = "主帖链接")
    private String mainPostUrl;

    @Schema(description = "敏感类型（如一般敏感/高度敏感/不敏感）")
    private String sensitiveType;

    @Schema(description = "事件清晰度（如清晰/模糊/一般）")
    private String eventClarity;

    @Schema(description = "是否需要回复（0=否，1=是）")
    private String isNeedReply;

    @Schema(description = "是否需要闭环（0=否，1=是）")
    private String isNeedClosedLoop;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新人ID")
    private String updateUserId;

    @Schema(description = "任务状态（如待处理/处理中/已完成/已驳回）")
    private String taskStatus;

    private String appNameFinal;
}
