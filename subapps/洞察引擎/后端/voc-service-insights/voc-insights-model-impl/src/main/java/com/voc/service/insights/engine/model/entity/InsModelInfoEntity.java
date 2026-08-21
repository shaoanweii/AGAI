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
 * 模型配置数据(InsModelInfo)表实体类
 *
 * @author leiww
 * @since 2024-02-22 13:32:56
 */
@Data
@TableName("ins_model_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InsModelInfoEntity extends Model<InsModelInfoEntity>  implements Serializable {

    /**
     * 主键id
     */
//    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 模型名称
     */
    @TableField(value = "model_name")
    private String modelName;
    /**
     * 模型类型
     */
    @TableField(value = "model_type")
    private String modelType;
    /**
     * 数据格式
     */
    @TableField(value = "format")
    private String format;
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
     * 项目名称
     */
    @TableField(value = "project_name")
    private String projectName;
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

