package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 下午5:52
 * @描述:
 **/
public enum ResourceGroupType {

    CUSTOM("custom","定制"),
    GENERAL("general","标准");

    private final String code;
    private final String text;

    ResourceGroupType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static ResourceGroupType getByCode(String code) {
        for (ResourceGroupType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static ResourceGroupType getByText(String text) {
        for (ResourceGroupType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
