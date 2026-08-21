package com.voc.service.security.api.clients;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.model.ChangePasswordRequest;
import com.voc.service.security.model.ValidatePasswordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

@FeignClient(name = "service.auth.user", url = "${service.auth.v1}/user")
//@FeignClient(name = "service.auth.user", url = "localhost:9000/user")
public interface ISecurityServiceClient {

    @PostMapping("/register")
    Result<Boolean> register(@RequestBody UserModel userModel);

    @PostMapping("/userinfo")
//    Result<UserModel> userinfo();
    Result<UserModel> userinfo(@RequestParam(value = "token", required = true) final String token);

//    @PostMapping("/accounts")
//    Result<List<AccountModel>> accounts();

    @PostMapping("/lock")
    Result<Boolean> lock(@RequestBody UserModel user);

    @PostMapping("/unlock")
    Result<Boolean> unlock(@RequestBody UserModel user);

    @PostMapping("/enable")
    Result<Boolean> enable(@RequestBody UserModel user);

    @PostMapping("/disable")
    Result<Boolean> disable(@RequestBody UserModel user);

    @PostMapping("/resetPassword")
    Result<Boolean> resetPassword(@RequestBody ChangePasswordRequest changePwd);

    @PostMapping("/valiedatePassword")
    Result<Boolean> valiedatePassword(@RequestBody ValidatePasswordRequest valiedatePwd);

//    @PostMapping("/addUser")
//    Result<Boolean> addUser(@RequestBody UserModel registerRequest);

    @PostMapping("/modifyUser")
    Result<Boolean> modifyUser(@RequestBody UserModel modifyRequest);
    @PostMapping("/modifyClientUser")
    Result<Boolean> modifyClientUser(@RequestBody UserModel modifyRequest);

    @PostMapping("/changeUser")
    Result<Boolean> changeUser(@RequestBody UserModel modifyRequest);

    @PostMapping("/removeUser")
    Result<Boolean> removeUser(@RequestBody UserModel user);

    @PostMapping("/findAll")
    public Result<List<UserModel>> findAll(@RequestBody UserModel userModel);

    @PostMapping("/findByUserId")
    public Result<List<UserModel>> findByUserId(@RequestBody UserModel userModel) ;

    @PostMapping("/findByConditional")
    Result<PageInfo> findByConditional(@RequestBody UserModel userModel);

    @PostMapping("/getTokenByUserId")
    Result<String> getTokenByUserId(@RequestBody UserModel user);

    @PostMapping("/checkAndGetToken")
    Result<UserModel> checkAndGetToken(@RequestBody UserModel user);
}
