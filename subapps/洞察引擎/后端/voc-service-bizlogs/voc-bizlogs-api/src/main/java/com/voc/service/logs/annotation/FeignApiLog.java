package com.voc.service.logs.annotation;

import java.lang.annotation.*;

/**
 * FeignClient 调用日志注解
 * 用于标记需要记录调用日志的 FeignClient 接口或方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignApiLog {

    /**
     * 业务描述
     */
    String value() default "";

    /**
     * 是否记录详细参数
     */
    boolean logParams() default false;

    /**
     * 是否记录返回结果
     */
    boolean logResult() default false;
}