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
 * 用车场景实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_car_scene")
public class InsCarSceneEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private String id;

    /**
     * 用车场景名称
     */
    private String sceneName;

    /**
     * 用车场景描述
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String sceneDescription;

    /**
     * 用车场景分类id
     */
    private String categoryId;

    /**
     * 用车场景分类名称
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 同义词
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String synonyms;

    /**
     * 状态属性
     */
    private String status;

    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
