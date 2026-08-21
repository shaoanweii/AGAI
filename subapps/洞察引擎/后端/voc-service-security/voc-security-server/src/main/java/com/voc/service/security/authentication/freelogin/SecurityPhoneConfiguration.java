package com.voc.service.security.authentication.freelogin;

import com.voc.service.security.impl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


//@Component
public class SecurityPhoneConfiguration extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private static final Logger log = LoggerFactory.getLogger(SecurityPhoneConfiguration.class);
    @Autowired
    FreeAuthenticationHandler authenticationHandler;
    @Autowired
    UserService userDetailsService;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        log.info("--->> init {}", this.getClass().getSimpleName());
        FreeLoginFilter filter = new FreeLoginFilter(httpSecurity.getSharedObject(AuthenticationManager.class), authenticationHandler);
        filter.setAuthenticationManager(httpSecurity.getSharedObject(AuthenticationManager.class));
        FreeAuthenticationProvider authenticationProvider = new FreeAuthenticationProvider(
                httpSecurity.getSharedObject(AuthenticationManager.class), userDetailsService);


        httpSecurity
                .authenticationProvider(authenticationProvider)
                .addFilterAfter(filter, UsernamePasswordAuthenticationFilter.class)  //账号+口令 、 手机号+口令/
        ;
    }


}
