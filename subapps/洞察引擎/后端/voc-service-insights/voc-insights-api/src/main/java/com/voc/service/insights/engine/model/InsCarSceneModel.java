package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用车场景模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用车场景对象")
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "cs.create_time"),
        @SortField(source = "updateTime", targer = "cs.update_time"),
        @SortField(source = "categoryName", targer = "categoryName")
})
public class InsCarSceneModel extends Page implements Serializable {
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
     * 用车场景分类id集合
     */
    @Schema(description = "用车场景分类id集合")
    private List<String> categoryIds;

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
     * id集合
     */
    @Schema(description = "id集合")
    private List<String> ids;
    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String operator;
}
