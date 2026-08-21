package com.voc.service.insights.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 15:39
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRegulationDetailsModel  implements Serializable {
    /**
     * id
     */
    private String id;
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
    /**
     * 序号
     */
    private String serialNumber;

}
