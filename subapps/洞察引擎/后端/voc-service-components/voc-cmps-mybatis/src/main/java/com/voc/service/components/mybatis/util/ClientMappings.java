package com.voc.service.components.mybatis.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/24 上午10:25
 * @描述:
 **/
@Configuration
@Component
@ConfigurationProperties(prefix = "clients.ds")
@Data
public class ClientMappings {
    Map<String, String> mappings = new HashMap<>();
}
