package com.voc.service.insights.engine.enums;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 上午11:01
 * @描述:
 **/
public enum TagAttribute {
    CATEGORY("Category","标签分类",2),
    FINAL_LABEL("FinalLabel","末级标签",1),
    FINAL_CATEGORY("FinalCategory","末级分类",1)
    ;



    private final String code;
    private final String text;
    private final Integer level;

    TagAttribute(String code, String text,Integer level) {
        this.code = code;
        this.text = text;
        this.level = level;
    }

    public String getCode() {
        return this.code;
    }

    public String getText() {
        return this.text;
    }

    public Integer getLevel(){
        return this.level;
    }

    public static TagAttribute getByCode(String code) {
        for (TagAttribute type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static TagAttribute getByText(String text) {
        for (TagAttribute type : values()) {
            if (type.getText().equals(text)) {
                return type;
            }
        }
        return null;
    }
}
