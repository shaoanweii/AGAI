package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/11/29 下午4:18
 * @描述:
 **/
public enum DataSourceAccessWay {

    UPLOAD("upload","本地上传"),
    API("api","系统集成");

    private final String code;
    private final String text;

    DataSourceAccessWay(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static DataSourceAccessWay getByCode(String code) {
        for (DataSourceAccessWay type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static DataSourceAccessWay getByText(String text) {
        for (DataSourceAccessWay type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
