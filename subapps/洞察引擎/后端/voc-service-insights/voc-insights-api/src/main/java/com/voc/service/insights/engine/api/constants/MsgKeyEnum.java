package com.voc.service.insights.engine.api.constants;

import lombok.Getter;

/**
 * @author leiww
 */
@Getter
public enum MsgKeyEnum {

    key1("1", "客户："),
    key2("2", "任务名称："),
    key3("3", "告警数据源："),
    key4("4", "告警时间："),
    key5("5", "告警等级："),
    key6("6", "处理时效："),
    key7("7", "请及时登录智数洞察引擎系统处理告警");

    private final String code;

    private final String name;

    MsgKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
