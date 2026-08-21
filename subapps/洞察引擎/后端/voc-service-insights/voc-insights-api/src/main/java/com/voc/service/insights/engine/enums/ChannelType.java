package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/11 下午1:42
 * @描述:
 **/
public enum ChannelType {

    CATEGORY("Category","分类"),
    CHANNEL("Channel","渠道");

    private final String code;
    private final String text;

    ChannelType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static ChannelType getByCode(String code) {
        for (ChannelType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static ChannelType getByText(String text) {
        for (ChannelType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
