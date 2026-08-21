package com.voc.service.insights.engine.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class InsertLabelCorrectionRecordModel implements Serializable {

    @Schema(description = "唯一ID")
    @NotBlank(message = "查询ID不能为空")
    String newId;

    @Schema(description = "客户ID不能为空")
    @NotBlank(message = "客户ID不能为空")
    String clientId;

    @Schema(description = "错误类型 1无效数据 2有效数据")
    private Integer errorType;

    @Schema(description = "选中状态 1正确 2错误")
    private Integer topicSelect;

    @Schema(description = "观点")
    private String topic;

    @Schema(description = "标签分类")
    private String LabelType;

    @Schema(description = "选中状态 1正确 2错误")
    private Integer tagSelect;

    @Schema(description = "一级标签名称")
    private String labelTypeLevelFirst;
    @Schema(description = "二级标签名称")
    private String labelTypeLevelSecond;
    @Schema(description = "三级级标签名称")
    private String labelTypeLevelThree;
    @Schema(description = "四级级标签名称")
    private String labelTypeLevelFour;
    @Schema(description = "五级标签名称")
    private String labelTypeLevelFive;

    private String labelTypeLevelFirstCode;
    private String labelTypeLevelSecondCode;
    private String labelTypeLevelThreeCode;
    private String labelTypeLevelFourCode;
    private String labelTypeLevelFiveCode;

    @Schema(description = "选中状态 1正确 2错误")
    private Integer sentimentSelect;

    @Schema(description = "情感")
    private String sentiment;


    @Schema(description = "选中状态 1正确 2错误")
    private Integer intentionSelect;

    @Schema(description = "意图")
    private String intention;

    @Schema(description = "纠错人")
    private String operateUser;


}
