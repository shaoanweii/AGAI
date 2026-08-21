package com.voc.service.security.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.Data;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName LoginDemo
 * @createTime 2024年01月30日 12:10
 * @Copyright futong
 */

@Builder
@Data
@Tag(name = "登陆样例")
//@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginDemo {
    @Schema(required= true,  description = "登陆类型为base时为必填字段", title = "登陆账号",example = "admin")
    private String username;
    @Schema(required= true,  description = "登陆类型为email,phone,base时为必填字段", title = "密码",example = "Passw0rd@!")
    private String password;
    @Schema(required= true,  description = "voc,modeltraining等", title = "系统标识",example = "insights")
    private String appId;
    @Schema(required= true,  description = "email,phone,sms,base等", title = "登录类型",example = "base")
    private String type;
    @Schema(required= true, title = "验证码key",example = "123")
    private String checkKey;
    @Schema(required= true, title = "验证码",example = "2587")
    private String captcha;

//    @Schema(required= false,  description = "登陆类型为email时为必填字段", title = "邮件地址")
//    private String email;

//    @Schema(required= false,  description = "登陆类型为phone,sms时为必填字段", title = "手机号")
//    private String phone;

//    @Schema(required= false, title = "短信码")
//    private String smscode;

}
