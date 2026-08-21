package com.voc.service.security.authentication.formbase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


//@Component
public class SecurityBaseConfiguration extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private static final Logger log = LoggerFactory.getLogger(SecurityBaseConfiguration.class);
    @Autowired
    BaseAuthenticationHandler baseAuthenticationHandler;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserDetailsService userDetailsService;

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        log.info("--->> init {}", this.getClass().getSimpleName());
        BaseLoginFilter filter = new BaseLoginFilter(httpSecurity.getSharedObject(AuthenticationManager.class), baseAuthenticationHandler);
        filter.setAuthenticationManager(httpSecurity.getSharedObject(AuthenticationManager.class));
        BaseAuthenticationProvider baseAuthenticationProvider = new BaseAuthenticationProvider(httpSecurity.getSharedObject(AuthenticationManager.class),
                userDetailsService, passwordEncoder);

        httpSecurity
                .authenticationProvider(baseAuthenticationProvider)
                .addFilterAfter(filter, UsernamePasswordAuthenticationFilter.class)  //账号+口令 、 手机号+口令/
        ;
    }
}
