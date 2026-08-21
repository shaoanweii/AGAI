package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/10 15:55
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_regulation_condition_config")
public class InsRegulationConditionConfigEntity implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 条件名称
     */
    private String name;

    /**
     * 条件编码
     */
    private String code;

    /**
     * 逻辑运算符名称
     */
    private String logicalOperatorName;

    /**
     * 逻辑运算符编码
     */
    private String logicalOperatorCode;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 统计方式名称
     */
    private String countingName;

    /**
     * 统计方式编码
     */
    private String countingCode;

    /**
     * 适用范围：1 仅给单点用、2 仅给批量用、1,2 两个都能用
     */
    private String canuse;

    /**
     * 排序字段
     */
    private Integer sortOrder;
}
