package com.voc.service.security.authentication.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class EmailAuthenticationProvider extends DaoAuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(EmailAuthenticationProvider.class);

    public EmailAuthenticationProvider(AuthenticationManager authenticationManager,
                                       UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.setUserDetailsService(userDetailsService);
        this.setPasswordEncoder(passwordEncoder);

        ((ProviderManager) authenticationManager).getProviders().add(this);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.trace("authenticate");
        return super.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (EmailAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
