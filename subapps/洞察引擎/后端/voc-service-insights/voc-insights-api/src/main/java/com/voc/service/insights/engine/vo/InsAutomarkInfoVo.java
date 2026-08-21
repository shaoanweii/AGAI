package com.voc.service.insights.engine.vo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
 * @创建时间: 2026/2/11 15:53
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsAutomarkInfoVo implements Serializable {
    private String id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "英文名称")
    private String nameEn;
    @Schema(description = "图片")
    private String img;
    @Schema(description = "是否核心")
    private Integer isCore;
    @Schema(description = "是否核心名称")
    private String isCoreName;
    @Schema(description = "本竞品类型")
    private Integer competitiveType;
    @Schema(description = "本竞品关系名称")
    private String competitiveTypeName;
    @Schema(description = "本竞品关系")
    private List<AutomarkVo> competitiveProduct;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "状态名称")
    private String statusName;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
