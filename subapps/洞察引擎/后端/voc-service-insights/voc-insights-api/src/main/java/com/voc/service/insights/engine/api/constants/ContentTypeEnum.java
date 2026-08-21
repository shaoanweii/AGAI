package com.voc.service.insights.engine.api.constants;

/**
 * consult-咨询opinion-意见反馈post cmt-帖子回评quest-问卷order-工单
 **/
public enum ContentTypeEnum {

    GUARANTOR("consult","咨询"),
    CORPORATION("opinion","意见反馈"),
    PERSONAGE("post_cmt","帖子回评"),
    PROWLER("quest","问卷"),
    OTHER("order","工单"),
    ;

    private final String code;
    private final String text;

    ContentTypeEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public static ContentTypeEnum getByCode(String code) {
        for (ContentTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return GUARANTOR;
    }

    public static ContentTypeEnum getByText(String text) {
        for (ContentTypeEnum type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
