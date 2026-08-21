package com.voc.service.security.model;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"credential"})
public class CredentialsModel implements Serializable {
    private String id;
    private String userId;  //sys_user_id
    private String identityType;  //phone、weixin、base
    private String identifier;  //例如：手机号 ,unionid
    private String credential;  //密码
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime expireDate;
    private LocalDateTime startExpireDate;
    private String operator;
    private String appId;
    @Builder.Default
    private boolean admin = false;
    @Builder.Default
    private boolean nonExpired = false;  // 默认过期
    @Builder.Default
    private boolean nonLocked = false;      // 默认锁定
    @Builder.Default
    private boolean enabled = false;    // 默认禁用

}
