package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/10 18:35
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsCarSeriesVo implements Serializable {
    private String id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "英文名称")
    private String nameEn;
    @Schema(description = "品牌id")
    private String brandId;
    @Schema(description = "品牌编码")
    private String brandCode;
    @Schema(description = "图片")
    private String img;
    @Schema(description = "别名")
    private String alias;
    @Schema(description = "排除词")
    private String exclusionWords;
    @Schema(description = "是否核心车系 0非核心 1核心")
    private Integer isCore;
    @Schema(description = "是否核心车系名称")
    private String isCoreName;
    @Schema(description = "本竞品类型 1本品，2竞品，3非关注范围")
    private Integer competitiveType;
    @Schema(description = "本竞品类型名称")
    private String competitiveTypeName;
    @Schema(description = "关联本竞品 选择本品时绑定竞品车系，选择竞品时绑定本品车系（多对多）")
    private List<CarInfoVo> competitiveProduct;
    @Schema(description = "是否新车 0非新车 1新车")
    private Integer isNewCar;
    @Schema(description = "是否新车名称")
    private String isNewCarName;
    @Schema(description = "预热开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate preheatStartTime;
    @Schema(description = "预热结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate preheatEndTime;
    @Schema(description = "上市开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate launchStartTime;
    @Schema(description = "上市结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate launchEndTime;
    @Schema(description = "稳定开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stableStartTime;
    @Schema(description = "稳定结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate stableEndTime;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "状态名称")
    private String statusName;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "品牌名称")
    private String brandName;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
