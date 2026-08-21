package com.voc.service.components.sms.tool;

import lombok.AllArgsConstructor;

/**
 * 短信模板枚举类
 * created by hanxm
 * date 2021-7-21
 */
@AllArgsConstructor
public enum SmsTemplate {

    VERITIFY_MESSAGE("【5+AI健康】{0}，该验证码 2 分钟内有效，请勿泄漏于他人，如非本人操作，请忽略此信息。"),
    LICENSE_MESSAGE("【5+AI健康】尊敬的平台用户您好，贵机构的激活码是{0}，租户代码为{1}，有效期至{2}，请及时激活，如过期，请于管理员联系。");

    private String content;

    public String getContent() {
        return content;
    }
}
