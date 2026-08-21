package com.voc.service.components.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "kuanyun")
@Data
public class SMSPropertiesKuanYun {

    private String baseUrl;

    private String userId;

    private String appKey;

    private String smsSendUrl;

    private String smsBalanceUrl;

    private String smsQueryUrl;
}
