package com.voc.service.components.sentinel.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelAopConfig {
    private static final Logger log = LoggerFactory.getLogger(SentinelAopConfig.class);

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        log.info("sentinelResourceAspect 加载成功");
        return new SentinelResourceAspect();
    }
}
