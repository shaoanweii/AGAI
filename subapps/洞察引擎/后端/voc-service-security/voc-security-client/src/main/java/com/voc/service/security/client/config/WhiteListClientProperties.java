package com.voc.service.security.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Token属性
 *
 * @author w
 */
@Data
@Configuration
@ConfigurationProperties(WhiteListClientProperties.PREFIX)
public class WhiteListClientProperties {
    /**
     * 前缀
     */
    public static final String PREFIX = "security.ignoring";

    /**
     * 认证中心默认忽略验证地址
     */
    private static final String[] SECURITY_ENDPOINTS = {
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/actuator/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/instances",
            "/actuator",
            "/actuator/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            /* 本地联调添加统一前缀 server.servlet.context-path: /api/insights等  */
            "/api/*/v2/api-docs",
            "/api/*/v3/api-docs",
            "/api/*/v3/api-docs/**",
            "/api/*/actuator/**",
            "/api/*/swagger-resources",
            "/api/*/swagger-resources/**",
            "/api/*/configuration/ui",
            "/api/*/configuration/security",
            "/api/*/instances",
            "/api/*/actuator",
            "/api/*/actuator/**",
            "/api/*/swagger-ui/**",
            "/api/*/webjars/**",
            "/api/*/favicon.ico",
            "/api/*/doc.html",
            "/api/*/swagger-ui.html"
    };
    /**
     * 自定义需要忽略的url
     */
    private Set<String> urls = new HashSet<>();

    /**
     * 是否开启token验证
     */
    private boolean enable = Boolean.TRUE;

    /**
     * 自定义需要忽略的token
     */
    private Set<String> tokens = new HashSet<>();

    /**
     * 首次加载合并ENDPOINTS
     */
    @PostConstruct
    public void initIgnoreSecurity() {
        Collections.addAll(urls, SECURITY_ENDPOINTS);
    }
}
