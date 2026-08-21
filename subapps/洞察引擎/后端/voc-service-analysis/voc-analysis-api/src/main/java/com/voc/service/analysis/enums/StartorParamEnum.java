package com.voc.service.analysis.enums;

import cn.hutool.core.util.StrUtil;

public enum StartorParamEnum {
    CLIENT_ID("clientId", "客户ID"),
    BEGIN_TIME("beginTime", "开始时间"),
    END_TIME("endTime", "结束时间"),
    DATE_TYPE("dateType", "时间类型createTime/syncTime"),
    TAG_TYPE("tagType", "标签类型emotion/quality"),
    GROUP_TYPE("groupType", "聚类维度，业务dimensionEmotion、意向intentionType"),
    DEST_CLASS("destClass", "目标类"),
    BASIC_CLASS("basicClass", "基础类、统计类"),
    STATISTIC_TYPE("statisticType", "统计类型d、w、m、q、y"),
    BUS_TYPE("busType", "统计维度用户user、提及量tag"),
    METHOD_TYPE("methodType", "操作类型：ods、dwd、dws"),
    METHOD_NAME("methodName", "操作类型：statistic"),
    BUS_400("400", "400渠道"),
    BUS_DCC_DA("dcc-da", "dcc-da"),
    BUS_DCC_NGA("dcc-nga", "dcc-nga"),
    BUS_APP_ARTICLE("app-article", "app社区文章"),
    BUS_APP_COMMENTS("app-comments", "app社区评论"),
    BUS_ESOCIAL_MESSAGE("esocial-message", "官方微信-私信"),
    BUS_ESOCIAL_COMMENTS("esocial-comments", "官方微信-评论"),
    BUS_CHAT_MESSAGE("chat-message", "企微"),
    BUS_OFFICIAL_ACCOUNTS("official-accounts", "官方账号-两微"),
    BUS_COMPLAIN("complain", "投诉"),
    BUS_REPAIR_PART("repair-part", "维修零部件"),
    BUS_SOCIAL_LISTENING("social-listening", "公域"),
    EMPTY_OTHER("default", ""),
    STATISTIC_DATE_D("d", "统计维度-天"),
    STATISTIC_DATE_W("w", "统计维度-周"),
    STATISTIC_DATE_M("m", "统计维度-月"),
    STATISTIC_DATE_Q("q", "统计维度-季"),
    STATISTIC_DATE_Y("y", "统计维度-年"),
    IS_RISK("isRisk", "操作类型risk"),
    BRAND("brand", "品牌"),

    METHOD_TYPE_ALL("all", "所有的渠道"),

    CHANNEL_SOCIAL("subject", "公域"),
    ;

    private String name;
    private String value;

    StartorParamEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public static StartorParamEnum targetEnum(String name) {
        for (StartorParamEnum targetEnum : values()) {
            if (StrUtil.equalsIgnoreCase(targetEnum.getName(), name)) {
                return targetEnum;
            }
        }
        return EMPTY_OTHER;
    }
}
