package com.voc.service.analysis.clients;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelServiceClientConfig {

    @Bean
    public feign.Request.Options options() {
        return new feign.Request.Options(10000, 60000*5); // 连接超时10秒，读取超时60秒
    }
}