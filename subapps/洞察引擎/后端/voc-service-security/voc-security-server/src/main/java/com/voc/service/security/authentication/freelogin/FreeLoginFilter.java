package com.voc.service.security.authentication.freelogin;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.config.PBEStringEncryptor;
import com.voc.service.security.api.ICredentialsService;
import com.voc.service.security.authentication.AbstractAuthenticationFilter;
import com.voc.service.security.crypto.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseLoginFilter
 * @Description ckcui
 * @createTime 2023年12月01日 9:55
 * @Copyright futong
 */

@Component
public class FreeLoginFilter extends AbstractAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(FreeLoginFilter.class);
    public static final AntPathRequestMatcher MATCHER = new AntPathRequestMatcher("/auth/free/login", "POST");
    public static final String TYPE = ICredentialsService.IDENTITY_TYPE_FREE;

    @Autowired
    FreeAuthenticationProvider authenticationProvider;

    public FreeLoginFilter(AuthenticationManager authenticationManager, FreeAuthenticationHandler authenticationHandler) {
        super(MATCHER, authenticationManager, TYPE);
//        this.setFilterProcessesUrl("/v1/auth/login");
        this.setAuthenticationSuccessHandler(authenticationHandler);
        this.setAuthenticationFailureHandler(authenticationHandler);
//        this.setauth
//        super(new AntPathRequestMatcher("/v1/auth/sms/login", "POST"));
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        final UserModel loginModel = this.getUserModel(request);
        log.debug("loginModel：{}",loginModel);
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getType()), "type cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getUserId()), "userId cannot be empty");

        final String username = PasswordUtil.decrypt(loginModel.getUserId());
        if(ObjectUtils.isNotEmpty( username)){
            loginModel.setUserId(username);
           loginModel.setPassword(PBEStringEncryptor.getInstance().encrypt(username));
        }

        FreeAuthenticationToken authRequest = new FreeAuthenticationToken(loginModel.getUserId(), loginModel.getPassword());
        // Allow subclasses to set the "details" property
        this.setDetails(request, authRequest);
//        return this.getAuthenticationManager().authenticate(authRequest);
        return authenticationProvider.authenticate(authRequest);
    }

    protected void setDetails(HttpServletRequest request, FreeAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}
