package com.voc.service.insights.engine.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum AlertDataType {

    META_DATA("metaData","原始数据"),
    PUSH_DATA("pushData","原始数据"),
    NLP_DATA("nlpData","NLP数据");

    private final String code;
    private final String text;

    AlertDataType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static AlertDataType getByCode(String code) {
        for (AlertDataType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
