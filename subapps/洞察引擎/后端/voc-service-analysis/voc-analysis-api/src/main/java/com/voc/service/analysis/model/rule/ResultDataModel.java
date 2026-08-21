package com.voc.service.analysis.model.rule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ResultDataModel
 * @createTime 2024年03月15日 10:26
 * @Copyright cuick
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultDataModel implements Serializable {
    /**
     * 逻辑符
     */
    private String signOperation;
    private String targetAttr;
    private String value;

    /**
     * 正则表达式
     * 输入值
     * 资源组
     */
    private String contionType;
}
