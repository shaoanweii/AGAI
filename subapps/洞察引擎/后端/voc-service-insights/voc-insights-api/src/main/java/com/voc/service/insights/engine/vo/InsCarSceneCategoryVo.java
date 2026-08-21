package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用车场景分类返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用车场景分类返回对象")
public class InsCarSceneCategoryVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;

    /**
     * patentId
     */
    @Schema(description = "patentId")
    private String patentId;

    /**
     * 子节点
     */
    @Schema(description = "子节点")
    private List<InsCarSceneCategoryVo> children;

    /**
     * 节点类型：category-用车场景分类，scene-用车场景
     */
    @Schema(description = "节点类型：category-用车场景分类，scene-用车场景")
    private String nodeType;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述")
    private String categoryDescription;
    /**
     * 同义词
     */
    @Schema(description = "同义词")
    private String synonyms;

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
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String typeName;

    /**
     * 层级
     */
    @Schema(description = "层级")
    private Integer level;

    /**
     * 末级数量
     */
    @Schema(description = "末级数量")
    private Integer leafCount;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
}
