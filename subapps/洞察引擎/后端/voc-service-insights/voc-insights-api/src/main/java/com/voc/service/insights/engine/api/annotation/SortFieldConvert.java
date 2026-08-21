package com.voc.service.insights.engine.api.annotation;

import java.lang.annotation.*;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/1 09:22
 * @描述:
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SortFieldConvert {
    /**
     * 待转换字段
     * @return
     */
    SortField[] fields();
}
