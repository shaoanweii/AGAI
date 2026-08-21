package com.voc.service.security.web;

import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.exception.SecurityException;
import com.voc.service.common.response.Result;
import com.voc.service.security.api.IAppService;
import com.voc.service.security.impl.SsoSerivce;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "系统单点登陆服务")
public class SsoLoginController {
    private static final Logger log = LoggerFactory.getLogger(SsoLoginController.class);
    @Autowired
    SsoSerivce ssoSerivce;
    @Autowired
    IAppService appService;

    @GetMapping(value = "/sso")
    @ResponseBody
    Result<?> sso(final String redirect, final String token, HttpServletResponse response) throws IOException {
        log.info("redirect:{}", redirect);
        log.info("token:{}", token);
        try {
            //验证条件地址有效性
            final String appId = Optional.ofNullable(appService.getAppIdByURL(redirect))
                    .orElseThrow(() -> new SecurityException("单点跳转URL无效"));

            final String sysToken = ssoSerivce.login(appId, token);
            response.addHeader("token", sysToken);
            response.sendRedirect(redirect.concat("?token=").concat(sysToken));
            log.info("单点登陆成功：{}",sysToken );
            return Result.OK();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //登陆失败
            return Result.error(CommonErrorEnum.SSO_LOGIN_EXECPTION.getMessage());
        }
    }

    @GetMapping(value = "/ssoLogin")
    @ResponseBody
    Result<String> ssoLogin(@RequestParam("userId") String userId, @RequestParam("appId") String appId, HttpServletResponse response) throws IOException {
        log.info("userId:{}", userId);
        log.info("appId:{}", appId);
        try {
            final String sysToken = ssoSerivce.ssoLogin(appId, userId);
            log.info("单点登陆成功：{}",sysToken);
            return Result.OK(sysToken);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //登陆失败
            return Result.errors(CommonErrorEnum.SSO_LOGIN_EXECPTION.getCode(),e.getMessage());
        }
    }
}
