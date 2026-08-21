package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

/**
 * @author leiww
 */
@Getter
public enum PushStatusEnum {

    NOT_PUSHED(0, "未推送"),
    PUSH_COMPLETED(1, "已推送完成"),
    UNTREATED(-1, "未处理");

    private final Integer code;

    private final String name;

    PushStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
