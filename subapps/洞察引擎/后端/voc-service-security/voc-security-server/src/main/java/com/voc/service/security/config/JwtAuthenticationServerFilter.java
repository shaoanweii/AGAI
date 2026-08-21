package com.voc.service.security.config;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.exception.SecurityException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.JsonMapper;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.security.api.IJwtService;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.token.TokenRepository;
import com.voc.service.security.model.TokenModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationServerFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationServerFilter.class);
    public static final String BEARER_TYPE = "Bearer";
    public static final String ACCESS_TOKEN_TYPE = "Authentication.ACCESS_TOKEN_TYPE";
    public static TimedCache<String, TokenModel> TOKEN_USER_CACHE = CacheUtil.newTimedCache(1000 * 60 * 60 * 24 * 7);  // 7天
    @Autowired
    JwtService jwtService;
    //    @Autowired
//    UserService userDetailsService;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    WhiteListServerProperties whiteListServerProperties;
    @Autowired
    SecurityConverMapperService securityConverMapperService;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;
    @Autowired
    private IllegalPathsFilterRuleProperties illegalPathsFilterRuleProperties;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        ServiceContextHolder.setTraceId(StrUtil.isNotBlank(TraceContext.traceId()) ?  TraceContext.traceId() : DigestUtil.md5Hex(IdWorker.getId()));
        ServiceContextHolder.setRequest(request);
        log.info("【{}】==>> requestURI: {}-{}", ServiceContextHolder.traceId(),request.getMethod(), request.getRequestURI());
        try {
            final String uri = request.getRequestURI();
            if(this.checkIllegalPath(illegalPathsFilterRuleProperties.getIllegalPaths(), uri)){
                log.error("【{}】==>> 非法路径 {}", ServiceContextHolder.traceId(), uri);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                Result<Object> errors = Result.errors(CommonErrorEnum.URL_ILLEGAL.getMessage());
                ObjectMapper mapper = JsonMapper.getInstances().getMapper();
                response.getWriter().write(mapper.writeValueAsString(errors));
                return;
            }

            if (this.match(whiteListServerProperties.getUrls(), uri)) {
                // 如果是白名单直接放行
                log.trace("【{}】白名单放行 {}", ServiceContextHolder.traceId(),  uri);
                filterChain.doFilter(request, response);
                return;
            }

            Optional<Object> tokenObj = Optional.ofNullable(this.extractHeaderToken(request));
            log.debug("【{}】==>> 开始认证流程: {}-{}", ServiceContextHolder.traceId(), request.getMethod(), request.getRequestURI());
//            RpcInvokeContext context = RpcInvokeContext.getContext();
            StringBuilder token = new StringBuilder();
            StringBuilder appid = new StringBuilder();
            StringBuilder identityType = new StringBuilder();


            if (tokenObj.isPresent() && StrUtil.isNotBlank(String.valueOf(tokenObj.get()))) {
                token.append((String) tokenObj.get());
                log.trace("【{}】rpc token {}", ServiceContextHolder.traceId(), tokenObj.get());
            } else {
                //获取 header 中的token
                token.append(this.extractHeaderToken(request));
                log.trace("【{}】header token {}", ServiceContextHolder.traceId(), token);
            }
            log.trace("【{}】token {}", ServiceContextHolder.traceId(), token);
            if (ObjectUtil.isNull(token.toString())) {
                log.error("【{}】token cannot be empty {} ", ServiceContextHolder.traceId(), token);
                throw new SecurityException(CommonErrorEnum.NOTNULL_AUTH_TOKN);
            }

            StringBuilder username = new StringBuilder();
            StringBuilder userid = new StringBuilder();
            try {
                //token解析
                final Map<String, String> map = jwtService.extractClaim(token.toString());

                username.append(map.get(IJwtService.USERNAME));
                userid.append(map.get(IJwtService.USER_ID));
                appid.append(map.get(IJwtService.APP_ID));
                identityType.append(map.get(IJwtService.IDENTITY_TYPE));
                log.trace("【{}】username {},userid {}, appid {}", ServiceContextHolder.traceId(), username.toString(), userid.toString(), appid.toString());
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new SecurityException(CommonErrorEnum.TOKEN_ERROR);
            }

            if (ObjectUtil.isNull(username)) {
                log.error("【{}】username cannot be empty {} ", ServiceContextHolder.traceId(), token);
                throw new SecurityException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
            }
            if (ObjectUtil.isNull(userid)) {
                log.error("【{}】userid cannot be empty {} ", ServiceContextHolder.traceId(), token);
                throw new SecurityException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
            }
            if (ObjectUtil.isNull(appid)) {
                log.error("【{}】app_id cannot be empty {} ", ServiceContextHolder.traceId(), token);
                throw new SecurityException(CommonErrorEnum.APP_ID_DISABLE);
            }
            if (ObjectUtil.isNull(identityType)) {
                log.error("【{}】identity_type cannot be empty {} ", ServiceContextHolder.traceId(), token);
                throw new SecurityException(CommonErrorEnum.LOGIN_TYPE_DISABLE);
            }

            //认证端验证用户数据顺序： redis token验证 -> redis用户数据验证 -> 数据库用户数据获取
            final UserModel userModelParam = UserModel.builder()
                    .username(username.toString())
                    .userId(userid.toString())
                    .appId(appid.toString())
                    .type(identityType.toString())
                    .token(token.toString())
                    .build();
            final String md5Token = userModelParam.getTokenKey();
            ServiceContextHolder.setUser(userModelParam);
            if(this.match(whiteListServerProperties.getTokens(), String.valueOf(token))){
                log.trace("【{}】白名单放行 {}", ServiceContextHolder.traceId(), uri);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        null,
                        md5Token,
                        null
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                ServiceContextHolder.setUser(null);
                filterChain.doFilter(request, response);
                return;
            }
            //redis 验证: token 有效性
            TokenModel tokenModel = tokenRepository.findByToken(userModelParam).orElse(null);
            if (ObjectUtil.isNull(tokenModel)) {
                throw new SecurityException(CommonErrorEnum.NOT_LOGIN_EXECPTION);
            }

            final boolean isTokenValid = Optional.of(tokenModel).map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
            if (!isTokenValid) {
                log.error("【{}】{}[0]！", ServiceContextHolder.traceId(), CommonErrorEnum.ACCOUNT_EXP.getMessage());
                throw new SecurityException(CommonErrorEnum.ACCOUNT_EXP);
            }
            // 验证token 用户名是否合法
            if (!jwtService.isTokenValid(token.toString(), userModelParam)) {
                log.error("【{}】{}[0]！", ServiceContextHolder.traceId(), CommonErrorEnum.TOKEN_ERROR.getMessage());
                throw new SecurityException(CommonErrorEnum.TOKEN_ERROR);
            }

            //本地缓存（5s） 验证
            if (TOKEN_USER_CACHE.containsKey(md5Token)) {
                tokenModel = JwtAuthenticationServerFilter.TOKEN_USER_CACHE.get(md5Token);
                log.trace("【{}】使用缓存 {}", ServiceContextHolder.traceId(), md5Token);
            } else {
                //redis 获取
                if (ObjectUtil.isNotNull(tokenModel)) {
                    log.debug("【{}】使用redis获取到的数据", ServiceContextHolder.traceId());
                    //放入缓存 UsernamePasswordAuthenticationToken
                    if (!JwtAuthenticationServerFilter.TOKEN_USER_CACHE.containsKey(md5Token)) {
                        JwtAuthenticationServerFilter.TOKEN_USER_CACHE.put(md5Token, tokenModel);
                    }
                } else {
                    log.debug("【{}】未从redis获取到数据", ServiceContextHolder.traceId());
                }
            }

            if (ObjectUtil.isNotNull(tokenModel.getUser())) {
                tokenModel.getUser().setToken(token.toString());
                //放入缓存
                JwtAuthenticationServerFilter.TOKEN_USER_CACHE.put(md5Token, tokenModel);

                final UserDetails userDetails = securityConverMapperService.converToUserDetails(tokenModel.getUser());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        md5Token,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                ServiceContextHolder.setUser(tokenModel.getUser());
            } else {
                throw new java.lang.SecurityException("设置 ServiceContextHolder 失败！");
            }
        } catch (SecurityException e) {
//            log.error(e.getMessage(),e);
            resolver.resolveException(request, response, null, e);
            return;
        }catch (IllegalArgumentException e){
//            log.error(e.getMessage(),e);
            resolver.resolveException(request, response, null,
                    new SecurityException(CommonErrorEnum.TOKEN_ERROR.getCode()
                            , CommonErrorEnum.LOGIN_PASSWORD_EXECPTION.getMessage()));
            return;
        }catch (Exception e) {
//            log.error(e.getMessage(),e);
            resolver.resolveException(request, response, null,
                    new SecurityException(CommonErrorEnum.TOKEN_ERROR.getCode()
                            , "调用接口失败"));
            return;
        }
        log.trace("【{}】doFilter.start: {}", ServiceContextHolder.traceId(), request.getRequestURI());
        filterChain.doFilter(request, response);
        log.trace("【{}】doFilter.end:", ServiceContextHolder.traceId());
    }

    private boolean match(Set<String> pathList, String uri) {
        return pathList.stream().anyMatch(path -> new AntPathMatcher().match(path, uri));
    }

    private Boolean checkIllegalPath(Set<String> pathList, String uri){
        return pathList.stream().anyMatch(path -> {
            Pattern compile = Pattern.compile(path, Pattern.CASE_INSENSITIVE);
            return compile.matcher(uri).matches();
        });
    }

    public static void main(String[] args) {
        String regis = "^.*[/\\\\\\\\]\\\\.*(apk|exe|dll|bat|sh|cmd|php|jsp|asp|aspx)$";
        String uri = "/file/6673cee7699b118a0e6b4103igi6cSqZ05";
        System.out.println("1. 原始正则: " +
                Pattern.compile("/[a-zA-Z0-9+/]{20,}={0,2}($|[?#/])",
                                Pattern.CASE_INSENSITIVE)
                        .matcher(uri).matches());
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