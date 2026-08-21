package com.voc.service.security.impl;

import cn.hutool.core.util.StrUtil;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.exception.SecurityException;
import com.voc.service.common.model.AccountModel;
import com.voc.service.common.model.UserModel;
import com.voc.service.config.PBEStringEncryptor;
import com.voc.service.security.api.IJwtService;
import com.voc.service.security.api.ISSOService;
import com.voc.service.security.api.ISecurityService;
import com.voc.service.security.api.ITokenService;
import com.voc.service.security.api.clients.ILoginServiceClient;
import com.voc.service.security.model.TokenModel;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName SsoSerivce
 * @createTime 2024年01月29日 11:09
 * @Copyright futong
 */

@Service
public class SsoSerivce implements ISSOService {

    private static final Logger logger = LoggerFactory.getLogger(SsoSerivce.class);
    @Autowired
    IJwtService jwtService;
    @Autowired
    ITokenService tokenService;
    @Autowired
    ILoginServiceClient loginServiceClient;
    @Autowired
    ISecurityService securityService;

    @Override
    public String login(final String appId, final String token) {
        Assert.notNull(token, "token cannot be empty");
        Assert.notNull(appId, "appId cannot be empty");

        //1、token有效性
        final Map<String, String> map = jwtService.extractClaim(token);
        final String userId = Optional.ofNullable(map.get(IJwtService.USER_ID))
                .orElseThrow(() -> new SecurityException("token数据异常，无法完成SSO"));

        //判断当前用户是否在本系统已完成了登陆
        final Optional<TokenModel> loginedToken = tokenService.findByToken(UserModel.builder().userId(userId).appId(appId).build());
        if (loginedToken.isPresent()) {
            return loginedToken.get().getToken();
        }

        //2、生成免密登陆加密窜
        final String authStr = this.encrypt(userId, appId);
        logger.info("authStr:{}", authStr);
        //3、提交系统免密登录申请，并后去token
        final String accessToken = this.freeAuth(appId,userId, authStr)
                .orElseThrow(() -> new SecurityException(CommonErrorEnum.SSO_LOGIN_EXECPTION.getMessage()));
        logger.info("accessToken:{}",accessToken);
        return accessToken;
    }

    @Override
    public String ssoLogin(String appId, String userId) {
        Assert.notNull(userId, "userId cannot be empty");
        Assert.notNull(appId, "appId cannot be empty");
        //1. 校验用户是否在本系统内
        List<UserModel> user = securityService.findUserByUserId(UserModel.builder().appId(appId).userId(userId).build());
        if(ObjectUtils.isEmpty(user)){
            throw new SecurityException(CommonErrorEnum.LOGIN_ACCOUNR_EXECPTION);
        }
        logger.info("查询到用户:{}",user);
        //用户
        final UserModel userModel = user.stream().findFirst().get();
        List<AccountModel> accounts = userModel.getAccounts();
        //账号
        final AccountModel accountModel = accounts.stream().findFirst().get();
        //判断账号是否锁定
        if(!accountModel.isNonLocked()){
            throw new SecurityException(CommonErrorEnum.ACCOUNT_LOCK);
        }
        //判断账号是否启用
        if(!accountModel.isEnabled()){
            throw new SecurityException(CommonErrorEnum.ACCOUNT_DISABLE);
        }
        //2. 获取免密登录加密串
        final String authStr = this.encrypt(userId, appId);
        logger.info("authStr:{}", authStr);
        //3、提交系统免密登录申请，并获取token
        final String accessToken = this.freeAuth(appId,userId, authStr)
                .orElseThrow(() -> new SecurityException(CommonErrorEnum.SSO_LOGIN_EXECPTION.getMessage()));
        logger.info("accessToken:{}",accessToken);
        return accessToken;
    }

    private Optional<String> freeAuth( String appId,String userId,String authStr) {


        return Optional.ofNullable(null);
    }

    private String encrypt(final String userId, final String appId) {
        Assert.notNull(userId, "userId cannot be empty");
        Assert.notNull(appId, "appId cannot be empty");

        final String str = userId.concat(":").concat(appId).concat(":").concat(String.valueOf(System.currentTimeMillis() + 1000 * 60 * 1));

        return PBEStringEncryptor.getInstance().encrypt(str);
    }

    /**
     * 解密
     *
     * @param userId
     * @param credential
     * @return
     */
    @Override
    public boolean decrypt(String userId, String credential) {
        Assert.isTrue(StrUtil.isNotBlank(userId), "userId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(credential), "credential cannot be empty");

        try {

            final String decrypt = PBEStringEncryptor.getInstance().decrypt(credential);
//            final String time = StrUtil.split(decrypt, ":").get(2);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return false;
    }


}
