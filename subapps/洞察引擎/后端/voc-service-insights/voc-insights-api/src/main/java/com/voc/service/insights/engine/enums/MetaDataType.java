package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/29 上午10:58
 * @描述:
 **/
public enum MetaDataType {

    ARTICLE("text","文章"),
    COMMENT("comment","评论"),
    DIALOGUE("dialogue","对话");

    private final String code;
    private final String text;

    MetaDataType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static MetaDataType getByCode(String code) {
        for (MetaDataType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static MetaDataType getByText(String text) {
        for (MetaDataType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
