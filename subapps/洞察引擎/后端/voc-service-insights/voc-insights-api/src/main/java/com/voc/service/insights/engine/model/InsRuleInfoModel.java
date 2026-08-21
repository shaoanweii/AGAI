package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/13 10:05
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsRuleInfoModel extends Page  implements Serializable {
    /**
     * 规则id
     */
    @Schema(description = "规则id")
    private String id;
    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;
    /**
     * 规则编码
     */
    @Schema(description = "规则编码")
    private String ruleCode;
    /**
     * 管理客户编码
     */
    @Schema(description = "关联客户编码")
    private String clientCode;
    /**
     * 关联项目编码
     */
    @Schema(description = "关联项目编码")
    private String projectCode;
    /**
     * 内容类型 例如:文本、工单
     */
    @Schema(description = "内容类型 例如:文本、工单")
    private String contentType;
    /**
     * 处理阶段 前置处理:0 后置处理:1
     */
    @Schema(description = "处理阶段 前置处理:0 后置处理:1")
    private String processPhase;
    /**
     * 停用/启用表示 停用:0 启用:1
     */
    @Schema(description = "停用/启用表示 停用:0 启用:1")
    private String enable;
    /**
     * 规则id集合
     */
    @Schema(description = "规则id集合")
    private Set<String> ruleIds;
}
