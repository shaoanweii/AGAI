package com.voc.service.insights.engine.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/13 上午9:58
 * @描述:
 **/
@Configuration
@Component
@Data
public class DynamicParameterMapping {
    @Value("${batchPushNumber:10}")
    private Integer batchPushNumber;
    @Value("${jsonDataSize:1000012}")
    private Long jsonDataSize;
}
