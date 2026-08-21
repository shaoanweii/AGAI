package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * 闭环规则查询模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsClosedRuleQueryModel extends Page implements Serializable {

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 规则分类
     */
    @Schema(description = "规则分类-统一资源组分类")
    private String categoryType;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型(数据字典-closedRuleType)")
    private String ruleType;


    /**
     * 品牌编码
     */
    @Schema(description = "品牌编码")
    private String brandCode;

    /**
     * 事件等级
     */
    @Schema(description = "事件等级(数据字典-closedRuleLevel)")
    private String eventLevel;

    /**
     * 处理优先级
     */
    @Schema(description = "处理优先级(数据字典-closedRulePriority)")
    private String processPriority;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用(数据字典-closedRuleEnabledStatus)")
    private String isEnabled;

}