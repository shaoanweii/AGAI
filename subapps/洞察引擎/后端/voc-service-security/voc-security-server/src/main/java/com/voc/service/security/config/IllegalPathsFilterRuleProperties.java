package com.voc.service.security.config;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2026/1/22 17:35
 * @描述:
 **/
@Configuration
@Component
@ConfigurationProperties(prefix = "security.illegal")
@Data
@RefreshScope
public class IllegalPathsFilterRuleProperties {
    private static final String[] ILLEGAL_PATH = {
            ".*\\\\.(apk|exe|dll|bat|sh|cmd|php|jsp|asp|aspx)$"
    };

    private Set<String> illegalPaths = new HashSet<>();


    @PostConstruct
    public void initIllegalPaths() {
        if(ObjectUtils.isEmpty(illegalPaths)){
            Collections.addAll(illegalPaths, ILLEGAL_PATH);
        }
    }
}
