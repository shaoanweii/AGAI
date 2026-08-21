package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

/**
 * @author leiww
 */
@Getter
public enum PushTypeEnum {

    FEI_SHU(1, "飞书"),
    DING_TALK(2, "钉钉"),
    WECHAT(3, "微信");

    private final Integer code;

    private final String name;

    PushTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
