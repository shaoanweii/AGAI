package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.LogicalOperatorType;
import com.voc.service.insights.engine.api.annotation.RuleCondition;
import com.voc.service.insights.engine.api.annotation.RuleVariable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 17:21
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegulationDetailsVo  implements Serializable {
    private String id;
    /**
     * 规则id
     */
    private String regulationId;

    /**
     * 字段
     */
    private String fieldName;
    private String fieldNameText;
    /**
     * 变量值
     */
    @RuleVariable
    private String variableValue;
    /**
     * 逻辑运算符
     */
    @LogicalOperatorType
    private String logicalOperator;
    /**
     * 条件类型
     */
    @RuleCondition
    private String conditionType;
    /**
     * 条件详情
     */
    private String conditionDetail;
    private String conditionDetailText;
    /**
     * 详情类型 规则条件:0 规则执行动作:1
     */
    private String detailType;

    /**
     * 序号
     */
    private String serialNumber;

}
