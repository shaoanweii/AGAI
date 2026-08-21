package com.voc.service.security.authentication.freelogin;

import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.response.Result;
import com.voc.service.security.authentication.ParentAuthenticationHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseAuthenticationHandler
 * @Description ckcui
 * @createTime 2023年12月01日 9:45
 * @Copyright futong
 */
@Component
public class FreeAuthenticationHandler extends ParentAuthenticationHandler implements AuthenticationSuccessHandler
        , AuthenticationFailureHandler
        , LogoutSuccessHandler
        , SessionInformationExpiredStrategy
        , AccessDeniedHandler, AuthenticationEntryPoint {


    private static final Logger log = LoggerFactory.getLogger(FreeAuthenticationHandler.class);

    /**
     * 认证成功时的处理
     *
     * @param request        the request which caused the successful authentication
     * @param response       the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     *                       the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * 权限不足时的处理
     *
     * @param request               that resulted in an <code>AccessDeniedException</code>
     * @param response              so that the user agent can be advised of the failure
     * @param accessDeniedException that caused the invocation
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String detailMessage = accessDeniedException.getClass().getSimpleName() + " " + accessDeniedException.getLocalizedMessage();

        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().println(OBJECT_MAPPER.writeValueAsString(Result.error(500, detailMessage)));
    }

    /**
     * 认证失败时的处理
     *
     * @param request   the request during which the authentication attempt occurred.
     * @param response  the response.
     * @param exception the exception which was thrown to reject the authentication
     *                  request.
     * @throws IOException
     * @throws ServletException
     */
    @Override
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
            code = 200;
            detailMessage = exception.getMessage();
        } else if (exception instanceof DisabledException) {
            code = 200;
            detailMessage = exception.getMessage();
        } else if (exception instanceof AccountExpiredException) {
            code = 200;
            detailMessage = exception.getMessage();
        }

        log.error("onAuthenticationFailure : {}", exception.getMessage());
        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().println(OBJECT_MAPPER.writeValueAsString(Result.error(code, detailMessage)));
    }


    /**
     * 登出成功处理
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().println(OBJECT_MAPPER.writeValueAsString(Result.OK("注销成功")));
    }

    /**
     * 会话过期处理
     *
     * @param event
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {

        String message = "该账号已从其他设备登陆,如果不是您自己的操作请及时修改密码";
        final HttpServletResponse response = event.getResponse();
        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().println(OBJECT_MAPPER.writeValueAsString(Result.OK(message, event.getSessionInformation())));
    }

    /**
     * 认证失败处理
     *
     * @param request       that resulted in an <code>AuthenticationException</code>
     * @param response      so that the user agent can begin authentication
     * @param authException that caused the invocation
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String detailMessage = authException.getClass().getSimpleName() + " " + authException.getLocalizedMessage();
        if (authException instanceof InsufficientAuthenticationException) {
            detailMessage = "请登陆后再访问";
        }
        response.setContentType(APPLICATION_JSON_CHARSET_UTF_8);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().println(OBJECT_MAPPER.writeValueAsString(Result.error(500, detailMessage)));
    }
}
