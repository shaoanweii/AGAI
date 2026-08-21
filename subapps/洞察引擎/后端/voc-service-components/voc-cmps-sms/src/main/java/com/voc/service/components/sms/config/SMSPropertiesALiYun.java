package com.voc.service.components.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "sms")
@Data
public class SMSPropertiesALiYun {
    /**
     * 产品名称:云通信短信API产品,开发者无需替换
     */
    private String product;
    /**
     * 产品域名,开发者无需替换
     */
    private String domain;
    /**
     * 此处需要替换成开发者自己的AK
     */
    private String accessKeyId;
    /**
     * 此处需要替换成开发者自己的AK
     */
    private String accessKeySecret;
    /**
     * 验证码短信的签名
     */
    private String verifyCodeSigName;
    /**
     * 验证码短信的模板code
     */
    private String verifyCodeTemplateCode;
    /**
     * 租户license短信的签名
     */
    private String licenseSigName;
    /**
     * 租户license短信的模板code
     */
    private String licenseTemplateCode;
}

/**
 * # 短信参数
 * sms:
 * product: Dysmsapi
 * domain: dysmsapi.aliyuncs.com
 * accessKeyId: LTAI4G2i43H4ZVCT6Rb2wHuR
 * accessKeySecret: M1Spw7YGH8WJvEwPAEf4F3cW0gNplj
 * verifyCodeSigName: 智能医疗
 * verifyCodeTemplateCode: SMS_204441335
 * licenseSigName: 智能医疗
 * licenseTemplateCode: SMS_204441336
 */
