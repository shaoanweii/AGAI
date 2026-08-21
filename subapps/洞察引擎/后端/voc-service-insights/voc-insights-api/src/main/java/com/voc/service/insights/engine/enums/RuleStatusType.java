package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/8 16:54
 * @描述:
 **/
public enum RuleStatusType {

    Disabled("Disabled","已禁用"),
    Enabled("Enabled","已启用"),
    NotEnabled("NotEnabled","未启用");


    private final String code;
    private final String text;

    RuleStatusType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuleStatusType getByCode(String code) {
        for (RuleStatusType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static RuleStatusType getByText(String text) {
        for (RuleStatusType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
