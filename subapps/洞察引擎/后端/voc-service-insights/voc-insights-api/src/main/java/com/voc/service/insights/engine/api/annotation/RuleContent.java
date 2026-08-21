package com.voc.service.insights.engine.api.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.voc.service.insights.engine.api.serializers.RuleContentTypeSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/15 11:15
 * @描述:
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@JacksonAnnotationsInside
@JsonSerialize(using = RuleContentTypeSerializer.class)
public @interface RuleContent {
    /**
     * 方法描述:  数据code
     * 作    者： dangzhenghui
     * 日    期： 2019年03月17日-下午9:37:16
     *
     * @return 返回类型： String
     */
    String code()default "";

    /**
     * 方法描述:  数据Text
     * 作    者： dangzhenghui
     * 日    期： 2019年03月17日-下午9:37:16
     *
     * @return 返回类型： String
     */
    String defaultText() default "";
}
