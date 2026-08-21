package com.voc.service.security.api.clients;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.model.AuthenticationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ckcui
 * @version 1.0.0
 * //@ClassName ISecurityService
 * @Description ckcui
 * @createTime 2023年10月09日 17:07
 * //@Copyright futong
 */
//@CacheConfig(cacheNames = "securityService")

@FeignClient(name = "service.auth.logout", url = "${service.auth.logout.v1}")
public interface ILogoutServiceClient {

    @PostMapping
    Result<?> logout();

}
