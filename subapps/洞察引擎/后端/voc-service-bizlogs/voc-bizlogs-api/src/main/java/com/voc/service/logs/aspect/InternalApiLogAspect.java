package com.voc.service.logs.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.logs.annotation.InternalApiLog;
import com.voc.service.logs.model.OpsLogModel;
import com.voc.service.logs.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 内部接口调用日志切面
 * 记录方法调用的详细信息：方法名、URI、耗时、返回码、消息等
 */
@Aspect
@Component
public class InternalApiLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(InternalApiLogAspect.class);

    private void saveSysLog(ProceedingJoinPoint joinPoint, OpsLogModel sysLog, HttpServletRequest request, final Object result) {
        // 获取方法签名
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
// 获取方法
        final Method method = signature.getMethod();
        final InternalApiLog syslog = method.getAnnotation(InternalApiLog.class);

        //请求的方法名
        final String className = joinPoint.getTarget().getClass().getName();
        // 获取方法名
        final String methodName = signature.getName();
        // 获取系统日志，并设置方法名为类名和方法名
        sysLog.setMethod(className + "." + methodName + "()");

        //设置IP地址
        sysLog.setIp(IpUtil.getIpAddr(request));
        // 设置请求的URL - 使用完整的类路径+方法名
        sysLog.setRequestUrl(className + "." + methodName);
        // 获取系统日志，并设置请求类型为request.getMethod()
        sysLog.setRequestType(request.getMethod());

        // 设置用户id
        sysLog.setUserid(ServiceContextHolder.getUser().getUserId());
        // 设置用户名
        sysLog.setUsername(ServiceContextHolder.getUser().getUsername());
        // 设置创建人
        sysLog.setCreateBy(ServiceContextHolder.getUser().getUserId());
        // 设置更新人
        sysLog.setUpdateBy(ServiceContextHolder.getUser().getUserId());
        // 设置系统标识
        sysLog.setAppId(ServiceContextHolder.getAppId());


        if (!Objects.isNull(result)) {
            if (result instanceof Result) {
                Result rs = (Result) result;
                // 获取系统日志，并设置编码
                sysLog.setCode(rs.getCode());
                // 设置系统日志的消息
                sysLog.setMessage(rs.getMessage());
            }
        }

    }

    @Around("@annotation(com.voc.service.logs.annotation.InternalApiLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        InternalApiLog apiLog = method.getAnnotation(InternalApiLog.class);
        OpsLogModel sysLog = OpsLogModel.builder().tid(ServiceContextHolder.traceId()).createTime(LocalDateTime.now()).build();
        StopWatch stopWatch = DateUtil.createStopWatch();
        stopWatch.start();


        //获取request
        final HttpServletRequest request = ServiceContextHolder.getRequest();
        final Object result_ = point.proceed();

        try {
            stopWatch.stop();
            //耗时
            sysLog.setCostTime(stopWatch.getTotalTimeMillis());
            logger.info("saveSysLog begin");
            saveSysLog(point, sysLog, request, result_);
            logger.info("saveSysLog end");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            logger.info("内部VOC接口调用记录({})：-->> method={}, uri={}, cost={}ms, code={}, msg={}", sysLog.getAppId()
                    , sysLog.getRequestType(), sysLog.getRequestUrl(), sysLog.getCostTime(),sysLog.getCode(), sysLog.getMessage() );
        }

        return result_;
    }

}
