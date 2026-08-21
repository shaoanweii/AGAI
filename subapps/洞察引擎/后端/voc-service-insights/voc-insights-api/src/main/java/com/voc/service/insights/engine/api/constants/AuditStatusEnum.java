package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

@Getter
public enum AuditStatusEnum {

    AUDIT("0", "待审核"),
    PASS("1", "通过"),
    REFUSE("2", "拒绝"),
    REVOKED("3", "已撤销");

    private final String code;

    private final String name;

    AuditStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AuditStatusEnum getByCode(String code) {
        for (AuditStatusEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return AUDIT;
    }

    public static boolean containsKey(String key) {
        for (AuditStatusEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
