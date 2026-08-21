package com.voc.service.components.mybatis.annotation;

import java.lang.annotation.*;

/**
 * @Title: SwitchDS
 * @Package: com.voc.service.components.mybatis.annotation
 * @Description: objectAttribute属性配置优先级高于value值
 * @Author: cuick
 * @Date: 2024/5/15 11:17
 * @Version:1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SwitchClientDS {
    //客户标识函数名
    String value() default "clientId";

    String datasource() default "";

    //model.clientId
    String objectAttribute() default "";
}
