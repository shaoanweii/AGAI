package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/12/26 下午3:23
 * @描述:
 **/
public enum LargeDigitaFilesType {

    DATA_SOURCE_API_RAW_DATA("DSAPIRawData","系统集成-原始数据"),
    DATA_SOURCE_API_RAW_RESULT_DATA("DSAPIResultData","系统集成-结果数据"),
    PROJECT_RAW_DATA("ProRawData","项目-原始数据"),
    PROJECT_RAW_RESULT_DATA("ProResultData","项目-结果数据"),
    ;

    private final String code;
    private final String text;

    LargeDigitaFilesType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static LargeDigitaFilesType getByCode(String code) {
        for (LargeDigitaFilesType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static LargeDigitaFilesType getByText(String text) {
        for (LargeDigitaFilesType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
