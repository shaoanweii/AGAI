package com.voc.service.insights.engine.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2026/3/23
 * @描述:
 **/
@Data
@Configuration
@Component
@ConfigurationProperties(prefix = "ins.cqca.raw-data")
public class InsCqCaRawDataConfig {

    private Map<String, String> channelCodeMap = new HashMap<>();

    private Map<String, String> resultChannelCodeMap = new HashMap<>();
}
