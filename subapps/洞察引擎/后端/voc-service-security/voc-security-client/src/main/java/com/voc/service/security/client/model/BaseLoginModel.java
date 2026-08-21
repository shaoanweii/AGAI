package com.voc.service.security.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName BaseLoginModel
 * @createTime 2024年03月04日 15:44
 * @Copyright futong
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BaseLoginModel implements Serializable {
    @Schema(description = "登录账号", example = "admin")
    protected String username;
    @Schema(description = "登陆口令 token脚本: debugger var code=ke.response.data.code; if(code==200){ var token=ke.response.data.result.access_token; ke.global.setHeader(\"Authorization\",\"Bearer \"+token); } ", example = "Passw0rd@!")
    protected String password;
    @Schema(description = "验证码", example = "2587")
    protected String captcha;
    @Schema(description = "验证码key", example = "123")
    protected String checkKey;
    /*@Schema(description = "认证类型" ,defaultValue = "base", example = "base")
    protected String type;*/
    /*@Schema(description = "认证系统" ,defaultValue = "insights", example = "insights")
    protected String appId;*/
    @Schema(description = "用户id")
    private String userId;
}
