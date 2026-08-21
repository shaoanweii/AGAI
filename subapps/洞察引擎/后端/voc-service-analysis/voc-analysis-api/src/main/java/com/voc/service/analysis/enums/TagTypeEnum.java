package com.voc.service.analysis.enums;

import java.util.Objects;

public enum TagTypeEnum {

    TAG_PROD("PROD", "产品"),
    TAG_SERVICE("SERVICE", "服务"),
    TAG_QY("QY", "品质");

    public String type;
    public String text;

    TagTypeEnum(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getType() {
        return this.type;
    }

    public String getText() {
        return this.text;
    }


    public static TagTypeEnum getByCode(String code) {
        for (TagTypeEnum type : values()) {
            if (Objects.equals(type.getType(), code)) {
                return type;
            }
        }
        return TAG_PROD;
    }

}

