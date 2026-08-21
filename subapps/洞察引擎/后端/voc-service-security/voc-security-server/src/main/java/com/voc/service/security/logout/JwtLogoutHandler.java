package com.voc.service.security.logout;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.security.api.IJwtService;
import com.voc.service.security.config.JwtService;
import com.voc.service.security.impl.token.TokenRepository;
import com.voc.service.security.model.TokenModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName LogoutHandler
 * @Description ckcui
 * @createTime 2023年12月04日 9:52
 * @Copyright futong
 */
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements org.springframework.security.web.authentication.logout.LogoutHandler {
    public static final String BEARER_TYPE = "Bearer";
    public static final String ACCESS_TOKEN_TYPE = "Authentication.ACCESS_TOKEN_TYPE";
    private static final Logger log = LoggerFactory.getLogger(JwtLogoutHandler.class);
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    // /logout
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        ServiceContextHolder.setTraceId(TraceContext.traceId());
        final String token = this.extractHeaderToken(request);
        if (StrUtil.isBlank(token)) {
            log.warn("token is empty");
            return;
        }

        final Map<String, String> map = jwtService.extractClaim(token);

        final String username = map.get(IJwtService.USERNAME);
        final String userId = map.get(IJwtService.USER_ID);
        final String appid = map.get(IJwtService.APP_ID);
        final String identityType = map.get(IJwtService.IDENTITY_TYPE);

        Assert.isTrue(StrUtil.isNotBlank(username), "username cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(userId), "userId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(appid), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(identityType), "type cannot be empty");


        Optional<TokenModel> storedToken =  tokenRepository.findByToken(UserModel.builder()
                .userId(userId)
                .username(username)
                .token(token)
                .appId(appid)
                .type(identityType)
                .username(username).build());
        if(storedToken.isPresent()) {
            storedToken.get().getUser().setToken(token);

            storedToken.get().setExpired(true);
            storedToken.get().setRevoked(true);
            tokenRepository.delete(storedToken.get().getUser());
        }else{
            log.warn("storedToken not found");
        }
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
