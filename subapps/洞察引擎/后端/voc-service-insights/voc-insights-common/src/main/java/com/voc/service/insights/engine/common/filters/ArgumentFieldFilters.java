package com.voc.service.insights.engine.common.filters;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/1 12:15
 * @描述:
 **/
@RestControllerAdvice
public class ArgumentFieldFilters implements RequestBodyAdvice {

    private static final Logger log = LoggerFactory.getLogger(ArgumentFieldFilters.class);

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> bodyClass = body.getClass();
        final boolean admin = ServiceContextHolder.isAdmin();
        final String appId = ServiceContextHolder.getAppId();
        AtomicReference<Field> fieldReference = new AtomicReference<>(null);
        while (ObjectUtils.isNotEmpty(bodyClass)){
            Field field = Arrays.stream(bodyClass.getDeclaredFields()).filter(e -> "clientId".equalsIgnoreCase(e.getName())||"customer".equalsIgnoreCase(e.getName())||"appClient".equalsIgnoreCase(e.getName())).findFirst().orElse(null);
            if(ObjectUtils.isNotEmpty(field)){
                fieldReference.set(field);
                break;
            }else {
                bodyClass = bodyClass.getSuperclass();
            }
        }
        Field clientField = fieldReference.get();
//        if(ObjectUtils.isNotEmpty(clientField)&&"insights".equalsIgnoreCase(appId)&& !Objects.equals(List.class, clientField.getType())){
        if(ObjectUtils.isNotEmpty(clientField)&& !Objects.equals(List.class, clientField.getType())){
            if(!admin){
                try {
                    clientField.setAccessible(true);
                    Object o = clientField.get(body);
                    if(ObjectUtils.isEmpty(o)){
                        clientField.set(body, ServiceContextHolder.getClientId());
                    }
                } catch (IllegalAccessException e) {
                    log.error("设置客户信息失败",e);
                }
//                try {
//                    clientField.setAccessible(true);
//                    clientField.set(body, ServiceContextHolder.getClientId());
//                } catch (IllegalAccessException e) {
//                    log.error("设置客户信息失败",e);
//                }
            }else {
                try {
                    UserModel user = ServiceContextHolder.getUser();
                    clientField.setAccessible(true);
                    clientField.set(body, ServiceContextHolder.getClientId());
                    Object o = clientField.get(body);
                    user.setClientId(String.valueOf(o));
                    ServiceContextHolder.setUser(user);
                } catch (IllegalAccessException e) {
                    log.error("设置客户信息失败",e);
                }
            }
        }else {
            log.debug("当前对象中没有客户信息,不做转换");
        }
        //获取注解，并改写其中的值
        SortFieldConvert annotation = body.getClass().getAnnotation(SortFieldConvert.class);
        AtomicReference<Field> fieldAtomicReference = new AtomicReference<>(null);
        Class<?> aClass = body.getClass();
        if(ObjectUtils.isNotEmpty(annotation)){
            while (ObjectUtils.isNotEmpty(aClass)){
                Field field = Arrays.stream(aClass.getDeclaredFields()).filter(e -> "order".equalsIgnoreCase(e.getName())).findFirst().orElse(null);
                if(ObjectUtils.isNotEmpty(field)){
                    fieldAtomicReference.set(field);
                    break;
                }else {
                    aClass = aClass.getSuperclass();
                }
            }
            Field field = fieldAtomicReference.get();
            if(ObjectUtils.isEmpty(field)){
                log.debug("当前对象中没有排序字段,不做转换");
            }else {
                try {
                    field.setAccessible(true);
                    Object o = field.get(body);
                    AtomicReference<String> value = new AtomicReference<>(String.valueOf(o));
                    if(ObjectUtils.isEmpty(o)||StrUtil.isBlank(value.get())){
                        log.debug("不做排序，放行");
                    }else {
                        SortField[] fields = annotation.fields();
                        Arrays.stream(fields).forEach(e->{
                            String s = value.get();
                            String replace = null;
                            if(StrUtil.isNotBlank(s)){
                                if(s.toLowerCase().contains("drop")||s.toLowerCase().contains("delete")||s.toLowerCase().contains("truncate")){
                                    log.warn("排序字段中存在异常值,将排序字段值置空");
                                }else {
                                    replace = value.get().replace(e.source(), e.targer());
                                }
                                try {
                                    field.set(body,replace);
                                } catch (IllegalAccessException ex) {
                                    log.error("字段转换失败，使用原值");
                                }
                                value.set(replace);
                            }
                        });
                    }
                } catch (IllegalAccessException e) {
                    log.error("字段转化失败，使用原值");
                }
            }
        }
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
