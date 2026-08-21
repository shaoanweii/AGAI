package com.voc.service.data.integration.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum ErrorDataMsgEnums {

    OriginalDataIsEmpty("A","原数据为空"),
    PushServiceHasFailed("B","推送服务失败"),
    RequiredFieldValidationHasFailed("C","必填项校验失败"),
    RequiredFieldTypeValidationHasFailed("F","必填项数据类型校验失败"),
    RequiredExtFieldValidationHasFailed("E","必填项（扩展属性）校验失败"),
    RequiredExtFieldTypeValidationHasFailed("G","必填项（扩展属性）类型校验校验失败"),
    ChannelHasNotConfiguredRequiredFields("D","渠道未配置必填项"),
    FailedToParseOriginalData("H","解析原始数据失败");

    private final String code;
    private final String text;

    ErrorDataMsgEnums(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static ErrorDataMsgEnums getByCode(String code) {
        for (ErrorDataMsgEnums type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
