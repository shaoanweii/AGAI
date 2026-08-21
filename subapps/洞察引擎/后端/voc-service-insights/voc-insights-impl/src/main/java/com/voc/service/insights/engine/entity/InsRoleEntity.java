package com.voc.service.insights.engine.entity;

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
@TableName("ins_role")
public class InsRoleEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    private String roleName;

    private Integer roleType;

    private Integer enabled;

    private String createUser;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
