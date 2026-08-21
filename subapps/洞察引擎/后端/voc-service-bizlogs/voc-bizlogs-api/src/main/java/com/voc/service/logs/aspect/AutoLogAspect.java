package com.voc.service.logs.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.voc.service.common.constant.CommonConstant;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.logs.annotation.AutoLog;
import com.voc.service.logs.api.clients.IBizLogServiceClient;
import com.voc.service.logs.model.OpsLogModel;
import com.voc.service.logs.util.IpUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;


/**
 * 系统日志，切面处理类
 */
@Aspect
@Component
public class AutoLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(AutoLogAspect.class);
    //    @SofaReference(binding = @SofaReferenceBinding(bindingType = "bolt"))
    @Autowired
    IBizLogServiceClient bizLogService;

    public AutoLogAspect() {
        logger.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Pointcut("@annotation(com.voc.service.logs.annotation.AutoLog)")
    public void logPointCut() {
        logger.info("call.{}", AutoLogAspect.class.getSimpleName());
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //创建一个StopWatch对象
        StopWatch stopWatch = DateUtil.createStopWatch();
        stopWatch.start();
        OpsLogModel sysLog = OpsLogModel.builder().tid(ServiceContextHolder.traceId()).createTime(LocalDateTime.now()).build();

        //执行方法
        logger.info("--->> around begin {}", point.getSignature().getName());
        final Object result_ = point.proceed();
        logger.info("--->> around proceed {}", point.getSignature().getName());


        stopWatch.stop();
        //耗时
        sysLog.setCostTime(stopWatch.getTotalTimeMillis());

        //获取request
        final HttpServletRequest request = ServiceContextHolder.getRequest();
        try {
            logger.info("saveSysLog begin");
            saveSysLog(point, sysLog, request, result_);
            logger.info("saveSysLog end");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            logger.info("内部VOC接口调用记录({})：-->> method= {}, uri={}, cost={}ms, code={}, msg={}", sysLog.getAppId()
                    , sysLog.getRequestType(), sysLog.getRequestUrl(), sysLog.getCostTime(),sysLog.getCode(), sysLog.getMessage() );
        }
        //保存日志

//        log.info("需实现该功能！");
        return result_;
    }


    public void saveSysLog(ProceedingJoinPoint joinPoint, OpsLogModel sysLog, HttpServletRequest request, final Object result) {
        // 获取方法签名
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
// 获取方法
        final Method method = signature.getMethod();

        final AutoLog syslog = method.getAnnotation(AutoLog.class);
        if (syslog != null) {
            //注解上的描述,操作日志内容
            sysLog.setLogContent(syslog.value());
            // 设置sysLog对象的logType属性为syslog的logType属性
            sysLog.setLogType(syslog.logType());
        }

        //请求的方法名
        final String className = joinPoint.getTarget().getClass().getName();
        // 获取方法名
        final String methodName = signature.getName();
        // 获取系统日志，并设置方法名为类名和方法名
        sysLog.setMethod(className + "." + methodName + "()");

        // 解析 User-Agent 判断请求来源
        String userAgent = request.getHeader("user-agent");
        String clientType = parseClientType(userAgent);
        // 设置访问来源类型
        sysLog.setAccessSourceType(clientType);

        //设置操作类型
        if (sysLog.getLogType() == CommonConstant.LOG_TYPE_2) {
            sysLog.setOperateType(getOperateType(methodName, syslog.operateType()));
        }

        //请求的参数
        sysLog.setRequestParam(getReqestParams(request, joinPoint));
        //设置IP地址
        sysLog.setIp(IpUtil.getIpAddr(request));
        // 设置请求的URL
        sysLog.setRequestUrl(request.getRequestURI());
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
        //保存系统日志
        ServiceContextHolder.getExecutor().execute(() -> {
            bizLogService.pushBizLogsMsg(sysLog);
        });

        // 将sysLog对象转换为JSON格式的字符串，并打印日志
        logger.info("{}", JSONUtil.toJsonStr(sysLog, JSONConfig.create().setIgnoreNullValue(true)));
        logger.trace("bizLogService push message successed.");

    }

    /**
     * 根据 User-Agent 判断客户端类型
     */
    private String parseClientType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "UNKNOWN";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "APP";
        }
        return "PC";
    }

    /**
     * 根据方法名和操作类型，获取操作类型
     */
    private int getOperateType(String methodName, int operateType) {
        // 如果operateType大于0，则返回operateType
        if (operateType > 0) {
            return operateType;
        }
        // 如果方法名以list、get或find开头，则返回OPERATE_TYPE_1
        if (methodName.startsWith("list") || methodName.startsWith("get") || methodName.startsWith("find") || methodName.startsWith("query")) {
            return CommonConstant.OPERATE_TYPE_1;
        }
        // 如果方法名以add或insert开头，则返回OPERATE_TYPE_2
        if (methodName.startsWith("add") || methodName.startsWith("insert")) {
            return CommonConstant.OPERATE_TYPE_2;
        }
        // 如果方法名以edit、update或modify开头，则返回OPERATE_TYPE_3
        if (methodName.startsWith("edit") || methodName.startsWith("update") || methodName.startsWith("modify")) {
            return CommonConstant.OPERATE_TYPE_3;
        }
        // 如果方法名以delete或remove开头，则返回OPERATE_TYPE_4
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return CommonConstant.OPERATE_TYPE_4;
        }
        // 如果方法名以import开头，则返回OPERATE_TYPE_5
        if (methodName.startsWith("import")) {
            return CommonConstant.OPERATE_TYPE_5;
        }
        // 如果方法名以export开头，则返回OPERATE_TYPE_6
        if (methodName.startsWith("export")) {
            return CommonConstant.OPERATE_TYPE_6;
        }
        // 默认返回OPERATE_TYPE_1
        return CommonConstant.OPERATE_TYPE_1;
    }

    /**
     * //     * @param request:   request
     *
     * @param joinPoint: joinPoint
     * @Description: 获取请求参数
     * @date: 2020/4/16 0:10
     * @Return: java.lang.String
     */
    private String getReqestParams(HttpServletRequest request, JoinPoint joinPoint) {
        final String httpMethod = request.getMethod();
        final String params;
        if ("POST".equals(httpMethod) || "PUT".equals(httpMethod) || "PATCH".equals(httpMethod)) {
            Object[] paramsArray = joinPoint.getArgs();
            // java.lang.IllegalStateException: It is illegal to call this method if the current request is not in asynchronous mode (i.e. isAsyncStarted() returns false)
            //  https://my.oschina.net/mengzhang6/blog/2395893
            Object[] arguments = new Object[paramsArray.length];
            for (int i = 0; i < paramsArray.length; i++) {
                if (paramsArray[i] instanceof ServletRequest || paramsArray[i] instanceof ServletResponse || paramsArray[i] instanceof MultipartFile) {
                    //ServletRequest不能序列化，从入参里排除，否则报异常：java.lang.IllegalStateException: It is illegal to call this method if the current request is not in asynchronous mode (i.e. isAsyncStarted() returns false)
                    //ServletResponse不能序列化 从入参里排除，否则报异常：java.lang.IllegalStateException: getOutputStream() has already been called for this response
                    continue;
                }
                arguments[i] = paramsArray[i];
            }

            params = JSONUtil.toJsonStr(arguments, JSONConfig.create().setIgnoreNullValue(true));
        } else {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            // 请求的方法参数值
            Object[] args = joinPoint.getArgs();
            // 请求的方法参数名称
//            LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
            final Parameter[] parameters = method.getParameters();
            final String[] paramNames = Arrays.stream(parameters).map(Parameter::getName).toArray(String[]::new);
            StringBuilder paramsBuilder = new StringBuilder();
            if (args != null && paramNames != null) {
                for (int i = 0; i < args.length; i++) {
                    paramsBuilder.append("  " + paramNames[i] + ": " + args[i]);
//                    params += "  " + paramNames[i] + ": " + args[i];
                }
            }
            // 将paramsBuilder转换为字符串
            params = paramsBuilder.toString();
        }
        return params;
    }
}
