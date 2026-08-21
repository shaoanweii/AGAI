package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

/**
 * @author leiww
 */
@Getter
public enum AlertLevelEnum {

    ONE_LEVEL("OneLevel", "1级"),
    SECOND_LEVEL("SecondLevel", "2级");

    private final String code;

    private final String name;

    AlertLevelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AlertLevelEnum getByCode(String code) {
        for (AlertLevelEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return ONE_LEVEL;
    }

    public static boolean containsKey(String key) {
        for (AlertLevelEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
