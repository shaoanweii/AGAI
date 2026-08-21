package com.voc.service.security.authentication.freelogin;

import cn.hutool.core.util.ObjectUtil;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.security.api.ISSOService;
import com.voc.service.security.impl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Optional;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseAuthenticationProvider
 * @Description ckcui
 * @createTime 2023年12月01日 11:54
 * @Copyright futong
 */
@Component
public class FreeAuthenticationProvider extends DaoAuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(FreeAuthenticationProvider.class);
    @Autowired
    ISSOService ssoService;
    public FreeAuthenticationProvider(AuthenticationManager authenticationManager, UserService userDetailsService) {
        this.setUserDetailsService(userDetailsService);
        this.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        ((ProviderManager) authenticationManager).getProviders().add(this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        FreeAuthenticationToken authenticationToken = (FreeAuthenticationToken) authentication;

        //解密
        log.info("credentials:{}", authenticationToken.getCredentials());
        Optional.ofNullable(ssoService.decrypt(String.valueOf(authentication.getPrincipal())
                        , String.valueOf(authentication.getCredentials())))
                .filter(rs -> rs.equals(Boolean.TRUE))
                .orElseThrow(() -> new BadCredentialsException(CommonErrorEnum.SSO_LOGIN_EXECPTION.getMessage()));
        log.info("解析成功");
        UserDetails user = ((UserService) this.getUserDetailsService()).loadUserByFree((String) authenticationToken.getPrincipal());
        if (ObjectUtil.isNull(user) ) {
            throw new InternalAuthenticationServiceException("无法获取用户信息");
        }

        FreeAuthenticationToken result = new FreeAuthenticationToken(user, user.getAuthorities());
        result.setDetails(authenticationToken.getDetails());
        return result;
    }




    @Override
    public boolean supports(Class<?> authentication) {
        return (FreeAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
