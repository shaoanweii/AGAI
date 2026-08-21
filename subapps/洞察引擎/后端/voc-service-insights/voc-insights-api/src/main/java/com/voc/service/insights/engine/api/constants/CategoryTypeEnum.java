package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

@Getter
public enum CategoryTypeEnum {

    SERVICE_EFFICIENCY("serviceEfficiency", "服务效率/质量/态度","1"),
    MEDIA_RELATED("mediaRelated", "媒体相关","2"),
    PERSONAL_SAFETY("personalSafety", "人身安全","3"),
    ADMINISTRATION("administration", "行政部门","4"),
    OTHER("other", "其他","5");

    private final String code;

    private final String name;

    private final String sort;

    CategoryTypeEnum(String code, String name,String sort) {
        this.code = code;
        this.name = name;
        this.sort = sort;
    }

    public static CategoryTypeEnum getByCode(String code) {
        for (CategoryTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SERVICE_EFFICIENCY;
    }

    public static boolean containsKey(String key) {
        for (CategoryTypeEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
