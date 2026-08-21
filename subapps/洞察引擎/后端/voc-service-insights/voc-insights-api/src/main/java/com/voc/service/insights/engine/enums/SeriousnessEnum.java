package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/4 下午8:00
 * @描述:
 **/
public enum SeriousnessEnum {
    Higher("Higher","较高"),
    Middle("Middle","中"),
    Inferiority("Inferiority","较低"),
    Low("Low","低"),
    NotEvaluate("NotEvaluate","无法评估"),
    High("High","高");


    private final String code;
    private final String text;

    SeriousnessEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static SeriousnessEnum getByCode(String code) {
        for (SeriousnessEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static SeriousnessEnum getByText(String text) {
        for (SeriousnessEnum type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
