package com.voc.service.security.impl.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/31 上午10:13
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_credentials_change_record")
@ToString(exclude = {"credential"})
public class CredentialsChangeRecordEntity implements Serializable {
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
    // 账号变更时间
    private LocalDateTime changeTime;

}
