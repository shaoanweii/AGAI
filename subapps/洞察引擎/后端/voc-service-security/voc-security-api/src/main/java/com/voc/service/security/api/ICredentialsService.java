package com.voc.service.security.api;

import com.voc.service.common.model.AccountModel;
import com.voc.service.security.model.CredentialsModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName IAuthorizationService
 * @Description ckcui
 * @createTime 2023年11月29日 12:33
 * @Copyright futong
 */
public interface ICredentialsService {
    String IDENTITY_TYPE_BASE = "base"; // 账号+密码

    String IDENTITY_TYPE_WX = "wx"; // 微信

    String IDENTITY_TYPE_PHONE = "phone"; // 手机号

    String IDENTITY_TYPE_PHONE_SMS = "sms"; // 手机号 + 密码

    String IDENTITY_TYPE_EMAIL = "email"; // 手机号 + 短信

    String IDENTITY_TYPE_FREE = "free"; // 用户id

//    List<String> IDENTITY_TYPES = Arrays.asList(IDENTITY_TYPE_BASE, IDENTITY_TYPE_WX, IDENTITY_TYPE_PHONE, IDENTITY_TYPE_PHONE_SMS,IDENTITY_TYPE_EMAIL);

//    List<CredentialsModel> findAllLoginType();

//    void addLoginType(CredentialsModel model);

    int add(CredentialsModel credentialsModel);

    Optional<CredentialsModel> find(CredentialsModel param);

    int removeTestUsers(Set<String> ids);
    List<AccountModel> findByUserIds(Set<String> ids, String appId);

    boolean changePassword(final CredentialsModel param);

    boolean unlock(final CredentialsModel model);
    boolean lock(final CredentialsModel model);

    boolean enable(final CredentialsModel build);

    boolean disable(final CredentialsModel model);

    List<CredentialsModel> findByUserId(final String userId, final String appId);

    boolean changeAllCredentials(final CredentialsModel model);


}
