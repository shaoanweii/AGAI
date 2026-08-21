package com.voc.service.analysis.enums;

import java.util.Objects;

public enum StatisticTypeStatus {


    day("d", "日"),
    week("w", "周"),
    moth("m", "月"),
    quarter("q", "季"),

    year("y", "年");

    private final String code;
    private final String text;

    StatisticTypeStatus(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static StatisticTypeStatus getByCode(String code) {
        for (StatisticTypeStatus type : values()) {
            if (Objects.equals(type.getCode(), code)) {
                return type;
            }
        }
        return day;
    }
}
