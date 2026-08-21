package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/4 下午8:18
 * @描述:
 **/
public enum EnergyCategoryEnum {
    FuelVehicle("FuelVehicle","燃油车"),
    EV("EV","新能源"),
    OtherEnergy("OtherEnergy","其他");

    private final String code;
    private final String text;

    EnergyCategoryEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static EnergyCategoryEnum getByCode(String code) {
        for (EnergyCategoryEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static EnergyCategoryEnum getByText(String text) {
        for (EnergyCategoryEnum type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
