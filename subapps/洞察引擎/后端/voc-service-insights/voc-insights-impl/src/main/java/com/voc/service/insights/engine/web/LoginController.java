package com.voc.service.insights.engine.web;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsProjectInfoService;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.vo.InsUserInfoVo;
import com.voc.service.security.api.ICredentialsService;
import com.voc.service.security.api.ICustomInfoService;
import com.voc.service.security.api.ILoginService;
import com.voc.service.security.api.clients.ISecurityServiceClient;
import com.voc.service.security.client.model.BaseLoginModel;
import com.voc.service.security.model.AuthenticationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "系统登陆服务")
public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    ILoginService loginService;
    @Autowired
    ICustomInfoService customInfoService;
    @Resource
    private IInsProjectInfoService insProjectInfoService;
    @Autowired
    ISecurityServiceClient securityService;

    @PostMapping(value = "/base/login")
    @Operation(summary = "账号、口令认证")
    @ResponseBody
    Result<AuthenticationResponse> base_login(@RequestBody BaseLoginModel login) {
        log.info("base-login:{}", login);
        Assert.isTrue(StrUtil.isNotBlank(login.getUsername()), "username cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(login.getPassword()), "password cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(login.getCheckKey()), "checkKey cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(login.getCaptcha()), "captcha cannot be empty");

        return loginService.login(UserModel.builder()
                .username(login.getUsername())
                .password(login.getPassword())
                .appId(ServiceContextHolder.getSystemId())
                .type(ICredentialsService.IDENTITY_TYPE_BASE)
                .captcha(login.getCaptcha())
                .checkKey(login.getCheckKey())
                .build());
    }

    @PostMapping(value = "/base/freeLogin")
    @Operation(summary = "账号、口令认证")
    @ResponseBody
    Result<AuthenticationResponse> freeLogin(@RequestBody BaseLoginModel login) {
        log.info("base-login:{}", login);
        Assert.isTrue(StrUtil.isNotBlank(login.getUsername()), "username cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(login.getUserId()), "userId cannot be empty");
        UserModel build = UserModel.builder()
                .userId(login.getUsername())
                .username(login.getUsername())
                .appId(ServiceContextHolder.getSystemId())
                .type(ICredentialsService.IDENTITY_TYPE_FREE)
                .build();
        Result<UserModel> userModel = securityService.checkAndGetToken(build);
        if(ObjectUtils.isNotEmpty(userModel)&&"200".equals(userModel.getCode())&&ObjectUtils.isNotEmpty(userModel.getResult())){
            final UserModel result = userModel.getResult();
            if(ObjectUtils.isNotEmpty(userModel)){
                log.info("token 未过期");
                return Result.OK(AuthenticationResponse.builder()
                        .accessToken(result.getTokenKey())
                        .appId(ServiceContextHolder.getSystemId())
                        .username(result.getUsername())
                        .userid(result.getUserId())
                        .build());
            }
        }
        return loginService.login(build);
    }

    @PostMapping(value = "/userInfo")
    @Operation(summary = "获取用户信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @ResponseBody
    Result<InsUserInfoVo> userInfo() {
        InsUserInfoVo userInfo = (InsUserInfoVo) customInfoService.getUserInfo();
        return Result.OK(userInfo);
    }

    @PostMapping(value = "/userPermissions")
    @Operation(summary = "获取用户权限")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @ResponseBody
    Result<InsUserInfoVo> userPermissions() {
        InsUserInfoVo userInfo = (InsUserInfoVo) customInfoService.getUserPermissions();
        return Result.OK(userInfo);
    }

    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取当前用户最新生成的文件记录")
    @PostMapping("/getFile")
    public Result<?> getFile(@RequestBody LargeDigitaFilesModel insDataSourceModel) {
        try {
            LargeDigitaFilesModel file = insProjectInfoService.getFile(insDataSourceModel);
            return Result.OK(file);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }


}
