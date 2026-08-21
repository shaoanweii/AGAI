package com.voc.service.security.impl.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_login_histroy")
public class LoginHistroyEntity implements Serializable {

    private String id;
    @NonNull
    private String appId;
    @NonNull
    private String credentialId;
    @NonNull
    private String userId;
    @NonNull
    private String loginType;
    @NonNull
    @Builder.Default
    private LocalDateTime loginTime = LocalDateTime.now();

    private String tid;
}
