package com.voc.service.insights.engine.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum RuleConditionType {

    Value("value","值"),
//    Variable("variable","变量"),
    ResourceGroup("resourceGroup","资源组"),
    Regex("regex","正则表达式");

    private final String code;
    private final String text;

    RuleConditionType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuleConditionType getByCode(String code) {
        for (RuleConditionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
