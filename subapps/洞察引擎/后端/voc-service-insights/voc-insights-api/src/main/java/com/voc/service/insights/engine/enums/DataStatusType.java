package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/25 上午10:26
 * @描述:
 **/
public enum DataStatusType {

    PENDING("0","待处理"),

    PROCESSING("1","处理中"),

    PROCESSED("2","已完成"),

    FAILURE("-1","处理失败");

    private final String code;
    private final String text;

    DataStatusType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static DataStatusType getByCode(String code) {
        for (DataStatusType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static DataStatusType getByText(String text) {
        for (DataStatusType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
