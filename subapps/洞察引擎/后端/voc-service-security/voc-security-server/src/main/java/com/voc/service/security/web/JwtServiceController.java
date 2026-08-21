package com.voc.service.security.web;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.api.IJwtService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Tag(name = "JWT服务")
public class JwtServiceController {
    @Autowired
    IJwtService jwtService;


    @PostMapping("/isTokenValid")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    Result<Boolean> isTokenValid(String token, @RequestBody UserModel userModel) {

        return Result.OK(jwtService.isTokenValid(token, userModel));
    }

    @PostMapping("/checkToken")
    @ResponseBody
    Result<Boolean> checkToken(@RequestBody UserModel userModel) {

        return Result.OK(jwtService.checkToken(userModel));
    }


    @PostMapping("/deleteRidisCache")
    @ResponseBody
    Result<Boolean> deleteRidisCache() {

        return Result.OK();
    }


}
