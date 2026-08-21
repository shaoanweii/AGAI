package com.voc.service.security.web;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.model.AccountModel;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.api.ISecurityService;
import com.voc.service.security.model.ChangePasswordRequest;
import com.voc.service.security.model.ValidatePasswordRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName SecurityServiceController
 * @createTime 2023年12月22日 18:00
 * @Copyright futong
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户服务")
public class SecurityServiceController {
    @Autowired
    ISecurityService securityService;


    @PostMapping("/register")
    @ResponseBody
    public Result<Boolean> register(@RequestBody UserModel userModel) {
        return Result.OK(securityService.register(userModel));
    }

    @PostMapping("/userinfo")
    @Parameter(name="token")
    @ResponseBody
    public Result<UserModel> userinfo(@RequestParam("token") final String token) {
//        System.out.println(Result.OK());
        return Result.OK(securityService.userinfo(token));
    }

    @PostMapping("/accounts")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, required= true, description = "Bearer [token]")
    @ResponseBody
    public Result<List<AccountModel>> accounts() {
        return Result.OK(securityService.accouns());
    }



    @PostMapping("/lock")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, required= true, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> lock(@RequestBody UserModel user) {
        return Result.OK(securityService.lock(user));
    }

    @PostMapping("/unlock")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> unlock(@RequestBody UserModel user) {
        return Result.OK(securityService.unlock(user));
    }

    @PostMapping("/enable")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> enable(@RequestBody UserModel user) {
        return Result.OK(securityService.enable(user));
    }

    @PostMapping("/disable")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> disable(@RequestBody UserModel user) {
        return Result.OK(securityService.disable(user));
    }

    @PostMapping("/resetPassword")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, required= true,  description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> resetPassword(@RequestBody ChangePasswordRequest changePwd) throws AccountNotFoundException {
        return Result.OK(securityService.resetPassword(changePwd));
    }

    @PostMapping("/valiedatePassword")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> valiedatePassword(@RequestBody ValidatePasswordRequest valiedatePwd) throws AccountNotFoundException {
        return Result.OK(securityService.valiedatePassword(valiedatePwd));
    }

    /*@PostMapping("/addUser")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> addUser(@RequestBody UserModel registerRequest) {
        return Result.OK(securityService.addUser(registerRequest));
    }*/

    @PostMapping("/modifyUser")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> modifyUser(@RequestBody UserModel modifyRequest) {
        return Result.OK(securityService.modifyUser(modifyRequest));
    }

    @PostMapping("/modifyClientUser")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> modifyClientUser(@RequestBody UserModel modifyRequest) {
        return Result.OK(securityService.modifyClientUser(modifyRequest));
    }

    @PostMapping("/changeUser")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> changeUser(@RequestBody UserModel modifyRequest) {
        return Result.OK(securityService.changeUser(modifyRequest));
    }

    @PostMapping("/removeUser")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<Boolean> removeUser(@RequestBody UserModel user) {
        return Result.OK(securityService.removeUser(user));
    }

    @PostMapping("/findAll")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<List<UserModel>> findAll(@RequestBody UserModel userModel) {
        return Result.OK(securityService.findAll( userModel));
    }

    @PostMapping("/findByUserId")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<List<UserModel>> findByUserId(@RequestBody UserModel userModel) {
        return Result.OK(securityService.findByUserId(userModel));
    }

    @PostMapping("/findByConditional")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<PageInfo> findByConditional(@RequestBody UserModel userModel){
        return Result.OK(securityService.findByConditional(userModel));
    }

    @PostMapping("/getTokenByUserId")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<String> getTokenByUserId(@RequestBody UserModel user) {
        return Result.OK(securityService.getTokenByUserId(user));
    }


    @PostMapping("/checkAndGetToken")
    @Parameter(name="Authorization", in = ParameterIn.HEADER, description = "Bearer [token]")
    @ResponseBody
    public Result<UserModel> checkAndGetToken(@RequestBody UserModel user) {
        return Result.OK(securityService.checkAndGetToken(user));
    }
}
