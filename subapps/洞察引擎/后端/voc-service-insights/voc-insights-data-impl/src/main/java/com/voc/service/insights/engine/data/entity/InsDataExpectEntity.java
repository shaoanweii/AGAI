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
 * 语料库数据集(InsDataExpect)表实体类
 *
 * @author leiww
 * @since 2024-03-05 14:44:43
 */
@Data
@TableName("ins_data_expect")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InsDataExpectEntity extends Model<InsDataExpectEntity> implements Serializable {

    /**
     * 主键id
     */
//    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 数据集语料库名称
     */
    @TableField(value = "name")
    private String name;
    /**
     * 数据格式
     */
    @TableField(value = "format")
    private String format;
    /**
     * 数据总数
     */
    @TableField(value = "count")
    private Integer count;
    /**
     * ⽤户客户ID
     */
    @TableField(value = "client_id")
    private String clientId;
    /**
     * 项目Id
     */
    @TableField(value = "project_id")
    private String projectId;
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

