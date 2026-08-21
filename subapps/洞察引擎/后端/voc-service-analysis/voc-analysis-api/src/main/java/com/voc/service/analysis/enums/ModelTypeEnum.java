package com.voc.service.analysis.enums;

import java.util.Objects;

public enum ModelTypeEnum {

    AI_OFFLINE(1, "LLM离线"),      //大模型离线
    AI_ONLINE(2, "LLM实时"),      //大模型实时
    CLUSTERING_LLM(3, "聚类LLM");  //小模型

    public   Integer type;
    public   String text;

    ModelTypeEnum(Integer type, String text) {
        this.type = type;
        this.text = text;
    }

    public Integer getType() {
        return this.type;
    }

    public String getText() {
        return this.text;
    }

    public static ModelTypeEnum getByCode(Integer code) {
        for (ModelTypeEnum type : values()) {
            if (Objects.equals(type.getType(), code)) {
                return type;
            }
        }
        return AI_ONLINE;
    }
}

