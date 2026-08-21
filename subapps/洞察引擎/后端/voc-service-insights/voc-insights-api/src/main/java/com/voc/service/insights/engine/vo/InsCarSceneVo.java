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
 * 用车场景返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用车场景返回对象")
public class InsCarSceneVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;

    /**
     * 用车场景名称
     */
    @Schema(description = "用车场景名称")
    private String sceneName;

    /**
     * 用车场景描述
     */
    @Schema(description = "用车场景描述")
    private String sceneDescription;

    /**
     * 用车场景分类id
     */
    @Schema(description = "用车场景分类id")
    private String categoryId;

    /**
     * 用车场景分类名称
     */
    @Schema(description = "用车场景分类名称")
    private String categoryName;

    /**
     * 同义词
     */
    @Schema(description = "同义词")
    private String synonyms;

    /**
     * 状态属性
     */
    @Schema(description = "状态属性")
    private String status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称")
    private String statusName;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String operator;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
