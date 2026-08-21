package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用车场景分类模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用车场景分类对象")
@EqualsAndHashCode(callSuper = false)
public class InsCarSceneCategoryModel extends Page implements Serializable {
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
     * 层级
     */
    @Schema(description = "层级")
    private Integer level;

    /**
     * 同义词
     */
    @Schema(description = "同义词")
    private String synonyms;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * id集合
     */
    @Schema(description = "id集合")
    private List<String> idsList;
}
