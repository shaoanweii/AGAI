package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/8 16:31
 * @描述:
 **/
public enum RuleWeight {

    A("A",1000L),
    B("B",700L),
    C("C",500L),
    D("D",300L),
    E("E",100L);



    private final String code;
    private final Long text;

    RuleWeight(String code, Long text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public Long getText() {
        return this.text;
    }

    public static RuleWeight getByCode(String code) {
        for (RuleWeight type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static RuleWeight getByText(Long text) {
        for (RuleWeight type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
