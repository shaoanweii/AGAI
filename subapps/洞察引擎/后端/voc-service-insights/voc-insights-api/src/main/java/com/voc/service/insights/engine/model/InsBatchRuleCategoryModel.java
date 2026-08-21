package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 批量规则分类模型
 * 支持二级分类结构，顶级分类的parentId为"0"
 */
@Data
public class InsBatchRuleCategoryModel {

    @Schema(description = "分类ID")
    private String id;

    @Schema(description = "分类名称", required = true)
    private String name;

    @Schema(description = "父分类ID，顶级分类为0")
    private String parentId;

    @Schema(description = "分类类型", defaultValue = "batchRule")
    private String type;

    @Schema(description = "状态：Enabled/Disabled", defaultValue = "Enabled")
    private String status;

    @Schema(description = "排序顺序", defaultValue = "0")
    private Integer sortOrder;

    @Schema(description = "操作人")
    private String operator;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "删除状态：0正常 1已删除", defaultValue = "0")
    private Integer delFlag;

    // 子分类列表，用于构建分类树
    @Schema(description = "子分类列表")
    private List<InsBatchRuleCategoryModel> children;

    // 规则数量，用于前端展示
    @Schema(description = "规则数量")
    private Integer ruleCount;
}
