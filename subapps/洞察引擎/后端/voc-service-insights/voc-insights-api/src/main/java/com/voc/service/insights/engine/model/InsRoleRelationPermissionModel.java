package com.voc.service.insights.engine.model;

import com.baomidou.mybatisplus.annotation.TableName;
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
public class InsRoleRelationPermissionModel implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    private String roleId;

    private String permissionId;

    private Integer buttonPermission;

    private Integer permissionType;

    private LocalDateTime createTime;

}
