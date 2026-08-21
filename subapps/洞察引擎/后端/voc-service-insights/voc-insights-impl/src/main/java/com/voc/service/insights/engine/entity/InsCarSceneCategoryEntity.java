package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用车场景分类实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_car_scene_category")
public class InsCarSceneCategoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private String id;

    /**
     * patentId
     */
    private String patentId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类描述
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String categoryDescription;

    /**
     * 分类名称
     */
    @TableField(exist = false)
    private String typeName;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 同义词
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String synonyms;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;
}
