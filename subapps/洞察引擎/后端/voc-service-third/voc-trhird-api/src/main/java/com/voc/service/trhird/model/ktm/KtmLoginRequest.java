package com.voc.service.trhird.model.ktm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yonglongjiang
 * @time: 2025/10/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KtmLoginRequest {

    // 登录用户ID
    private String loginId;
    // 密码 base加密
    private String password;
    // 服务器IP，选填
    private String clientIp;
    // 选填
    private String utcZeroOffset;
}
