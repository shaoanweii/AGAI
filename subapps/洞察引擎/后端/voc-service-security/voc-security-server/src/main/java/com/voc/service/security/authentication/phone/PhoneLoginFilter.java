package com.voc.service.security.authentication.phone;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.security.api.ICredentialsService;
import com.voc.service.security.authentication.AbstractAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class PhoneLoginFilter extends AbstractAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(PhoneLoginFilter.class);
    public static final AntPathRequestMatcher MATCHER = new AntPathRequestMatcher("/auth/phone/login", "POST");
    public static final String TYPE = ICredentialsService.IDENTITY_TYPE_PHONE;

    @Autowired
    PhoneAuthenticationProvider authenticationProvider;

    //    public PhoneLoginFilter(AuthenticationManager authenticationManager, PhoneAuthenticationHandler authenticationHandler) {
    public PhoneLoginFilter(AuthenticationManager authenticationManager, PhoneAuthenticationHandler authenticationHandler) {
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
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getType()), "type cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getPhone()), "phone cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getPassword()), "password cannot be empty");

        //
        PhoneAuthenticationToken authRequest = new PhoneAuthenticationToken(loginModel.getPhone(), loginModel.getPassword());
        // Allow subclasses to set the "details" property
        this.setDetails(request, authRequest);
//        return this.getAuthenticationManager().authenticate(authRequest);
        return authenticationProvider.authenticate(authRequest);
    }

    protected void setDetails(HttpServletRequest request, PhoneAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

}
