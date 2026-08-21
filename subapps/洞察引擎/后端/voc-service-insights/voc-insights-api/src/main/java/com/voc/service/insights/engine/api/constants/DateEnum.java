package com.voc.service.insights.engine.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 时间筛选
 *
 * @author lww
 */
@Getter
@AllArgsConstructor
public enum DateEnum {

    ALL_TIME("all_time", "All Time", "全部时间"),
    TODAY("today", "DATE(create_time) = DATE('" + LocalDateTime.now() + "')", "今天"),
    YESTERDAY("yesterday", "Yesterday", "昨天"),
    LAST_7_DAYS("last_7_days", "create_time >= DATE('" + LocalDateTime.now() + "') - INTERVAL 7 DAY", "过去7天"),
    LAST_30_DAYS("last_30_days", "YEAR(create_time) = YEAR(" + LocalDateTime.now() + ") AND MONTH(create_time) = MONTH('" + LocalDateTime.now() + "')", "过去30天"),
    LAST_60_DAYS("last_60_days", "Last 60 Days", "过去60天"),

    ;


    private final String code;

    private final String value;

    private final String name;

    public static DateEnum getByCode(String code) {
        for (DateEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return TODAY;
    }

    public static boolean containsKey(String key) {
        for (DateEnum type : values()) {
            if (type.getCode().equals(key)) {
                return true;
            }
        }
        return false;
    }

}
