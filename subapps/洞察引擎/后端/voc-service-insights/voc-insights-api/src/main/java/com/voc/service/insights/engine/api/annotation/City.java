package com.voc.service.insights.engine.api.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.voc.service.insights.engine.api.serializers.CitySerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类描述:  字典注解
 * 作    者： dangzhenghui
 * 日    期： 2019年03月17日-下午9:37:16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = CitySerializer.class)
public @interface City {
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
