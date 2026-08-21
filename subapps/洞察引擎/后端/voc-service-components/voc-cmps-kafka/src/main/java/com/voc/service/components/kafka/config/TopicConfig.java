package com.voc.service.components.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/3 下午2:48
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "topics")
public class TopicConfig {
    @Value("${spring.application.name:}")
    public String applicationName;
    @Builder.Default
    public int partitions = 1;
    boolean deleteFrist;
    @Builder.Default
    public Set<String> topicList = new HashSet<>();
}
