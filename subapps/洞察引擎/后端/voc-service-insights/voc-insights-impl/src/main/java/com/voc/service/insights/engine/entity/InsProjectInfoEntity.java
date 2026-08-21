package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_project_info")
public class InsProjectInfoEntity implements Serializable {

    /**
     * id
     */
    @Schema(description = "id")
    private String id;
    /**
     * 项目名称
     */
    @Schema(description = "项目名称")
    private String projectName;
    /**
     * 项目描述
     */
    @Schema(description = "项目描述")
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String projectDesc;
    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 更新者
     */
    private String updateUser;

}
