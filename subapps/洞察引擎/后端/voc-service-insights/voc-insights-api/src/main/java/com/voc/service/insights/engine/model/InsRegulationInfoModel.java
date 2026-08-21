package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.constant.InsCommonConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 09:41
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time"),
        @SortField(source = "regulationWeight", targer = "regulation_weight"),
})
public class InsRegulationInfoModel extends Page implements Serializable {
    /**
     * id
     */
    @Schema(description = "id")
    private String id;
    /**
     * 客户id
     */
    @Schema(description = "客户id")
//    @Client
    @Builder.Default
    private String clientId = "0";
    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String name;
    /**
     * 规则描述
     */
    @Schema(description = "规则描述")
    private String description;

    /**
     * 处理阶段 前置处理:0 后置处理:1
     */
    @Schema(description = "处理阶段 前置处理:0 后置处理:1")
    private String processPhase;

    /**
     * 规则类型
     */
    @Schema(description = "规则类型")
    private String regulationType;

    /**
     * 内容类型 例如:文本、工单
     */
    @Schema(description = "内容类型")
    private String contentType;

    /**
     * 数据渠道
     */
    @Schema(description = "数据渠道")
    private List<String> channel;

    /**
     * 匹配规则
     */
    @Schema(description = "匹配规则 满足全部条件：and,满足任意条件:or")
    private String matchingRule;

    /**
     * 规则条件
     */
    @Schema(description = "规则条件")
    private List<InsRegulationDetailsModel> regulationConditions;
    /**
     * 规则执行动作
     */
    @Schema(description = "规则执行动作")
    private List<InsRegulationDetailsModel> regulationPerformAction;
    /**
     * 规则权重
     */
    @Schema(description = "规则权重")
    private String regulationWeight;
    @Builder.Default
    private String regulationClassify = InsCommonConstant.REGULATION_CUSTOM;
    @Schema(description = "启用状态 停用:0 启用:1 默认0")
    private String status;

    /**
     * 规则id集合
     */
    @Schema(description = "规则id集合")
    private Set<String> ruleIds;

    private String resourceGroupId;
    private List<String> regulationTypes;
    private List<String> contentTypes;
    private List<String> processPhases;
    private List<String> statusList;

}
