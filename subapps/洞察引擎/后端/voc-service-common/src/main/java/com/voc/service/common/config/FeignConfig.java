package com.voc.service.common.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.common.util.ServiceContextHolder;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.Optional;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName FeignConfig
 * @Description ckcui
 * @createTime 2023年10月09日 11:32
 * @Copyright futong
 */
@Configuration
@ConditionalOnClass(Feign.class)
@AutoConfigureBefore(FeignAutoConfiguration.class)
@EnableFeignClients(basePackages = "com.voc")
@ConfigurationProperties("feign.client")
public class FeignConfig implements RequestInterceptor {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FeignConfig.class);

    public static final String BEARER_TYPE = "Bearer";

    /**
     * 自定义需要忽略的url
     */
    @Setter
    Set<String> token_ignore_urls = CollUtil.newHashSet() ;


    public FeignConfig() {
        logger.info("--->> init {}", this.getClass().getSimpleName());
    }



    /**
     * feign 日志记录级别
     * NONE：无日志记录（默认）
     * BASIC：只记录请求方法和 url 以及响应状态代码和执行时间。
     * HEADERS：记录请求和响应头的基本信息。
     * FULL：记录请求和响应的头、正文和元数据。
     *
     * @return Logger.Level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
//        return Logger.Level.BASIC;
        return Logger.Level.FULL;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {

        final String token = ServiceContextHolder.getToken();
        logger.trace("Feign.token ->{}", token);
        //添加token
        if (StrUtil.isNotBlank(token) &&  !this.match(token_ignore_urls, requestTemplate.url())) {
            requestTemplate.header("Authorization", BEARER_TYPE.concat(" ").concat(token));
        }
//        TraceContext.putCorrelation()
        final String tid = ServiceContextHolder.traceId();
        if( Optional.ofNullable(tid).isPresent()){
            logger.debug("tid:{}",tid);
            requestTemplate.header("tid", tid);
        }

        final String appCode = ServiceContextHolder.getAppCode();
        if(ObjectUtils.isNotEmpty(appCode)){
            logger.trace("appCode:{}", appCode);
            requestTemplate.header("apim-appcode-key", appCode);
        }
    }

    private boolean match(Set<String> pathList, String uri) {
        return pathList.stream().anyMatch(path -> new AntPathMatcher().match(path, uri));
    }


}
