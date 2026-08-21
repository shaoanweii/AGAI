package com.voc.service.security.config;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/11 下午5:21
 * @描述:
 **/

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Configuration
@ConfigurationProperties(WhiteClientServerProperties.PREFIX)
@RefreshScope
public class WhiteClientServerProperties {

    /**
     * 前缀
     */
    public static final String PREFIX = "security.client";

    /**
     * 自定义需要忽略的url
     */
    private Set<String> ids = new HashSet<>();


    @PostConstruct
    public void initIgnoreSecurity() {
        ids = ids.stream().map(StrUtil::trim).collect(Collectors.toSet());
    }
}
