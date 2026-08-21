package com.voc.service.security.config;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Token属性
 *
 * @author w
 */
@Data
@Configuration
@ConfigurationProperties(WhiteListServerProperties.PREFIX)
@RefreshScope
public class WhiteListServerProperties {
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
            "/swagger-ui.html"};
    /**
     * 自定义需要忽略的url
     */
    private Set<String> urls = new HashSet<>();

    private Set<String> tokens = new HashSet<>();

    /**
     * 是否开启token验证
     */
    private boolean enable = Boolean.TRUE;

    /**
     * 首次加载合并ENDPOINTS
     */
    @PostConstruct
    public void initIgnoreSecurity() {
        Collections.addAll(urls, SECURITY_ENDPOINTS);
        urls = urls.stream().map(StrUtil::trim).collect(Collectors.toSet());
    }
}
