package com.voc.service.logs.annotation;
import java.lang.annotation.*;

/**
 * 内部接口调用日志注解
 * 用于记录 Feign Client 或内部方法调用的详细信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApiLog {

    /**
     * 业务描述
     */
    String value() default "";
}
