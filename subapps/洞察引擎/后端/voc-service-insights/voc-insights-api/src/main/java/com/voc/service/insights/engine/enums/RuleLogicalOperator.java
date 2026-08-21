package com.voc.service.insights.engine.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum RuleLogicalOperator {

    Empty("empty","为空"),
    NotEmpty("notEmpty","不为空"),
    Contain("contain","包含"),
    NotContain("notContain","不包含"),
    Equals("equals","=="),
    NotEquals("notEquals","!="),
    GreaterThen("greaterThen",">"),
    LessThen("lessThen","<"),
    GreaterThenOrEqual("greaterThenOrEqual",">="),
    LessThenOrEqual("lessThenOrEqual","<=")
    ;

    private final String code;
    private final String text;

    RuleLogicalOperator(String code,String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuleLogicalOperator getByCode(String code) {
        for (RuleLogicalOperator type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
