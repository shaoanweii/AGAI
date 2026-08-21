package com.voc.service.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.voc.service.common.model.auth.PermissionModel;
import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName UserModel
 * @Description ckcui
 * @createTime 2023年10月19日 10:00
 * @Copyright futong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(callSuper = false)
public class UserModel extends Page implements Serializable {
    //用户再系统内的权限数据
    @JsonIgnore
    @Builder.Default
    protected PermissionModel systemPermissions = PermissionModel.builder().build();
    @JsonIgnore
    @Builder.Default
    protected PermissionModel businessPermissions = PermissionModel.builder().build();
    @JsonIgnore
    @Builder.Default
    protected PermissionModel accessPermissions = PermissionModel.builder().build();
    //账号ID
    protected String id;
    //用户信息ID
    protected String userId;
    protected String firstname;
    protected String lastname;
    protected String email;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime expireDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime startExpireDate;
    @JsonIgnore
    protected String token;
    protected String tokenKey;
    protected String appName;
    protected String operator;
    @Builder.Default
    // 默认过期
    protected boolean nonExpired = false;
    // 默认禁用
    @Builder.Default
    protected boolean enabled = false;
    protected String status;
    protected String phone;
    protected String username;
    //    @JsonIgnore
    protected String password;
    protected String smscode;
    //    @JsonIgnore
    protected String captcha;
    //    @JsonIgnore
    protected String checkKey;
    protected String uniconId;
    protected String appId;
    //    @Builder.Default
    protected String admin;
    protected String clientId;
    protected String type;
    protected String labelstudToken;
    @Builder.Default
    private boolean nonLocked = false;      // 默认锁定
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime loginTime;
    @Builder.Default
    protected List<AccountModel> accounts = new ArrayList<>();

    protected List<String> clientIds;//用于多选条件判断
    //员工编号
    protected String employeeId;
    //职位
    protected String position;
    //备注
    protected String remark;

    @Schema(description = "办公电话")
    private String officePhone;

    @Schema(description = "家庭电话")
    private String homePhone;
    protected List<String> userIds;
    protected List<String> userNameList;
    @Schema(description = "cmp中的userId（业务系统发起流程使用）")
    protected String cmpId;
    @Schema(description = "完成率")
    private Integer loginCompleteRate;

    private LocalDate startTime;
    private LocalDate endTime;
    private String taskId;
    private String fileName;
    private String fileType;

}
