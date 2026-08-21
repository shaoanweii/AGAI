package com.voc.service.security.client.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.exception.ExpiredJwtException;
import com.voc.service.common.exception.GetUserInfoException;
import com.voc.service.common.exception.SecurityException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.model.auth.PermissionModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.security.api.IUserService;
import com.voc.service.security.client.config.WhiteListClientProperties;
import com.voc.service.security.util.UserServiceUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName JwtFilter
 * @Description ckcui
 * @createTime 2023年11月24日 14:34
 * @Copyright futong
 */
@Component
@WebFilter(filterName = "FirstFilter", urlPatterns = "/*")
@Order(1)
public class JwtFilter implements jakarta.servlet.Filter {

    public static final String BEARER_TYPE = "Bearer";
    public static final String ACCESS_TOKEN_TYPE = "Authentication.ACCESS_TOKEN_TYPE";
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
//    public static TimedCache<String, UserModel> TOKEN_USER_CACHE = CacheUtil.newTimedCache(1000 * 60 * 60 * 24 * 7);  // 10秒

    /*Cache<String, UserModel> TOKEN_USER_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)  // 设置缓存过期时间为10分钟
//            .maximumSize(100)  // 设置最大缓存条目数
            .build();*/

    //    @SofaReference(binding = @SofaReferenceBinding(bindingType = "bolt"))
    @Autowired
    UserServiceUtil userServiceUtil;
    @Autowired
    StringRedisTemplate strRedisTemplate;
    /*@Autowired
    IUserService iUserService;*/
    @Value("${appId:}")
    String appid;
    @Autowired
    WhiteListClientProperties whiteListClientProperties;
    @Autowired
    IUserService userService;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;
    @Autowired
    private Environment env;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.debug("--->> init {}", this.getClass().getSimpleName());

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse reps, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) reps;
        final String uri = request.getRequestURI();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("jwt过滤任务开始 ".concat(uri));
        try {
            log.info("--->> Invoking {} {}", this.getClass().getSimpleName(), request.getRequestURI());
            ServiceContextHolder.setTraceId(TraceContext.traceId());
            ServiceContextHolder.setRequest(request);
            stopWatch.stop();
            stopWatch.start("白名单");

            if (this.match(whiteListClientProperties.getUrls(), uri)) {
                // 如果是白名单直接放行
                log.trace("白名单放行 {}", uri);
                filterChain.doFilter(req, reps);
                return;
            }
            stopWatch.stop();
            stopWatch.start("获取token");
//            if (true){
//                filterChain.doFilter(req, reps);
//                return;
//            }
            /*Iterator<String> it = request.getHeaderNames().asIterator();
            while (it.hasNext()){
                String name = it.next();
                System.out.println( name + " " +request.getHeader(name));
            }*/
            final String token = this.extractHeaderToken(request);
            log.debug("uri:{} token={}", uri, token);
            if (StrUtil.isBlank(token)) {
                log.error("token cannot be empty {} ", token);
                throw new SecurityException(CommonErrorEnum.NOTNULL_AUTH_TOKN);
            }
            stopWatch.stop();
            stopWatch.start("获取userinfo");

            //链路数据透传
           /* RpcInvokeContext context = RpcInvokeContext.getContext();
            context.putRequestBaggage(RpcConstants.HIDDEN_KEY_TOKEN, token);
            context.putRequestBaggage(RpcConstants.INTERNAL_KEY_APP_NAME, appid);*/

            log.debug("获取用户信息-开始");
            ServiceContextHolder.setToken(token);
//            userModel.set(iSecurityService.userinfo(token));
            final UserModel userModel = userServiceUtil.getUserinfo(token, MD5.create().digestHex(token));
            log.info("当前用户信息:{}", userModel);
            if (ObjectUtil.isNotNull(userModel)) {
                ServiceContextHolder.setUser(userModel);

                if (!containsAppId(userModel.getAppId())) {
                    log.error("{}, {}", userModel.getAppId(), CommonErrorEnum.APP_ID_DISABLE.getMessage());
                    throw new SecurityException(CommonErrorEnum.APP_ID_DISABLE);
                }
                if (this.match(whiteListClientProperties.getTokens(), token)) {
                    log.trace("白名单放行 {}", uri);
                    filterChain.doFilter(req, reps);
                    return;
                }
                final String appName = env.getProperty("spring.application.name");
                final String appId = env.getProperty("spring.appid");
                userModel.setAppName(appName);
                log.info("系统appName:{},当前用户Id:{}", appName, userModel.getUserId());
                if (ObjectUtils.isNotEmpty(appId) && !appId.contains(userModel.getAppId())) {
//                    log.info("系统appName:{},当前用户appId:{},白名单放行", appName,userModel.getAppId());
                    log.trace("白名单放行 {}", uri);
                    filterChain.doFilter(req, reps);
                    return;
                }

            }


            stopWatch.stop();
            stopWatch.start("加载本系统内的用户数据");
            if (ObjectUtil.isNotNull(userModel)) {
                //加载本系统内的用户数据（包含前线信息）
                log.debug("加载本系统内的用户数据（包含权限信息）-开始");
                userService.readPermissions(userModel);
                log.debug("加载本系统内的用户数据（包含权限信息）-结束");

                log.trace("readPermissions 调用成功 {}", userModel);

                stopWatch.stop();
                stopWatch.start("获取api访问权");

                //TODO  暂时注释接口访问权限，有需要在打开
                //请求路径权限过滤
//                if (isAccessOk(uri, userModel)) {
//                    log.trace("用户模块访问权限校验通过");
//                } else {
//                    log.error("{}：{}", CommonErrorEnum.NOT_AUTHORITY_EXECPTION.getMessage(), uri);
//                    throw new SecurityException(CommonErrorEnum.NOT_AUTHORITY_EXECPTION);
//                }
            } else {
                log.trace("findByUsername 调用失败 {}", userModel);
                log.error("获取当前系统用户数据失败！");
            }

            stopWatch.stop();
            stopWatch.start("执行业务");
            filterChain.doFilter(req, reps);

        } catch (ExpiredJwtException e) {
            log.error(uri.concat("->").concat(e.getMessage()), e);
            resolver.resolveException(request, response, null,
                    new ExpiredJwtException(CommonErrorEnum.EXPIRED_JWT_EXECPTION.getCode()
                            , e.getMessage()));
        } catch (GetUserInfoException e) {
            log.error(uri.concat("->").concat(e.getMessage()), e);
            resolver.resolveException(request, response, null,
                    new GetUserInfoException(CommonErrorEnum.USERINFO_EXECPTION.getCode()
                            , e.getMessage()));
        } catch (SecurityException e) {
            log.error(uri.concat("->").concat(e.getMessage()), e);
            resolver.resolveException(request, response, null,
                    new SecurityException(e.getCode()
                            , e.getMessage()));
        } catch (Exception e) {
            log.error(uri.concat("->").concat(e.getMessage()), e);
            resolver.resolveException(request, response, null,
                    new SecurityException(CommonErrorEnum.TOKEN_ERROR.getCode()
                            , e.getMessage()));
        } finally {
            stopWatch.stop();
//            log.info("uri:{} 执行时间:{}", uri, stopWatch.prettyPrint( TimeUnit.MILLISECONDS));
        }

    }


    private boolean containsAppId(String appIdParam) {
        if (StrUtil.isBlank(appid) || StrUtil.isBlank(appIdParam)) {
            return true;
        }

        return Stream.of(appid.split(",")).collect(Collectors.toSet()).contains(appIdParam);
    }

    /**
     * 请求路径权限过滤
     *
     * @param uri
     * @param userModel
     * @return
     */
    private boolean isAccessOk(String uri, UserModel userModel) {
        if (StrUtil.equalsIgnoreCase(userModel.getAdmin(), "true")) {
            return true;
        }
        if (uri.lastIndexOf('/') == 0) {
            return true;
        }
        List<String> list = Arrays.asList("/overview/databriefing", "/dndc/sso/checkToken"
        );
        Set<String> collect = list.stream().filter(s -> uri.contains(s)).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(collect)) {
            return true;
        }
        PermissionModel accessPermissions = userModel.getAccessPermissions();
        if (ObjectUtils.isNotEmpty(accessPermissions) && CollUtil.isNotEmpty(accessPermissions.getValues()) && accessPermissions.getValues().containsKey("paths")) {
            final Set<String> paths = accessPermissions.getValue("paths");
            if (CollUtil.isNotEmpty(paths)) {
                if (uri.contains(ServiceContextHolder.getAppId())) {
                    String[] split = uri.split(ServiceContextHolder.getAppId());
                    return this.match(new HashSet<>(paths), split[1]);
                }
                return this.match(new HashSet<>(paths), uri);
            }
        }
        return false;
    }

    private boolean match(Set<String> pathList, String uri) {
        return pathList.stream().anyMatch(path -> new AntPathMatcher().match(path, uri));
    }

    protected String extractHeaderToken(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders("Authorization");
        while (headers.hasMoreElements()) { // typically there is only one (most servers enforce that)
            String value = headers.nextElement();
            if ((value.toLowerCase(Locale.ENGLISH).startsWith(BEARER_TYPE.toLowerCase(Locale.ENGLISH)))) {
                String authHeaderValue = value.substring(BEARER_TYPE.length()).trim();
                // Add this here for the auth details later. Would be better to change the signature of this method.
                request.setAttribute(ACCESS_TOKEN_TYPE,
                        value.substring(0, BEARER_TYPE.length()).trim());
                int commaIndex = authHeaderValue.indexOf(',');
                if (commaIndex > 0) {
                    authHeaderValue = authHeaderValue.substring(0, commaIndex);
                }
                return authHeaderValue;
            }
        }

        return null;
    }
}
