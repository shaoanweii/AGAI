package com.voc.service.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.logs.api.clients.IBizLogServiceClient;
import com.voc.service.security.api.ILoginHistoryService;
import com.voc.service.security.config.JwtService;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.entity.UserEntity;
import com.voc.service.security.impl.token.TokenRepository;
import com.voc.service.security.model.AuthenticationResponse;
import com.voc.service.security.model.LoginHistroyModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ParentAuthenticationHandler
 * @createTime 2024年01月26日 12:16
 * @Copyright futong
 */

public class ParentAuthenticationHandler {

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ParentAuthenticationHandler.class);


    @Autowired
    JwtService jwtService;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    SecurityConverMapperService securityConverMapperService;
    @Autowired
    IBizLogServiceClient bizLogServiceClient;
    @Autowired
    ILoginHistoryService iLoginHistoryService;


    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        final UserEntity userEntity = (UserEntity) authentication.getPrincipal();
        log.info("success {}", userEntity.getIdentifier());

        final UserModel model = securityConverMapperService.converTo(userEntity);

        final long expirationHours = this.getExpirationHours(userEntity.getExpireDate());
        //根据账号有效期生成token过期时间
        final String token = jwtService.generateToken(model, expirationHours);
        log.trace("token.{}", token);

        model.setToken(token);
        //保存用户新token
        tokenRepository.save(model, LocalDateTime.ofInstant(
                new Date(System.currentTimeMillis() + expirationHours * 1000 * 60 * 60 * 7).toInstant(), ZoneId.systemDefault()));

        AuthenticationResponse.builder()
                .appId(model.getAppId())
                .username(model.getUsername())
                .type(model.getType())
                .accessToken(token)
//                    .refreshToken(refreshToken)
                .build();


        //登陆成功记录
        try {
            ServiceContextHolder.setToken(token);
//            ServiceContextHolder.setTraceId(TraceContext.traceId());
//            ServiceContextHolder.setUser(model);
//            model.setBizAuthRoles(null);
//            model.setSysAuthRoles(null);
            model.setToken(null);


            iLoginHistoryService.addAsync(LoginHistroyModel.builder()
                    .appId(userEntity.getAppId())
                    .credentialId(userEntity.getId())
                    .loginType(userEntity.getIdentityType())
                    .userId(userEntity.getUserId())
                    .tid(ServiceContextHolder.traceId())
//                    .loginTime(userEntity.)
                    .build());

            log.info("发送登陆日志. {}", model.getUserId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        response.setStatus(HttpStatus.OK.value());

        // SecurityContext在设置Authentication的时候并不会自动写入Session，读的时候却会根据Session判断，所以需要手动写入一次，否则下一次刷新时SecurityContext是新创建的实例。
//        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        response.getWriter().println(OBJECT_MAPPER.writeValueAsString(
                Result.OK(
                        AuthenticationResponse.builder()
                                .userid(model.getUserId())
                                .appId(model.getAppId())
                                .username(model.getUsername())
                                .type(model.getType())
                                .accessToken(token)
//                    .refreshToken(refreshToken)
                                .build()
                )));
    }

    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response
            , AuthenticationException exception) throws IOException, ServletException {

//        String detailMessage = exception.getClass().getSimpleName() + " " + exception.getLocalizedMessage();
        String detailMessage = exception.getMessage();
        int code = 500;
        if (exception.getCause() instanceof BussinessException) {
            BussinessException ex = (BussinessException) exception.getCause();
            code = ex.getCode();
            detailMessage = ex.getMessage();
        } else if (exception instanceof LockedException) {
//            code = 200;
            detailMessage = CommonErrorEnum.ACCOUNT_EXP.getMessage();
        } else if (exception instanceof DisabledException) {
//            code = 200;
            detailMessage = CommonErrorEnum.ACCOUNT_EXP.getMessage();
        } else if (exception instanceof AccountExpiredException) {
            code = 500;
            detailMessage = CommonErrorEnum.ACCOUNT_EXP.getMessage();
        } else if (exception instanceof BadCredentialsException) {
            code = 500;
            detailMessage = CommonErrorEnum.LOGIN_PASSWORD_EXECPTION.getMessage();
        } else if (exception instanceof InsufficientAuthenticationException) {
            code = 500;
            detailMessage = CommonErrorEnum.VERIFICATION_EXECPTION.getMessage();
        } else {
            code = 500;
            detailMessage = exception.getMessage();
        }

        log.error("onAuthenticationFailure : {}", detailMessage);
        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().println(OBJECT_MAPPER.writeValueAsString(Result.error(code, detailMessage)));
    }

    /**
     * //根据账号有效期生成token过期时间
     *
     * @param expireDate
     * @return
     */
    private long getExpirationHours(LocalDateTime expireDate) {
        final long expirationDate;

        final LocalDateTime now = LocalDateTime.now();
        if (now.plusHours(jwtService.getJwtExpiration()).compareTo(expireDate) > 1) {
            expirationDate = ChronoUnit.HOURS.between(now, expireDate);
        } else {
            expirationDate = jwtService.getJwtExpiration();
        }
        return expirationDate;
    }
}
