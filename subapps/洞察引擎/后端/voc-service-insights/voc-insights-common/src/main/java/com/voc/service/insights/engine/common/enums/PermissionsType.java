package com.voc.service.insights.engine.common.enums;

import lombok.Getter;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/29 11:49
 * @描述:
 **/
@Getter
public enum PermissionsType {

    READ("r"),
    WRITE("w")
    ;

    String type;


    PermissionsType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

}
