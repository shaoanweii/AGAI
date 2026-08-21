package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2025/12/17 14:10
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicVo implements Serializable {
    @Schema(description = "观点code")
    private String topicCode;
    @Schema(description = "观点名称")
    private String topicName;
    @Schema(description = "观点描述")
    private String topicDesc;
    @Schema(description = "同义词")
    private String synonyms;
    @Schema(description = "智慧交互中心编码")
    private String mappingCode;
    @Schema(description = "情感")
    private String emotion;
    @Schema(description = "意图")
    private String intention;
    @Schema(description = "客户问题分级(S、A、B、C等)")
    private String tagCustomerIssueClassification;
    @Schema(description = "问题程度(高、中、低)")
    private String tagIssueSeverity;
    @Schema(description = "事件清晰度")
    private String eventClarity;
    @Schema(description = "敏感类型")
    private String susceptiveType;
    @Schema(description = "业务领域(产品质量、产品设计、服务体验)")
    private String tagBusinessDomain;
    @Schema(description = "主责部门")
    private String d2cResponsibleDept;
    @Schema(description = "代码的精准性(精准、有待提升等)")
    private String tagAccuracy;
    @Schema(description = "是否需回复")
    private String tagComplaintFlagNeedingReply;
    @Schema(description = "是否需闭环")
    private String tagNeedForvclosedLoop;
    @Schema(description = "全领域业务")
    private TagClientVo ca;
    @Schema(description = "用户旅程")
    private TagClientVo jour;
    @Schema(description = "VRT")
    private TagClientVo vrt;
    @Schema(description = "CPT")
    private TagClientVo cpt;
    @Schema(description = "商品化属性")
    private TagClientVo pro;
    @Schema(description = "NPS")
    private TagClientVo nps;
    @Schema(description = "操作人")
    private String operateUser;
    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @Schema(description = "创建人")
    private String createUser;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "状态")
    private String tagStatus;

}
