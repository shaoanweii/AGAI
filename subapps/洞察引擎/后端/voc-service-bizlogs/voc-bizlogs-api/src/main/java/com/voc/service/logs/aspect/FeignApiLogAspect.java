package com.voc.service.logs.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.logs.annotation.FeignApiLog;
import com.voc.service.logs.model.OpsLogModel;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * FeignClient 调用日志切面
 * 记录 Feign Client 接口调用的详细信息：方法名、URI、耗时、HTTP状态码、消息等
 * <p>
 * 使用方式：
 * 1. 在 FeignClient 接口上添加 @FeignApiLog 注解，记录该接口所有方法
 * 2. 在具体方法上添加 @FeignApiLog 注解，只记录该方法
 */
@Aspect
@Component
public class FeignApiLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(FeignApiLogAspect.class);

    private static final int SUCCESS_HTTP_CODE = 200;

    /**
     * ThreadLocal 存储 HTTP 状态码
     */
    private static final ThreadLocal<Integer> HTTP_STATUS_CODE = new ThreadLocal<>();

    /**
     * ThreadLocal 存储错误消息
     */
    private static final ThreadLocal<String> ERROR_MESSAGE = new ThreadLocal<>();

    /**
     * 保存 Feign 调用日志信息
     */
    private void saveFeignLog(ProceedingJoinPoint joinPoint, OpsLogModel sysLog,
                              FeignApiLog apiLog) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取目标类和方法信息
        // 对于 FeignClient，需要从签名中获取实际的接口类
        String interfaceName = getFeignClientInterfaceName(joinPoint);
        String methodName = signature.getName();

        // 设置方法名：接口全限定名.方法名()
        sysLog.setMethod(interfaceName + "." + methodName + "()");

        // 从 FeignClient 注解中获取服务名称
