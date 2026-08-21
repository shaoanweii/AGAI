package com.voc.service.security.impl.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_credentials")
@ToString(exclude = {"credential"})
public class CredentialsEntity implements Serializable {
    @NonNull
    private String id;
    @NonNull
    private String userId;  //sys_user_id
    @NonNull
    private String identityType;  //phone、weixin、base
    @NonNull
    private String identifier;  //例如：手机号 ,unionid
    private String credential;  //密码
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime expireDate;
    private LocalDateTime startExpireDate;
    private String operator;
    private String appId;
    @Builder.Default
    private boolean admin= false;
    @Builder.Default
    private boolean nonExpired = false;  // 默认过期
    @Builder.Default
    private boolean nonLocked = false;      // 默认锁定
    @Builder.Default
    private boolean enabled = false;    // 默认禁用
    @TableField(exist = false)
    private long loginCounts;  //账号累积登录次数
    @TableField(exist = false)
    private LocalDateTime lastLoginTime; //账号最后一次登录时间，精确到秒

}
