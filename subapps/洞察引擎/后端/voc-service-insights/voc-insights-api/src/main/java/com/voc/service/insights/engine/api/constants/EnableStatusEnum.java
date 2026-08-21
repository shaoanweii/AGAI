package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

@Getter
public enum EnableStatusEnum {

    ENABLED("0", "已禁用"),
    DISABLED("1", "已启用");

    private final String code;

    private final String name;

    EnableStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static EnableStatusEnum getByCode(String code) {
        for (EnableStatusEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return ENABLED;
    }

    public static boolean containsKey(String key) {
        for (EnableStatusEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
