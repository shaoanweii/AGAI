package com.voc.service.insights.engine.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum RuLerelations {

    And("and","满足全部条件(AND)"),
    Or("or","满足任意条件(OR)");

    private final String code;
    private final String text;

    RuLerelations(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuLerelations getByCode(String code) {
        for (RuLerelations type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
