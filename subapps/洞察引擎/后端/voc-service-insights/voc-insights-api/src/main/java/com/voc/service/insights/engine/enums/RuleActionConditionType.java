package com.voc.service.insights.engine.enums;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum RuleActionConditionType {
    //直接修改
    Modify("R01","修改"),
    //满足正则表达式是用 * 号代替
    Desensitization("R02","脱敏"),
    //数据为空时修改
    Supplement("R03","补充"),
    //删除内容中文字
    Clean("R04","清洗"),
    //删除整条数据
    FilterDistinct("R05","过滤");

    private final String code;
    private final String text;

    RuleActionConditionType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static RuleActionConditionType getByCode(String code) {
        for (RuleActionConditionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
