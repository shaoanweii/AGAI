package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/11 下午1:42
 * @描述:
 **/
public enum TagLibeType {

    BIZ("BIZ","业务标签"),
    QY("QY","品质"),
    ST("ST","场景标签"),
    PROD("PROD","产品"),
    SERVICE("SERVICE","服务"),
    USER_JOURNEY("JOUR","用户全旅途"),
    VRT("VRT","VRT"),
    CPT("CPT","CPT"),
    DOMAIN("CA","全领域业务"),
    COMMODITY_ATTR("PRO","商品化属性"),
    NPS("NPS","NPS"),
    ;

    private final String code;
    private final String text;

    TagLibeType(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }


    public static TagLibeType getByCode(String code) {
        for (TagLibeType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    public static TagLibeType getByText(String text) {
        for (TagLibeType type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
