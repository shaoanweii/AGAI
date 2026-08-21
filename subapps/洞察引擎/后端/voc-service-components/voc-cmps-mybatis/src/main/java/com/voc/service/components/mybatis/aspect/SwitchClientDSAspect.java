package com.voc.service.components.mybatis.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.components.mybatis.util.ClientMappings;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Title: SwitchDSAspect
 * @Package: com.voc.service.components.mybatis.aspects
 * @Description: 完成数据源的动态切换，根据客户标识对应的数据源key完成动作
 * @Author: cuick
 * @Date: 2024/5/15 11:19
 * @Version:1.0
 */
@Aspect
@Component
//@ConfigurationProperties(prefix = "insights.clients.ds")
public class SwitchClientDSAspect {
    private static final Logger log = LoggerFactory.getLogger(SwitchClientDSAspect.class);
    @Setter
    Map<String, String> mappings = new HashMap<>();
    @Autowired
    private ClientMappings clientMappings;

    public SwitchClientDSAspect() {
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Pointcut("@annotation(com.voc.service.components.mybatis.annotation.SwitchClientDS)")
    public void dsPointCut() {
        log.debug("call.dsPointCut");
    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodInvocationProceedingJoinPoint joinPoint_ = (MethodInvocationProceedingJoinPoint) joinPoint;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        System.out.println();
   //     Arrays.stream(signature.getParameterNames()).forEach(System.out::println);

        Object[] args = joinPoint.getArgs();
        if (ArrayUtil.isEmpty(args)) {
//            throw new RuntimeException("未找到指定的参数");
        }
        Method method = signature.getMethod();
        final SwitchClientDS clientDs = method.getAnnotation(SwitchClientDS.class);
        StringBuilder matchClientValue = null;

        Parameter[] parameters = method.getParameters();
        String[] paramNames = Arrays.stream(parameters).map(Parameter::getName).toArray(String[]::new);
        if (StrUtil.isNotBlank(clientDs.objectAttribute()) && StrUtil.contains(clientDs.objectAttribute(), '.')) {
            final String paramObjName = StrUtil.split(clientDs.objectAttribute(), ".").get(0);
            final String attrName = StrUtil.split(clientDs.objectAttribute(), ".").get(1);

            for (int i = 0; i < args.length; i++) {
                log.debug("参数名称:{},参数值:{}", paramNames[i], args[i]);
                if (paramNames[i].equals(paramObjName) && ObjUtil.isNotEmpty(args[i])) {
                    final Object value = ReflectUtil.getFieldValue(args[i], attrName);
                    Optional.ofNullable(value).orElseThrow(() -> new RuntimeException("值不能为空！"));
                    matchClientValue = new StringBuilder(String.valueOf(value));
                    log.trace("匹配到客户标识:{} ", matchClientValue);
                    break;
                }
            }
        } else {
            final String attrName = clientDs.value();
            for (int i = 0; i < args.length; i++) {
                log.debug("参数名称:{},参数值:{}", paramNames[i], args[i]);
                if (paramNames[i].equals(attrName) && ObjUtil.isNotEmpty(args[i])) {
                    matchClientValue = new StringBuilder(String.valueOf(args[i]));
                    log.trace("匹配到客户标识:{}", matchClientValue);
                    break;
                }
            }
        }
        String datasource = clientDs.datasource();
        if (ObjUtil.isNull(matchClientValue) && StrUtil.isBlank(datasource)) {
            datasource = "默认";
        }
        final String dsKey;
        if(StrUtil.isNotBlank(datasource)){
            dsKey = this.getClientId(datasource);
        }else {
            dsKey = this.getClientId(matchClientValue.toString());
        }

        DynamicDataSourceContextHolder.push(dsKey);
        log.info("切换数据源:{} , client: {}", dsKey, matchClientValue);

        Object result_;
        try {
            result_ = joinPoint.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }

        return result_;
    }

    private String getClientId(String datasource) {
        log.debug("{}, getMappings() -> {}" ,datasource,clientMappings.getMappings());
        if (clientMappings.getMappings().containsKey(datasource)) {
            log.debug("getClientId() -> {}" ,clientMappings.getMappings().get(datasource));
            return clientMappings.getMappings().get(datasource);
        }

        return datasource;
    }
}
