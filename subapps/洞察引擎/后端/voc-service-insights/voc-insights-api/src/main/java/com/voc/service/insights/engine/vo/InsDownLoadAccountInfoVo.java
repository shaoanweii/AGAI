package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 13:04
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDownLoadAccountInfoVo implements Serializable {
    @Schema(description = "用户id")
    private String userId;
    @Schema(description = "账号名称")
    private String accountName;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "员工编号")
    private String employeeId;
    @Schema(description = "部门名称")
    private String deptName;
    @Schema(description = "部门id")
    private String deptId;
    @Schema(description = "一级部门名称")
    private String firstDeptName;
    @Schema(description = "一级部门编号")
    private String firstDeptCode;
    @Schema(description = "二级部门名称")
    private String secondDeptName;
    @Schema(description = "二级部门编号")
    private String secondDeptCode;
    @Schema(description = "三级部门名称")
    private String thirdDeptName;
    @Schema(description = "三级部门编号")
    private String thirdDeptCode;
    @Schema(description = "角色名称")
    private String roleName;
    @Schema(description = "角色id")
    private String roleId;
    @Schema(description = "操作角色名称")
    private String operationRoleName;
    @Schema(description = "操作角色id")
    private String operationRoleId;
    private String status;
    @Schema(description = "登录次数")
    private long loginCounts;
    @Schema(description = "最后一次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
    @Schema(description = "完成率")
    private Integer completeRate;

    @Schema(description = "联系方式")
    private String contact;
    @Schema(description = "职位")
    private String position;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "办公电话")
    private String officePhone;

    @Schema(description = "家庭电话")
    private String homePhone;
    private String phone;
    @Schema(description = "原声聆听数")
    private Integer originalListenCount;
    @Schema(description = "聆听任务完成率")
    private String listenTaskCompleteRate;
    @Schema(description = "访问时长")
    private String visitDuration;
    @Schema(description = "抱怨原声聆听数")
    private Integer complainOriginalListenCount;
    private LocalDate startTime;
}
