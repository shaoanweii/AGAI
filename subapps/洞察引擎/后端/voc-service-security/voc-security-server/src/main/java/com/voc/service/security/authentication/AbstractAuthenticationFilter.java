package com.voc.service.security.authentication;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voc.service.common.filter.RepeatedlyReadRequestWrapper;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.security.api.ICredentialsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.Map;


/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AbstractAuthenticationFilter
 * @Description ckcui
 * @createTime 2023年12月04日 10:31
 * @Copyright futong
 */
public abstract class AbstractAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger log = LoggerFactory.getLogger(AbstractAuthenticationFilter.class);
    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=UTF-8";

    private final static AntPathRequestMatcher mather = new AntPathRequestMatcher("/auth/login", "POST");
    //    protected static TransmittableThreadLocal<UserModel> userThreadLocal = new TransmittableThreadLocal<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    ICredentialsService credentialsService;
    //    RequestMatcher subclassMatcher;
    String type;

    public AbstractAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher
            , AuthenticationManager authenticationManager
            , final String type) {
//        super(requiresAuthenticationRequestMatcher, authenticationManager);
        super(requiresAuthenticationRequestMatcher);
        super.setAuthenticationManager(authenticationManager);
        this.type = type;
//        this.subclassMatcher = requiresAuthenticationRequestMatcher;
    }

    private static boolean isContentTypeJson(HttpServletRequest request) {
        final String contentType = request.getContentType();
        return APPLICATION_JSON_CHARSET_UTF_8.equalsIgnoreCase(contentType) || MimeTypeUtils.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        if (!mather.matches(request)) {
            chain.doFilter(request, response);
            return;
        }
        log.info("uri: {}",request.getRequestURI());
        ServiceContextHolder.setTraceId(TraceContext.traceId());
        ServiceContextHolder.setRequest(request);
        if (isContentTypeJson(request) && ObjectUtil.isNull(this.getUserModel(request).getType())) {
            final Map<String, String> map = objectMapper.readValue(new RepeatedlyReadRequestWrapper(request).getReader(), new TypeReference<>() {
            });
            try {
                final UserModel userModel = BeanUtil.fillBeanWithMap(map, UserModel.builder().build(), true);
                request.setAttribute("userModel", userModel);
                Assert.isTrue(StrUtil.isNotBlank(userModel.getType()), "type cannot be empty");
                Assert.isTrue(StrUtil.isNotBlank(userModel.getAppId()), "appId cannot be empty");
                ServiceContextHolder.setUser(userModel);
                log.info("loginModel: {}", userModel);
            } catch (Exception e) {
                log.error("{}", request.getRequestURI());
                log.error(e.getMessage(), e);
                throw new UsernameNotFoundException(e.getMessage(), e);
            }
        }

        final String type = this.getUserModel(request).getType();
        if (this.type.equalsIgnoreCase(type)) {
            this.setRequiresAuthenticationRequestMatcher(mather);
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    public UserModel getUserModel(HttpServletRequest request) {
        Object att = request.getAttribute("userModel");
        if (ObjectUtil.isNull(att)) {
            return UserModel.builder().build();
        }
        return (UserModel) att;
    }

    /*public RequestMatcher matcher() {
        return this.subclassMatcher;
    }*/
}
