package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2025/12/12 14:17
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagClientVo implements Serializable {
    @Schema(description = "一级标签id")
    private String firstId;
    @Schema(description = "一级标签code")
    private String firstCode;
    @Schema(description = "一级标签名称")
    private String firstName;
    @Schema(description = "二级标签id")
    private String secondId;
    @Schema(description = "二级标签code")
    private String secondCode;
    @Schema(description = "二级标签名称")
    private String secondName;
    @Schema(description = "三级标签id")
    private String thirdId;
    @Schema(description = "三级标签code")
    private String thirdCode;
    @Schema(description = "三级标签名称")
    private String thirdName;
    @Schema(description = "四级标签id")
    private String fourthId;
    @Schema(description = "四级标签code")
    private String fourthCode;
    @Schema(description = "四级标签名称")
    private String fourthName;
    @Schema(description = "五级标签id")
    private String fifthId;
    @Schema(description = "五级标签code")
    private String fifthCode;
    @Schema(description = "五级标签情感")
    private String fifthEmotion;
    private String type;
}
