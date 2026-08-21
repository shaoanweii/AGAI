package com.voc.service.analysis.enums;

import java.util.Objects;

public enum UserJourneyTypeEnum {

    REPURCHASE("Repurchase", "复购"),
    BUYCAR("BuyCar", "购买"),
    CARSELECTION("CarSelection", "选择"),
    KNOW("know", "认知"),
    USECAR("UseCar", "拥车");

    public   String type;
    public   String text;

    UserJourneyTypeEnum(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getType() {
        return this.type;
    }

    public String getText() {
        return this.text;
    }

    public static UserJourneyTypeEnum getByCode(String text) {
        for (UserJourneyTypeEnum type : values()) {
            if (Objects.equals(type.getText(), text)) {
                return type;
            }
        }
        return REPURCHASE;
    }
}

