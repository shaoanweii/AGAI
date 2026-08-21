package com.voc.service.security.authentication.formbase;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UserModel;
import com.voc.service.security.api.IAuthenticationService;
import com.voc.service.security.api.ICredentialsService;
import com.voc.service.security.authentication.AbstractAuthenticationFilter;
import com.voc.service.security.config.WhiteClientServerProperties;
import com.voc.service.security.crypto.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseLoginFilter
 * @Description ckcui
 * @createTime 2023年12月01日 9:55
 * @Copyright futong
 */

@Component
public class BaseLoginFilter extends AbstractAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(BaseLoginFilter.class);
    public static final String TYPE = ICredentialsService.IDENTITY_TYPE_BASE;
    public static final AntPathRequestMatcher MATCHER = new AntPathRequestMatcher("/auth/base/login", "POST");
    @Autowired
    BaseAuthenticationProvider authenticationProvider;
    @Autowired
    IAuthenticationService authenticationService;
    @Autowired
    WhiteClientServerProperties whiteClientServerProperties;

    public BaseLoginFilter(AuthenticationManager authenticationManager, BaseAuthenticationHandler baseAuthenticationHandler) {
        super(MATCHER, authenticationManager, TYPE);
//        this.setFilterProcessesUrl("/v1/auth/login");
        this.setAuthenticationSuccessHandler(baseAuthenticationHandler);
        this.setAuthenticationFailureHandler(baseAuthenticationHandler);
//        this.setAuthenticationSuccessHandler(baseAuthenticationHandler);
//        this.setAuthenticationFailureHandler(baseAuthenticationHandler);
//        this.setauth
//        super(new AntPathRequestMatcher("/v1/auth/sms/login", "POST"));
        log.info("--->> init {}", this.getClass().getSimpleName());
    }

    /*public BaseLoginFilter(){

        log.info("--->> init {}", this.getClass().getSimpleName());
//        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/v1/auth/base/login", "POST"));
//        setFilterProcessesUrl("/v1/auth/base/login");

    }*/

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        log.debug("attemptAuthentication");
        final UserModel loginModel = this.getUserModel(request);
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getUsername()), "username cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getPassword()), "password cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getType()), "type cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getAppId()), "appId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(loginModel.getCheckKey()), "checkKey cannot be empty");

        Optional<String> codeMsg = Optional.ofNullable(authenticationService.checkCaptcha(loginModel));
        if(codeMsg.isPresent()){
            throw new InsufficientAuthenticationException(CommonErrorEnum.VERIFICATION_EXECPTION.getMessage());
        }
        final String username = PasswordUtil.decrypt(loginModel.getUsername());
        final String password = PasswordUtil.decrypt(loginModel.getPassword());
        //
//        BaseAuthenticationToken authRequest =
//                new BaseAuthenticationToken(loginModel.getUsername(), loginModel.getPassword());
                BaseAuthenticationToken authRequest =
                new BaseAuthenticationToken(username, password);
        // Allow subclasses to set the "details" property
        this.setDetails(request, authRequest);
        log.debug("authenticate(authRequest)");
//        return this.getAuthenticationManager().authenticate(authRequest);
        Authentication authenticate = authenticationProvider.authenticate(authRequest);
//        UserEntity principal = (UserEntity) authenticate.getPrincipal();
//        final String clientId = principal.getClientId();
//        if (!this.match(whiteClientServerProperties.getIds(), clientId)) {
//           throw new CredentialsExpiredException(CommonErrorEnum.CLIENT_DISABLED.getMessage());
//        }
        return authenticate;
    }

    protected void setDetails(HttpServletRequest request, BaseAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    private boolean match(Set<String> pathList, String uri) {
        return pathList.stream().anyMatch(path -> new AntPathMatcher().match(path, uri));
    }
}
