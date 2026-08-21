package com.voc.service.security.api.clients;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author ckcui
 * @version 1.0.0
 * //@ClassName ISecurityService
 * @Description ckcui
 * @createTime 2023年10月09日 17:07
 * //@Copyright futong
 */
//@CacheConfig(cacheNames = "securityService")

@FeignClient(name = "service.auth.ssoLogin", url = "${service.auth.sso.v1}")
//@FeignClient(name = "service.auth.ssoLogin", url = "http://localhost:9000")
public interface ISSOLoginServiceClient {

//    @GetMapping("/ssoLogin")
//    Result<String> ssoLogin(@RequestParam("userId") String userId, @RequestParam("appId") String appId);

    @PostMapping("/token/checkToken")
    Result<Boolean> checkToken(@RequestBody UserModel userModel);
}
