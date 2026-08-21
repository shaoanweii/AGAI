package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/17 下午3:20
 * @描述:
 **/
public enum DataResourceType {
    DATA_RESOURCE_CUSTOM("custom","定制"),
    DATA_RESOURCE_GENERAL("general","标准"),
    DATA_RESOURCE_CLOSED_LOOP("closedLoop","闭环规则"),
    DATA_RESOURCE_RULE("rule","规则词库"),
    DATA_RESOURCE_ACCOUNT("account","账号词库"),

    ;

    private final String code;
    private final String text;

    DataResourceType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static DataResourceType getByCode(String code) {
        for (DataResourceType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static DataResourceType getByText(String text) {
        for (DataResourceType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
