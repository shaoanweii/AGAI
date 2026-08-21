package com.voc.service.components.sms.service;

public interface SmsSendApi {
    Boolean sendCaptchaSms(String phoneNumber);

    Boolean sendLicenseSms(String phoneNumber, String license, String tenantCode, String validate);
}
