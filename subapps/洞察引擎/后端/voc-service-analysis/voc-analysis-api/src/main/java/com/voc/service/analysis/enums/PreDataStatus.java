package com.voc.service.analysis.enums;

import java.util.Objects;

/**
 * @Title: RuleLogicalOperator
 * @Package: com.voc.service.insights.engine.constant
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/7 15:06
 * @Version:1.0
 */
public enum PreDataStatus {


    EXCEPTION_DATA(-1, "失败数据"),
    DENOISING_DATA(1, "去噪数据"),
    MARKED_DATA(2, "已打标数据"),
    MISS_DATA(3, "未打标数据");

    private final Integer code;
    private final String text;

    PreDataStatus(Integer code, String text) {
        this.code = code;
        this.text = text;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static PreDataStatus getByCode(Integer code) {
        for (PreDataStatus type : values()) {
            if (Objects.equals(type.getCode(), code)) {
                return type;
            }
        }
        return MARKED_DATA;
    }
}
