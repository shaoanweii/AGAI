package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 11:31
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsAccountInfoModel extends Page implements Serializable {
    @Schema(description = "用户id")
    private String userId;
    @Schema(description = "账号类型 系统账号:system 运营账号:operation")
    private String accountType;
    @Schema(description = "账号名称")
    private String accountName;
    @Schema(description = "账号密码")
    private String accountPwd;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "联系方式")
    private String contact;
    @Schema(description = "权限")
    private List<InsPermissionsModel> permissions;
    @Schema(description = "停用/启用状态 停用:0 启用:1 默认启用")
    private String status;
    @Schema(description = "有效期, 若为空，则代表账号为长期有效")
    private String expiryDate;
    @Schema(description = "登录类型 表单:base 邮箱:email 默认为表单类型")
    @Builder.Default
    private String loginType = "base";
    @Schema(description = "客户id数组")
    private List<String> customers;
    @Schema(description = "客户id")
    private String clientId;
    @Schema(description = "角色ID")
    private String roleId;
    private String enable;
    @Schema(description = "完成率")
    private Integer completeRate;
    @Schema(description = "部门ID")
    private String deptId;

}
