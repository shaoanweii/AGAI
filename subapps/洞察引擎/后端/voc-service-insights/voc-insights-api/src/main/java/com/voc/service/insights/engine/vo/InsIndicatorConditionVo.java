package com.voc.service.insights.engine.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 指标条件VO
 * 用于返回指标条件信息
 */
@Data
@Builder
public class InsIndicatorConditionVo {

    /**
     * 运算符名称
     */
    private String operatorName;

    /**
     * 运算符编码
     */
    private String operatorCode;

    /**
     * 值类型名称
     */
    private String valueTypeName;

    /**
     * 值类型编码
     */
    private String valueTypeCode;

    /**
     * 值格式（正整数、百分数等）
     */
    private String valueFormat;
}
