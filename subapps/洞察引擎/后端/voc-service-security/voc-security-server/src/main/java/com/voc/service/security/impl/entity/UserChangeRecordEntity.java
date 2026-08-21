package com.voc.service.security.impl.entity;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.voc.service.security.api.ICredentialsService;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_users_change_record")
@ToString(exclude = {"password"})
public class UserChangeRecordEntity implements UserDetails {

    protected String username;
    @TableField(exist = false)
    protected String password;
    @Builder.Default
    @TableField(exist = false)
    protected boolean nonExpired = false;  // 默认过期
    @Builder.Default
//    @TableField(exist = false)
    protected boolean nonLocked = false;      // 默认锁定
    @Builder.Default
    protected boolean enabled = false;    // 默认禁用
    private String id;
    @TableField(exist = false)
    private String userId;
    private String phone;
    private String firstname;
    private String lastname;
    private String email;
    private String clientId;   //客户标识
    @TableField(exist = false)
    private String identifier;  //系统登陆账号
    @TableField(exist = false)
    private String identityType;  //phone、weixin、base
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime expireDate;
    private LocalDateTime startExpireDate;
    private String operator;
    private String labelstudToken;
    private String employeeId;

    private LocalDateTime changeTime;


    @Override
    public boolean isAccountNonExpired() {
        return this.nonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.nonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.nonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return null;
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getIdentifier() {
        if (StrUtil.isBlank(identityType)) {
            return null;
        }
        switch (identityType) {
            case ICredentialsService.IDENTITY_TYPE_BASE:
                return getUsername();
            case ICredentialsService.IDENTITY_TYPE_PHONE:
                return getPhone();
            case ICredentialsService.IDENTITY_TYPE_PHONE_SMS:
                return getPhone();
            case ICredentialsService.IDENTITY_TYPE_EMAIL:
                return getEmail();
            default:
                return null;
        }
    }
}
