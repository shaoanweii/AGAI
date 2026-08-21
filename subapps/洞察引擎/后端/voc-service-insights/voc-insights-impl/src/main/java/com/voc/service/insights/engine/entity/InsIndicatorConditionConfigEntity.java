package com.voc.service.insights.engine.entity;

import lombok.Data;

/**
 * 指标条件配置实体类
 * 对应数据库表：ins_indicator_condition_config
 */
@Data
public class InsIndicatorConditionConfigEntity {
    
    /**
     * 主键ID
     */
    private String id;
    
    /**
     * 指标名称
     */
    private String indicatorName;
    
    /**
     * 指标编码
     */
    private String indicatorCode;
    
    /**
     * 指标类型名称
     */
    private String typeName;
    
    /**
     * 指标类型编码
     */
    private String typeCode;
    
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
    
    /**
     * 给哪个用(1、单点；2、批量；）多个逗号分隔
     */
    private String canuse;
}
