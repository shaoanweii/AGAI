package com.voc.service.analysis.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/4/3 15:47
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysValidAttributeModel implements Serializable {
    /**
     * 规则id
     */
    private String regulationId;

    /**
     * 字段
     */
    private String fieldName;
    /**
     * 变量值
     */
    private String variableValue;
    /**
     * 逻辑运算符
     */
    private String logicalOperator;
    /**
     * 条件类型
     */
    private String conditionType;
    /**
     * 条件详情
     */
    private String conditionDetail;

    private List<String> conditionDetailList;
    /**
     * 详情类型 规则条件:0 规则执行动作:1
     */
    private String detailType;
}
