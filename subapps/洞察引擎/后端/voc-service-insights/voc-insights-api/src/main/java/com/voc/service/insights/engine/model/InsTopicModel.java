package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2025/12/18 10:03
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsTopicModel extends Page implements Serializable {
    @Schema(description = "观点名称")
    private String tagName;
    @Schema(description = "观点描述")
    private String tagDescription;
    @Schema(description = "同义词")
    private String synonyms;
    @Schema(description = "体验编码")
    private List<InsTopicExperienceCodeModel>  experienceCode;
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
    @Schema(description = "代码的精准性(精准、有待提升等)")
    private String tagAccuracy;
    @Schema(description = "业务领域(产品质量、产品设计、服务体验)")
    private String tagBusinessDomain;
    @Schema(description = "是否需回复")
    private String tagComplaintFlagNeedingReply;
    @Schema(description = "是否需闭环")
    private String tagNeedForvclosedLoop;
    @Schema(description = "主责部门")
    private String d2cResponsibleDept;
    @Schema(description = "状态")
    private String tagStatus;

    private String appClient;
    @Schema(description = "观点编码集合")
    private List<String> topicCodes;
    @Schema(description = "标签编码")
    private String tagCode;
    @Schema(description = "标签编码集合")
    private List<String> tagCodes;
    @Schema(description = "操作人")
    private String operateUser;
    @Schema(description = "标签类型")
    private String tagType;
    @Schema(description = "属性标签id集合")
    private List<String> attributeLabelIds;
    @Schema(description = "情感集合")
    private List<String> emotions;
    @Schema(description = "意图集合")
    private List<String> intentions;
    @Schema(description = "客户问题分级集合")
    private List<String> tagCustomerIssueClassifications;
    @Schema(description = "问题程度集合")
    private List<String> tagIssueSeverities;
    @Schema(description = "业务领域集合")
    private List<String> tagBusinessDomains;


}
