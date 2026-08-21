package com.voc.service.insights.engine.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InsCqCaLabelCorrectionRecordModel implements Serializable {

    @Schema(description = "唯一ID")
    @NotEmpty(message = "新增newId不能为空")
    List<String> newId;
    @Schema(description = "错误类型 1无效数据 2有效数据")
    private Integer errorType;

    @Schema(description = "品牌code")
    private String brandCode;

    @Schema(description = "品牌name")
    private String brandName;

    @Schema(description = "车系name")
    private String carSeriesName;

    @Schema(description = "观点name")
    private String topicName;

    @Schema(description = "车系code")
    private String carSeriesCode;

    @Schema(description = "观点")
    private String topicCode;

    @Schema(description = "情感")
    private String sentiment;

    @Schema(description = "意图")
    private String intention;

    @Schema(description = "用车场景一级")
    private String usageScenarioFirst;

    @Schema(description = "用车场景二级")
    private String usageScenarioSecond;

    @Schema(description = "纠错人")
    private String operateUser;

    private String startTime;

    private String endTime;


}
