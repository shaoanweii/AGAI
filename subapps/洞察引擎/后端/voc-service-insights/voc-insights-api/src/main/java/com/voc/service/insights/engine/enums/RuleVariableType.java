package com.voc.service.insights.engine.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum RuleVariableType {

    VariableValue("value","变量值"),
    TextLength("textLength","文本长度");

    private final String code;
    private final String text;

    RuleVariableType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuleVariableType getByCode(String code) {
        for (RuleVariableType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
