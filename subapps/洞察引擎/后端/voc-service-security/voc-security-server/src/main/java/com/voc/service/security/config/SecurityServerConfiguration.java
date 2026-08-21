package com.voc.service.security.config;

import com.voc.service.security.logout.JwtLogoutHandler;
import com.voc.service.security.logout.JwtLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableScheduling
public class SecurityServerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SecurityServerConfiguration.class);
    @Autowired
    WhiteListServerProperties whiteListServerProperties;
    @Autowired
    JwtAuthenticationServerFilter jwtAuthenticationServerFilter;
    @Autowired
    JwtLogoutHandler jwtLogoutHandler;
    @Autowired
    JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    /*@Autowired
    SecurityBaseConfiguration securityBaseConfiguration;
    @Autowired
    SecuritySmsConfiguration securitySmsConfiguration;*/

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity
            , AuthenticationManager authenticationManager) throws Exception {
//    @Override
//    public void configure(HttpSecurity httpSecurity) throws Exception {

        log.info("--->> init {}", this.getClass().getSimpleName());

        if (whiteListServerProperties.isEnable()) {
            whiteListServerProperties.getUrls().stream().forEach(e -> {
                log.info("ignore:{}", e);
            });
        }

        final RequestMatcher[] matchers = whiteListServerProperties.getUrls().stream().map(uri -> AntPathRequestMatcher.antMatcher(uri))
                .toArray(AntPathRequestMatcher[]::new);
        // 配置拦截规则
        httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> {
            authorizeHttpRequests
//                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(matchers).permitAll()
                    .anyRequest().fullyAuthenticated();
        });
        // 禁用默认的登录和退出
        httpSecurity.formLogin(AbstractHttpConfigurer::disable);
//        httpSecurity.logout(AbstractHttpConfigurer::disable);
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        // 添加过滤器
        httpSecurity
                /*.formLogin(conf->{
                    // 自定义表单登录页
                    // https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html
                    conf.loginPage("/v1/auth/login");
                    // 表单登录请求
                    conf.loginProcessingUrl("/v1/auth/login");
                    // 登录成功处理器，取消defaultSuccessUrl默认登录成功页可以看到效果，如登录失败处理器类似
                    // 使用handler类
                    conf.successHandler(baseAuthenticationHandler);
                    conf.failureHandler(baseAuthenticationHandler);
                    // 默认登录成功页，使用了handler，就不要使用默认登录页，否则handler不起作用
                    // conf.defaultSuccessUrl("/home");
                    // 登录相关请求不需要认证
                    conf.permitAll();
                })*/
                .logout(conf -> {
                    conf.addLogoutHandler(jwtLogoutHandler);
                    // 登出请求
                    conf.logoutUrl("/auth/logout");
//                    conf.logoutSuccessUrl("/login");
                    conf.permitAll();
                    conf.logoutSuccessHandler(jwtLogoutSuccessHandler);
                    // 登出后清楚请求的上下文，也就是用户信息
                })
//                .csrf(conf -> conf.ignoringRequestMatchers("/druid/**") )
                .headers(conf -> conf.contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterAt(jwtAuthenticationServerFilter, UsernamePasswordAuthenticationFilter.class)
               /* .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(baseAuthenticationHandler)
                                .accessDeniedHandler(baseAuthenticationHandler))*/

//                .addFilterBefore(new BaseLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(new SmsLoginFilter(authenticationManager), UsernamePasswordAuthenticationFilter.class)
//                .apply(securityBaseConfiguration()).and()
//                .apply(securitySmsConfiguration())
        ;

        return httpSecurity.build();
    }

    /*@Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        final RequestMatcher[] matchers = whiteListServerProperties.getUrls().stream().map(uri -> AntPathRequestMatcher.antMatcher(uri))
                .toArray(AntPathRequestMatcher[]::new);

        return (web) -> web.ignoring()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .requestMatchers(matchers)
                ;
    }*/

   /* @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }*/

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    /*public AuthenticationManager authenticationManager(
            BaseAuthenticationProvider baseAuthenticationProvider,
            PhoneAuthenticationProvider phoneAuthenticationProvider,
            SmsAuthenticationProvider smsAuthenticationProvider
    ) throws Exception {
        ProviderManager providerManager =
                new ProviderManager(Collections.synchronizedList(
                        Arrays.asList(baseAuthenticationProvider, phoneAuthenticationProvider, smsAuthenticationProvider)
                ));
        return providerManager;
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return MD5PasswordEncoder.getInstance();
        return NoOpPasswordEncoder.getInstance();
    }

    /*@Autowired
    MyUserDetailsService userDetailsService;
//    @Bean("securityBaseConfiguration")
    public SecurityBaseConfiguration securityBaseConfiguration() {
        return new SecurityBaseConfiguration(baseAuthenticationHandler,md5PasswordEncoder(),userDetailsService);
    }

//    @Bean("securitySmsConfiguration")
    public SecuritySmsConfiguration securitySmsConfiguration() {
        return new SecuritySmsConfiguration();
    }*/
    /*@Bean
    public AuthenticationProvider authenticationProvider(MyUserDetailsService userService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
//        authProvider.setPasswordEncoder(defaultPasswordEncoder);
        return authProvider;
    }*/

}