//        String serviceId = extractServiceId(joinPoint);
        sysLog.setAppId(ServiceContextHolder.getAppId());

        // 设置请求 URL - 使用完整的类路径+方法名
        sysLog.setRequestUrl(interfaceName + "." + methodName);


        // 设置请求类型 - 根据注解判断 HTTP 方法
        String requestType = getHttpMethod(method);
        sysLog.setRequestType(requestType);


        // 设置业务描述
        if (apiLog != null && !apiLog.value().isEmpty()) {
            sysLog.setMessage(apiLog.value());
        }

        // 设置用户信息
        try {
            if (ServiceContextHolder.getUser() != null) {
                sysLog.setUserid(ServiceContextHolder.getUser().getUserId());
                sysLog.setUsername(ServiceContextHolder.getUser().getUsername());
                sysLog.setCreateBy(ServiceContextHolder.getUser().getUserId());
                sysLog.setUpdateBy(ServiceContextHolder.getUser().getUserId());
            }
        } catch (Exception e) {
            logger.debug("获取用户信息失败", e);
        }

        // 从 ThreadLocal 获取 HTTP 状态码
        Integer httpCode = HTTP_STATUS_CODE.get();
        if (httpCode != null) {
            sysLog.setCode(String.valueOf(httpCode));

            // HTTP 状态码非 200 时，记录错误消息
            if (httpCode != SUCCESS_HTTP_CODE) {
                String errorMsg = ERROR_MESSAGE.get();
                sysLog.setMessage(errorMsg != null ? errorMsg : "HTTP请求失败，状态码: " + httpCode);
            }
        } else {
            // 如果没有获取到 HTTP 状态码，默认设置为成功
            sysLog.setCode(String.valueOf(SUCCESS_HTTP_CODE));
        }
    }
    /**
     * 获取 HTTP 请求方法
     */
    private String getHttpMethod(Method method) {
        if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class)) {
            return "DELETE";
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PatchMapping.class)) {
            return "PATCH";
        } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.RequestMapping.class)) {
            org.springframework.web.bind.annotation.RequestMapping requestMapping =
                    method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
            if (requestMapping.method().length > 0) {
                return requestMapping.method()[0].name();
            }
        }
        return "UNKNOWN";
    }
    /**
     * 获取 FeignClient 接口的实际名称
     */
    private String getFeignClientInterfaceName(ProceedingJoinPoint joinPoint) {
        try {
            // 从 MethodSignature 中获取声明类（即 FeignClient 接口）
            Class<?> declaringClass = ((MethodSignature) joinPoint.getSignature()).getDeclaringType();
            return declaringClass.getName();
        } catch (Exception e) {
            logger.debug("获取FeignClient接口名称失败", e);
            // 降级方案：尝试从目标类获取
            return joinPoint.getTarget().getClass().getName();
        }
    }

    /**
     * 清理 ThreadLocal
     */
    private void clearThreadLocal() {
        HTTP_STATUS_CODE.remove();
        ERROR_MESSAGE.remove();
    }

    /**
     * 从 FeignClient 中提取服务 ID
     */
    private String extractServiceId(ProceedingJoinPoint joinPoint) {
        try {
            Class<?> targetClass = joinPoint.getTarget().getClass();
            // 获取实现的接口
            Class<?>[] interfaces = targetClass.getInterfaces();
            for (Class<?> iface : interfaces) {
                // 检查接口是否有 FeignClient 注解
                if (iface.isAnnotationPresent(org.springframework.cloud.openfeign.FeignClient.class)) {
                    org.springframework.cloud.openfeign.FeignClient feignClient =
                            iface.getAnnotation(org.springframework.cloud.openfeign.FeignClient.class);
                    return feignClient.name();
                }
            }
        } catch (Exception e) {
            logger.debug("提取服务ID失败", e);
        }
        return null;
    }

    /**
     * 构建请求 URL
     */
    private String buildRequestUrl(ProceedingJoinPoint joinPoint, Method method, String interfaceName) {
        try {
            // 尝试从 RequestMapping 注解中获取路径
            String path = "";
            if (method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)) {
                org.springframework.web.bind.annotation.PostMapping postMapping =
                        method.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);
                path = postMapping.value().length > 0 ? postMapping.value()[0] : "";
            } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)) {
                org.springframework.web.bind.annotation.GetMapping getMapping =
                        method.getAnnotation(org.springframework.web.bind.annotation.GetMapping.class);
                path = getMapping.value().length > 0 ? getMapping.value()[0] : "";
            } else if (method.isAnnotationPresent(org.springframework.web.bind.annotation.RequestMapping.class)) {
                org.springframework.web.bind.annotation.RequestMapping requestMapping =
                        method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
                path = requestMapping.value().length > 0 ? requestMapping.value()[0] : "";
            }

            // 使用接口简单名称 + 方法路径
            String simpleInterfaceName = interfaceName.substring(interfaceName.lastIndexOf('.') + 1);
            return "/" + simpleInterfaceName + path;
        } catch (Exception e) {
            logger.debug("构建请求URL失败", e);
            return "/unknown";
        }
    }

    /**
     * 环绕通知：拦截带有 @FeignApiLog 注解的方法
     */
    @Around("@annotation(com.voc.service.logs.annotation.FeignApiLog)")
    public Object aroundMethod(ProceedingJoinPoint point) throws Throwable {
        return executeWithLog(point, true);
    }

    /**
     * 环绕通知：拦截带有 @FeignApiLog 注解的类中的所有方法
     */
    @Around("@within(com.voc.service.logs.annotation.FeignApiLog)")
    public Object aroundClass(ProceedingJoinPoint point) throws Throwable {
        // 如果方法上已经有注解，跳过（避免重复记录）
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(FeignApiLog.class)) {
            return point.proceed();
        }
        return executeWithLog(point, false);
    }

    /**
     * 执行带日志记录的方法调用
     */
    private Object executeWithLog(ProceedingJoinPoint point, boolean isMethodLevel) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取注解（优先使用方法上的注解，其次使用类上的注解）
        FeignApiLog apiLog = method.getAnnotation(FeignApiLog.class);
        if (apiLog == null && !isMethodLevel) {
            // 从类上获取注解
            Class<?> targetClass = point.getTarget().getClass();
            apiLog = targetClass.getAnnotation(FeignApiLog.class);
        }

        // 创建日志对象
        OpsLogModel sysLog = OpsLogModel.builder()
                .tid(ServiceContextHolder.traceId())
                .createTime(LocalDateTime.now())
                .build();

        StopWatch stopWatch = DateUtil.createStopWatch();
        stopWatch.start();

        Object result = null;
        Throwable exception = null;

        try {
            // 执行目标方法
            result = point.proceed();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            stopWatch.stop();

            try {
                // 设置耗时
                sysLog.setCostTime(stopWatch.getTotalTimeMillis());

                // 保存日志信息
                saveFeignLog(point, sysLog, apiLog);

                // 输出日志 - 参考 InternalApiLogAspect 的格式
                logger.info("外部VOC接口调用记录({})：-->>method={}, uri={}, cost={}ms, code={}, msg={}",
                        sysLog.getAppId(),
                        sysLog.getRequestType(),
                        sysLog.getRequestUrl(),
                        sysLog.getCostTime(),
                        sysLog.getCode(),
                        sysLog.getMessage());
            } catch (Exception e) {
                logger.error("记录FeignClient日志失败", e);
            } finally {
                // 清理 ThreadLocal
                clearThreadLocal();
            }
        }
    }
}
