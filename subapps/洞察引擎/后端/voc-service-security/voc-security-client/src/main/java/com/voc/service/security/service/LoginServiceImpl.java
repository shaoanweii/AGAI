package com.voc.service.security.service;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.api.ILoginService;
import com.voc.service.security.api.IUserService;
import com.voc.service.security.api.clients.ILoginServiceClient;
import com.voc.service.security.model.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName LogoutServiceImpl
 * @createTime 2024年03月01日 17:30
 * @Copyright futong
 */
@Service("defaultLoginService")
public class LoginServiceImpl implements ILoginService {
    private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);
    @Autowired
    ILoginServiceClient loginServiceClient;
    @Autowired
    IUserService userService;

    @Override
    public Result<AuthenticationResponse> login(@RequestBody UserModel login) {
        Assert.isTrue(StrUtil.isNotBlank(login.getAppId()), "appId cannot be empty");
        Result<AuthenticationResponse> rs = loginServiceClient.login(login);
//        if("200".equals(rs.getCode())) {
//            log.info("login success");
//            userService.generateSession(rs.getResult().getAccessToken());
//        }

        return rs;
    }
}
