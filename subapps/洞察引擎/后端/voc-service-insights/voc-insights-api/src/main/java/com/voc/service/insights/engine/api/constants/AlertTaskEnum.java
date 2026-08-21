package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

/**
 * @author leiww
 */
@Getter
public enum AlertTaskEnum {
    /**
     * nlpData NLP数据
     * metaData 原始数据
     * notificationMsg 告警推送
     */
    NLP_DATA("nlpData", "NLP数据"),
    META_DATA("metaData", "原始数据"),
    PUSH_DATA("pushData", "数据推送");

    private final String code;

    private final String name;

    AlertTaskEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AlertTaskEnum getByCode(String code) {
        for (AlertTaskEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return META_DATA;
    }

    public static boolean containsKey(String key) {
        for (AlertTaskEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
