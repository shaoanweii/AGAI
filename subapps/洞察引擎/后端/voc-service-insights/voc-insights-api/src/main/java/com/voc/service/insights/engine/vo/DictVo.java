package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DictVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 字典value
     */
    private String value;
    /**
     * 字典文本
     */
    private String text;
    /**
     * 特殊用途： JgEditableTable
     *
     * @return
     */
//	public String getTitle() {
//		return this.text;
//	}
    private List<?> childes;

    public DictVo() {
    }

    public DictVo(String value, String text) {
        this.value = value;
        this.text = text;
    }
}
