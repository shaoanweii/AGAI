package com.voc.service.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @Title: JacksonConfig
 * @Package: com.voc.service.common.config
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/22 16:43
 * @Version:1.0
 */
//@Configuration
public class JacksonConfig extends WebMvcConfigurationSupport {
    private static final Logger logger = LoggerFactory.getLogger(JacksonConfig.class);

    /*@Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        converter.setSupportedMediaTypes(List.of(
                org.springframework.http.MediaType.APPLICATION_JSON,
                org.springframework.http.MediaType.TEXT_HTML
        ));
        converters.add(converter);
    }*/
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> messageConverter : converters) {
           logger.info("messageConverter -> {}", messageConverter);
        }
    }
   /* @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
        ObjectMapper objectMapper = FastJsonHttpMessageConverter
                .build();

        // 在这里添加其他的配置
        return objectMapper;
    }*/
}
