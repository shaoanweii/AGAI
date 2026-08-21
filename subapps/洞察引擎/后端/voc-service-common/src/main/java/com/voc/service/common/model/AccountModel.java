package com.voc.service.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AccountModel
 * @createTime 2024年02月19日 15:52
 * @Copyright futong
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountModel implements Serializable {
    @NonNull
    private String id;
    @NonNull
    //sys_user_id
    private String userId;
    @NonNull
    //phone、weixin、base
    private String identityType;
    @NonNull
    //例如：手机号 ,unionid
    private String identifier;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startExpireDate;
    private String operator;
    private String appId;
    private String username;
    private String password;
    @Builder.Default
    private boolean admin = false;
    @Builder.Default
    // 默认过期
    private boolean nonExpired = false;
    @Builder.Default
    // 默认锁定
    private boolean nonLocked = false;
    @Builder.Default
    // 默认禁用
    private boolean enabled = false;
    //账号累积登录次数
    private long loginCounts;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    //账号最后一次登录时间，精确到秒
    private LocalDateTime lastLoginTime;
}
