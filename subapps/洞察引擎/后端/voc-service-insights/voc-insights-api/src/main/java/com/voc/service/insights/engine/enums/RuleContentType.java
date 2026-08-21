package com.voc.service.insights.engine.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum RuleContentType {

    PreRule("text","文本"),
    PostRule("order","工单");

    private final String code;
    private final String text;

    RuleContentType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuleContentType getByCode(String code) {
        for (RuleContentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return PreRule;
    }
}
