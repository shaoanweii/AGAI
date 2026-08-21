package com.voc.service.insights.engine.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模型训练详情(InsModelDesc)表实体类
 *
 * @author leiww
 * @since 2024-02-22 11:55:45
 */
@Data
@TableName("ins_model_desc")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InsModelDescEntity extends Model<InsModelDescEntity>  implements Serializable {

    /**
     * 主键id
     */
//    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * ins_model_info的主键id
     */
    @TableField(value = "model_id")
    private String modelId;
    /**
     * 应用标签
     */
    @TableField(value = "model_label")
    private String modelLabel;
    /**
     * 模型路径
     */
    @TableField(value = "model_path")
    private String modelPath;
    /**
     * 模型描述
     */
    @TableField(value = "model_desc")
    private String modelDesc;
    /**
     * 模型状态
     */
    @TableField(value = "status")
    private String status;
    /**
     * F1值
     */
    @TableField(value = "test_acc")
    private String testAcc;
    /**
     * 版本号
     */
    @TableField(value = "version")
    private String version;
    /**
     * 版本说明
     */
    @TableField(value = "version_desc")
    private String versionDesc;
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

