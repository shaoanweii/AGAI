package com.voc.service.security.web;

import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.security.api.ITokenService;
import com.voc.service.security.model.TokenModel;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName TokenServiceController
 * @createTime 2023年12月22日 18:14
 * @Copyright futong
 */

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Tag(name = "Token服务")
public class TokenServiceController {
    @Autowired
    ITokenService tokenService;

    @PostMapping("/findByToken")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    Result<TokenModel> findByToken(@RequestBody UserModel user) {
        return Result.OK(tokenService.findByToken(user).get());
    }
}
