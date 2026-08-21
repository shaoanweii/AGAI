package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/27 下午2:15
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagLibCategoryVo  implements Serializable {
    /**
     * 标签id
     */
    private String id;
    /**
     * 标签所属分类
     */
    private String tagParentId;
    /**
     * 标签名称
     */
    private String tagName;
    private String tagNameEn;
    /**
     * 标签类型
     */
    private String tagType;
    /**
     * 标签编码
     */
    private String tagCode;
    /**
     * 标签定义
     */
    private String tagDescription;
    /**
     * 标签状态
     */
    private String tagStatus;
    /**
     * 同义词
     */
    private String synonyms;

    //情感
    private String emotion;
    //意图
    private String intention;

    @Schema(description = "是否选中")
    @Builder.Default
    Boolean checked = Boolean.FALSE;

    private Integer sort;

    @Schema(description = "末级数量")
    private Integer leafCount;

    @Schema(description = "是否存在直属末级")
    @Builder.Default
    private Boolean hasFinalCategory = Boolean.FALSE;

    @Schema(description = "是否存在末级观点")
    @Builder.Default
    private Boolean hasFinalTopic = Boolean.FALSE;

    /**
     * 下级标签
     */
    private List<TagLibCategoryVo> child;
}
