package com.voc.service.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName TImeConfig
 * @createTime 2024年03月05日 17:49
 * @Copyright futong
 */
@Configuration
public class TImeConfig {
    private static final Logger logger = LoggerFactory.getLogger(TImeConfig.class);
    @PostConstruct
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Asia/Shanghai")));
        logger.info("--->> {} {}", this.getClass().getSimpleName(),java.util.TimeZone.getDefault().getID());
    }
}
