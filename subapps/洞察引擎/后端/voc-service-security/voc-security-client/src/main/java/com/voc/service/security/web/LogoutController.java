package com.voc.service.security.web;

import com.voc.service.common.response.Result;
import com.voc.service.security.api.ILogoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@Tag(name = "系统注销服务")
public class LogoutController {
    @Autowired
    ILogoutService logoutService;

    @PostMapping(value = "/logout")
    @Operation(summary = "系统注销")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    Result<?> logout() throws IOException {
        logoutService.logout();
        return Result.OK();
    }
}
