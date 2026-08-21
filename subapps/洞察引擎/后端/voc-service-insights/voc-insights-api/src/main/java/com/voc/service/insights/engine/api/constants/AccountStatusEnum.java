package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

@Getter
public enum AccountStatusEnum {

    ENABLED("0", "已禁用"),
    DISABLED("1", "已启用");

    private final String code;

    private final String name;

    AccountStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AccountStatusEnum getByCode(String code) {
        for (AccountStatusEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return ENABLED;
    }

    public static boolean containsKey(String key) {
        for (AccountStatusEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
