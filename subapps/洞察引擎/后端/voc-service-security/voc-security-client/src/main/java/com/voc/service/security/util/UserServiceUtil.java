package com.voc.service.security.util;

import cn.hutool.core.util.ObjectUtil;
import com.voc.service.common.exception.ExpiredJwtException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.api.ILogoutService;
import com.voc.service.security.api.IUserService;
import com.voc.service.security.api.clients.ISecurityServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Title: UserServiceImpl
 * @Package: com.voc.service.security.service
 * @Description:
 * @Author: cuick
 * @Date: 2024/3/25 17:08
 * @Version:1.0
 */

@Component
@SuppressWarnings("SpringJavaAutowiringInspection")
public class UserServiceUtil {

    private static final Logger log = LoggerFactory.getLogger(UserServiceUtil.class);
    @Autowired
    ISecurityServiceClient iSecurityService;

    @Autowired
    IUserService userService;
    @Autowired
    ILogoutService logoutService;

    public UserModel getUserinfo(final String token, final String md5Token ) {
        //获取认证服务端的用户信息：  本地缓存有效期为10m ， 数据过期时重新读取认证服务用户数据，
        //如果期间用户被注销或停用时，过期token将无法正常访问
//        stopWatch.stop();
//        stopWatch.start("调用API 获取userinfo");

        //判断当前登陆系统中的用户会话是否已过期

        log.debug("调用接口开始：userinfo");
        final Result<UserModel> getUserRs = iSecurityService.userinfo(token);
        if (!"200".equals(getUserRs.getCode()) && ObjectUtil.isNull(getUserRs.getResult())) {
            log.error("auth.userinfo service:{}", getUserRs.getMessage());
        }

        final Optional<UserModel> userIdRs = Optional.ofNullable(getUserRs.getResult());
        if (!userIdRs.isPresent()) {
            log.error("获取用户信息失败! error: {} ", getUserRs.getMessage());
            throw new ExpiredJwtException(getUserRs.getMessage());
        }
//        if( userService.sessionTimeout(token)){
//            log.warn(CommonErrorEnum.LOGIN_EXPERD_EXECPTION.getMessage());
//            ServiceContextHolder.getExecutor().execute(() -> {
//                logoutService.logout();
//            });
//            throw new ExpiredJwtException(CommonErrorEnum.LOGIN_EXPERD_EXECPTION);
//        }
        log.trace("userinfo 调用成功 {}", userIdRs.get());
        return userIdRs.get();

    }

}
