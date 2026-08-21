package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源库(InsDataResource)实体类
 *
 * @author leiww
 * @since 2024-04-02 16:37:37
 */
@Data
@TableName("ins_data_resource")
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class InsDataResourceEntity implements Serializable {
    /**
     * 主键id
     *
     * @TableId
     */
    private String id;

    /**
     * 资源名称
     */
    private String name;
    /**
     * 资源类型 custom:定制 general:标准
     */
    private String type;

    private String ruleType;
    /**
     * 图标
     */
    private String icon;
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

