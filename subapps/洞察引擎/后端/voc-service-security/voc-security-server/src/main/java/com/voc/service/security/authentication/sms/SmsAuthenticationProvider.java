package com.voc.service.security.authentication.sms;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.security.impl.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseAuthenticationProvider
 * @Description ckcui
 * @createTime 2023年12月01日 11:54
 * @Copyright futong
 */
@Component
public class SmsAuthenticationProvider extends DaoAuthenticationProvider {
    public SmsAuthenticationProvider(AuthenticationManager authenticationManager, UserService userDetailsService) {
        this.setUserDetailsService(userDetailsService);
        this.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        ((ProviderManager) authenticationManager).getProviders().add(this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken smsCodeAuthenticationToken = (SmsAuthenticationToken)authentication;

        UserDetails user = ((UserService)this.getUserDetailsService()).loadUserByMobile((String)smsCodeAuthenticationToken.getPrincipal());
        if (user == null) {
            throw new InternalAuthenticationServiceException("无法获取用户信息");
        }
        if(!StrUtil.equals(String.valueOf(authentication.getCredentials()), String.valueOf(user.getPassword()))){
            throw new BadCredentialsException(CommonErrorEnum.SMS_VERIFICATION_EXECPTION.getMessage());
        }

        SmsAuthenticationToken result = new SmsAuthenticationToken(user, user.getAuthorities());
        result.setDetails(smsCodeAuthenticationToken.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (SmsAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
