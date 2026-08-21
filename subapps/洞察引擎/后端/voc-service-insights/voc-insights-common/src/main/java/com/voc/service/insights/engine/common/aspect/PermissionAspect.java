package com.voc.service.insights.engine.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.common.annotation.Permissions;
import com.voc.service.insights.engine.common.enums.PermissionsType;
import com.voc.service.insights.engine.common.util.PermissionContextHolder;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/29 11:32
 * @描述:
 **/
@Component
@Aspect
public class PermissionAspect {


    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    private static Permissions getAnnotation(JoinPoint joinPoint){
        joinPoint.getTarget().getClass().getAnnotations();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(Permissions.class);
        }
        return null;
    }
    public PermissionAspect() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }
    @Pointcut("@annotation(com.voc.service.insights.engine.common.annotation.Permissions)")
    public void PermissionsPointCut() {
        log.debug("call.{}", Permissions.class.getSimpleName());
    }

    @Before(value = "PermissionsPointCut()")
    public void checkPermissions(JoinPoint joinPoint){
        Permissions annotation = this.getAnnotation(joinPoint);
        //获取注解中的类型值
        final String type = annotation.type().getType();
        //获取请求URI
        final String requestURI = ServiceContextHolder.getRequest().getRequestURI();
        //获取当前用户API读写权限
        final Map<String, String> apiPerms = PermissionContextHolder.getApiPerms();
        if(ObjectUtils.isEmpty(apiPerms)){
            throw new BussinessException("没有访问权限");
        }

        String permission = "";
        if(apiPerms.containsKey(requestURI)){
            //权限精确到URL上
            permission = apiPerms.get(requestURI);
        }else {
            //权限只精确到URI上
            String url = "/uri/**";
            String[] split = requestURI.split("/");
            String uri;
            if(split.length>1&&ObjectUtils.isNotEmpty(split[1])){
                 uri = url.replace("uri",split[1]);
                 permission = apiPerms.get(uri);
            }else {
                if(ObjectUtils.isEmpty(split[0])){
                    //requestURI 只包含根路径 '/'
                    permission = apiPerms.get(requestURI);
                }
            }
        }

        if(StrUtil.isBlank(permission)){
            throw new BussinessException("没有访问权限");
        }

        if(PermissionsType.READ.getType().equalsIgnoreCase(type)){
            if(permission.contains(type)){
                log.debug("允许访问");
            }else {
                throw new BussinessException("没有查看权限");
            }
        }else if(PermissionsType.WRITE.getType().equalsIgnoreCase(type)){
            if(permission.contains(type)){
                log.debug("允许访问");
            }else {
                throw new BussinessException("没有编辑权限");
            }
        }else {
            log.error("未知类型");
            throw new BussinessException("没有访问权限");
        }
    }
}
