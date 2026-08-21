package com.voc.service.insights.engine.api.clients;


import feign.Logger;
import org.springframework.context.annotation.Bean;

public class InsDataServiceClientConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public feign.Request.Options options() {
        return new feign.Request.Options(120000, 60000*5); // 连接超时10秒，读取超时60秒
    }
}