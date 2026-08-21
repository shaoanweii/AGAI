package com.voc.service.security.authentication.phone;

import com.voc.service.security.impl.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
public class PhoneAuthenticationProvider extends DaoAuthenticationProvider {
    public PhoneAuthenticationProvider(AuthenticationManager authenticationManager, UserService userDetailsService) {
        this.setUserDetailsService(userDetailsService);

        ((ProviderManager) authenticationManager).getProviders().add(this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        return super.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (PhoneAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
