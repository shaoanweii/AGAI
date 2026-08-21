package com.voc.service.security.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.AccountModel;
import com.voc.service.common.model.UserModel;
import com.voc.service.security.model.ChangePasswordRequest;
import com.voc.service.security.model.ValidatePasswordRequest;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * //@ClassName ISecurityService
 * @Description ckcui
 * @createTime 2023年10月09日 17:07
 * //@Copyright futong
 */
//@CacheConfig(cacheNames = "securityService")
public interface ISecurityService {

    boolean register(final UserModel userModel);

    UserModel userinfo(final String token);

    //@CacheEvict(value = "users", key = "#p0.id")
    boolean  unlock(final UserModel user);
    boolean lock(final UserModel user);

    //@CacheEvict(value = "users", key = "#p0.id")
    boolean enable(final UserModel user);

    //@CacheEvict(value = "users", key = "#p0.id")
    boolean disable(final UserModel user);

    //@CacheEvict(value = "users", key = "#p0.id")
    boolean resetPassword(final ChangePasswordRequest changePwd) throws AccountNotFoundException;

    //@CacheEvict(value = "users", key = "#p0.id")
    boolean valiedatePassword(final ValidatePasswordRequest valiedatePwd) throws AccountNotFoundException;

    boolean addUser(final UserModel registerRequest);

    //@CacheEvict(value = "users", key = "#p0.id")
    boolean modifyUser(final UserModel modifyRequest);
    boolean changeUser(final UserModel modifyRequest);

    boolean removeUser(final UserModel user);


    boolean removeTestUsers(UserModel user);
    List<AccountModel> accouns();
    List<UserModel> findAll(UserModel userModel);
    List<UserModel> findByUserId(UserModel userModel);
    List<UserModel> findUserByUserId(UserModel userModel);

    PageInfo findByConditional(UserModel userModel);

    Boolean modifyClientUser(UserModel modifyRequest);

    String getTokenByUserId(UserModel user);

    UserModel checkAndGetToken(UserModel user);
}
