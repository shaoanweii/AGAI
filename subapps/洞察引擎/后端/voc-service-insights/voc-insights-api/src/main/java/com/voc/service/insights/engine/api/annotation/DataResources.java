package com.voc.service.insights.engine.api.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.voc.service.insights.engine.api.serializers.DataResourcesSerializer;

import java.lang.annotation.*;

/**
 * 资源组
 *
 * @author lww
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@JacksonAnnotationsInside
@JsonSerialize(using = DataResourcesSerializer.class)
public @interface DataResources {

    String code() default "";

    String defaultText() default "";
}
