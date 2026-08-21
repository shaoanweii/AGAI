package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/4 下午7:31
 * @描述:
 **/
public enum CarType {
    Sedan("Sedan","轿车"),
    SUV("SUV","SUV"),
    MPV("MPV","MPV"),
    Microvan("Microvan","微面"),
    SportsVehicle("SportsVehicle","跑车"),
    PickupTruck("PickupTruck","皮卡"),
    Other("Other","其他"),
    LightTruck("LightTruck","轻卡"),
    LightBus("LightBus","轻客"),
    MicroTruck("MicroTruck","微卡");

    private final String code;
    private final String text;

    CarType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static CarType getByCode(String code) {
        for (CarType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static CarType getByText(String text) {
        for (CarType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
