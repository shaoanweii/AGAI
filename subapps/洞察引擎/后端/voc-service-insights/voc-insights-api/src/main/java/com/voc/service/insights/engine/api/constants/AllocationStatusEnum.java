package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

@Getter
public enum AllocationStatusEnum {

    DISABLED("0", "未分配"),
    ENABLED("1", "已分配");


    private final String code;

    private final String name;

    AllocationStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AllocationStatusEnum getByCode(String code) {
        for (AllocationStatusEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return DISABLED;
    }

    public static boolean containsKey(String key) {
        for (AllocationStatusEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
