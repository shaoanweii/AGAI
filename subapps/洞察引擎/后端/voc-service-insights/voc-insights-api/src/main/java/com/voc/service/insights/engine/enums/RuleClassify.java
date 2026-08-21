package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/9 10:26
 * @描述:
 **/
public enum RuleClassify {

    REGULATION_CUSTOM("custom","定制规则"),
    REGULATION_GENERAL("general","标准规则");

    private final String code;
    private final String text;

    RuleClassify(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuleClassify getByCode(String code) {
        for (RuleClassify type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static RuleClassify getByText(String text) {
        for (RuleClassify type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
