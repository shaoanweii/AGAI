package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/11/11 上午11:01
 * @描述:
 **/
public enum LabelTypeEnum {

    PROD("PROD","prod","产品体验指数" ),
    SERVICE("SERVICE","srv","服务体验指数"),
    QY("QY","qy","品质体验指数");

    private final String code;
    private final String dbCode;
    private final String text;

    LabelTypeEnum(String code, String dbCode, String text) {
        this.code = code;
        this.dbCode = dbCode;
        this.text = text;
    }

    public String getDbCode() {
        return this.dbCode;
    }
    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static LabelTypeEnum getByCode(String code) {
        for (LabelTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }


    public static LabelTypeEnum getByText(String text) {
        for (LabelTypeEnum type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
