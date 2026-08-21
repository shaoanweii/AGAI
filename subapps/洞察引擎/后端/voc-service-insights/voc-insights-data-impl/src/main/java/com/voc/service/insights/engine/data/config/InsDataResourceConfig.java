package com.voc.service.insights.engine.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据资源配置
 */
@Data
@Configuration
@Component
@ConfigurationProperties(prefix = "ins.data.resource")
@RefreshScope
public class InsDataResourceConfig {

    private Set<String> forbiddenDeletionIds = new HashSet<>();
}
