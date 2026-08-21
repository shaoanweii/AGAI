package com.voc.service.data.integration.services;

import com.voc.service.data.integration.config.PublicDomainConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TextProcessorService {
    @Autowired
    PublicDomainConfig config;

    /**
     * 脱敏手机号
     */
    public String maskPhoneNumbers(String text) {
        if (text == null) {
            return null;
        }
        return config.getPhonePattern().matcher(text).replaceAll("$1****$3");
    }

    /**
     * 脱敏身份证号
     */
    public String maskIdCard(String text) {
        if (text == null) {
            return null;
        }
        return config.getIdCardPattern().matcher(text).replaceAll("$1**********$3");
    }

    /**
     * 脱敏车牌号
     */
    public String maskLicensePlate(String text) {
        if (text == null) {
            return null;
        }
        return config.getLicensePlatePattern().matcher(text).replaceAll("$1$2$3****$5");
    }

    /**
     * 脱敏车架号（VIN码）
     */
    public String maskVIN(String text) {
        if (text == null) {
            return null;
        }
        return config.getVinPattern().matcher(text).replaceAll("XXXXXXXXXXXXX$1");
    }

}
