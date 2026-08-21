package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName msg_event_data
 * @createTime 2024年01月15日 12:00
 * @Copyright cuick
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sta_sys_depart")
public class InsStaSysDepartEntity implements Serializable {
    // ID
    private String id;

    // 应用组织ID，数字工作台orgId
    private String departId;

    // 组织名称
    private String name;

    // 0停用，1正常，-1删除
    private String status;

    // 组织编码，用于数据关联
    private String code;

    // 父级组织ID
    private String parentId;

    // 父级组织编码
    private String parentCode;

    // 备注信息
    private String remark;

    // 同级排序字段，默认值为0
    private String orgOrder;

    // 组织层级
    private String orgLevel;

    // 组织管理员账号ID(应用系统账号ID)
    private String orgAdmin;

    // 组织类型；0：无 1：集团 2：公司 3：部门
    private String orgType;

    // 所属公司Id
    private String companyId;

    // 所属公司编码
    private String companyCode;

    // 组织模板ID
    private String orgTemplateId;

    // 应用组织ID路径
    private String orgIdPath;

    // 所属租户编码
    private String tenantCode;

    // 所属租户ID
    private String tenantId;

    // 删除状态（0，正常，1已删除）
    private String delFlag;

    // 创建人
    private String createBy;

    // 创建日期
    private LocalDateTime createTime;

    // 更新人
    private String updateBy;

}
