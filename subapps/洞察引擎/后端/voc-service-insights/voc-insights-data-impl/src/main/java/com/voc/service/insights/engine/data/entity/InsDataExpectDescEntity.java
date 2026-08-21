package com.voc.service.insights.engine.data.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 语料库数据详情(InsDataExpectDesc)表实体类
 *
 * @author leiww
 * @since 2024-03-05 14:51:15
 */
@Data
@TableName("ins_data_expect_desc")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InsDataExpectDescEntity extends Model<InsDataExpectDescEntity> implements Serializable {

    /**
     * 主键id
     */
//    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 语料库数据集id
     */
    @TableField(value = "expect_id")
    private String expectId;
    /**
     * 内容
     */
    @TableField(value = "content")
    private String content;
    /**
     * 业务标签
     */
    @TableField(value = "business")
    private String business;
    /**
     * 质量标签
     */
    @TableField(value = "quality")
    private String quality;
    /**
     * 场景标签
     */
    @TableField(value = "scene")
    private String scene;
    /**
     * 情感
     */
    @TableField(value = "emotion")
    private String emotion;
    /**
     * 意图
     */
    @TableField(value = "intention")
    private String intention;
    /**
     * 观点
     */
    @TableField(value = "viewpoint")
    private String viewpoint;
    /**
     * 创建时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    /**
     * 修改用户
     */
    @TableField(value = "update_by")
    private String updateBy;
    /**
     * 创建用户
     */
    @TableField(value = "create_by")
    private String createBy;
}

