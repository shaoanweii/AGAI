package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/10 12:06
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsBrandInfoVo implements Serializable {
    private String id;
    @Schema(description = "品牌名称")
    private String name;
    @Schema(description = "英文名称")
    private String nameEn;
    private String code;
    @Schema(description = "车企")
    private String automark;
    @Schema(description = "图片")
    private String img;
    @Schema(description = "别名")
    private String alias;
    @Schema(description = "排除词")
    private String exclusionWords;
    @Schema(description = "是否核心名称")
    private String isCoreName;
    @Schema(description = "是否核心")
    private String isCore;
    @Schema(description = "本竞品类型名称")
    private String competitiveTypeName;
    @Schema(description = "本竞品类型")
    private String competitiveType;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    @Schema(description = "本竞品")
    private List<BrandInfoVo> competitiveProduct;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "状态名称")
    private String statusName;
}
