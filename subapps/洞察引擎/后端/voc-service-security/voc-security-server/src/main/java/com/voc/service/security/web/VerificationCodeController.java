package com.voc.service.security.web;

import com.voc.service.common.response.Result;
import com.voc.service.security.api.IAuthenticationService;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @version 1.0.0
 * @ClassName VerificationCodeController.java
 * @Description
 * @createTime 2022年09月07日 09:23
 * @Copyright futong
 */
@RestController
@RequestMapping("/randomImage")
@Tag(name = "验证码")
public class VerificationCodeController {
    //    @Autowired
//    RedisUtil redisUtil;
    @Autowired
    IAuthenticationService authenticationService;

    /**
     * 后台生成图形验证码 ：有效
     *
     * @param key
     */
    @Schema(defaultValue = "获取验证码")
    @GetMapping(value = "/{key}")
    @ResponseBody
    public Result<?> randomImage(@PathVariable String key) {
        try {
            final String base64 = authenticationService.createRandomImage(key);
            return Result.OK(base64);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取验证码出错" + e.getMessage());
        }
    }
}
