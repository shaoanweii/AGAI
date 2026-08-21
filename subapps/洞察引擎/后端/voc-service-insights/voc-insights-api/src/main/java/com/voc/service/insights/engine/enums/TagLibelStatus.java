package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/14 下午7:35
 * @描述:
 **/
public enum TagLibelStatus {
    ENABLED("1","已启用"),
    DISABLED("0","已停用"),
    ;

    private final String code;
    private final String text;

    TagLibelStatus(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static TagLibelStatus getByCode(String code) {
        for (TagLibelStatus type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static TagLibelStatus getByText(String text) {
        for (TagLibelStatus type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
