package com.voc.service.components.sentinel.config;

import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 全局异常处理器
 * 处理限流等 Sentinel 异常
 */
@Configuration
public class SentinelGlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(SentinelGlobalExceptionHandler.class);

    @Bean
    public BlockExceptionHandler blockExceptionHandler() {
        return (request, response, e) -> {
            log.warn("⚠️ Sentinel 限流触发: resource={}, rule={}, limitApp={}",
                    e.getRuleLimitApp(), e.getRule(), e.getRuleLimitApp());
            // 自定义处理
            response.setStatus(429);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSONUtil.toJsonStr(
                    Result.error(CommonErrorEnum.RATE_LIMIT_EXCEEDED.getCode(),
                            CommonErrorEnum.RATE_LIMIT_EXCEEDED.getMessage())
            ));
        };
    }
}