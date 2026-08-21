package com.voc.service.insights.engine.common.annotation;

import com.voc.service.insights.engine.common.enums.PermissionsType;

import java.lang.annotation.*;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/29 11:28
 * @描述:
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permissions {
    /**
     * 读或写的类型
     * @return
     */
    PermissionsType type();
}
