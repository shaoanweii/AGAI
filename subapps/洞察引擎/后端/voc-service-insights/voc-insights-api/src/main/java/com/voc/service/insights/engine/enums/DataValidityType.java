package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/25 上午10:23
 * @描述:
 **/
public enum DataValidityType {

    VALID("1", "有效数据"),
    INVALID("0", "无效数据");

    private final String code;
    private final String text;

    DataValidityType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static DataValidityType getByCode(String code) {
        for (DataValidityType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static DataValidityType getByText(String text) {
        for (DataValidityType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
