package com.voc.service.insights.engine.api.constants;

import lombok.Getter;


@Getter
public enum IncreaseTypeEnum {
    MODEL_IDENTIFIED("1", "模型识别"),
    MANUAL_ADDITION("2", "手动添加");

    private final String code;

    private final String name;

    IncreaseTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static IncreaseTypeEnum getByCode(String code) {
        for (IncreaseTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return MODEL_IDENTIFIED;
    }

    public static boolean containsKey(String key) {
        for (IncreaseTypeEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
