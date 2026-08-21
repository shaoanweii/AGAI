package com.voc.service.security.api;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName SsoSerivce
 * @createTime 2024年01月29日 11:09
 * @Copyright futong
 */
public interface ISSOService {
    String login(final String appId, final String token);

    String ssoLogin(final String appId,final String userId);

    boolean decrypt(String userId, String credential);
}
