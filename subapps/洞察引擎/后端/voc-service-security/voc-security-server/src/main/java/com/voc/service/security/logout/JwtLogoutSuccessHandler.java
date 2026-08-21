package com.voc.service.security.logout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voc.service.common.response.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
public class JwtLogoutSuccessHandler implements org.springframework.security.web.authentication.logout.LogoutSuccessHandler {
    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


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

}
