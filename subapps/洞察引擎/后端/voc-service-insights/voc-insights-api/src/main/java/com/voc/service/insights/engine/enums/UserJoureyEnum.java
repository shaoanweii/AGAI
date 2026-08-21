package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/4 下午7:31
 * @描述:
 **/
public enum UserJoureyEnum {
    know("know","认知"),
    CarSelection("CarSelection","选择"),
    BuyCar("BuyCar","购买"),
    UseCar("UseCar","拥车"),
    Repurchase("Repurchase","复购");

    private final String code;
    private final String text;

    UserJoureyEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static UserJoureyEnum getByCode(String code) {
        for (UserJoureyEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static UserJoureyEnum getByText(String text) {
        for (UserJoureyEnum type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
