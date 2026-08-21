package com.voc.service.insights.engine.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/1 09:23
 * @描述:
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface SortField {
    /**
     * 原字段
     * @return
     */
    String source();

    /**
     * 目标字段
     * @return
     */
    String targer();
}
