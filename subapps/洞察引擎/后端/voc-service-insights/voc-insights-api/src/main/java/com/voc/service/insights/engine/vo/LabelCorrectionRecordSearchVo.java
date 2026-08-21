package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelCorrectionRecordSearchVo implements Serializable {
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
    @Schema(description = "关联客户名称")
    private String clientName;
    /**
     * 关联项目编码
     */
    @Schema(description = "关联项目编码")
    private String projectCode;
    /**
     * 关联项目名称
     */
    @Schema(description = "关联项目名称")
    private String projectName;
    /**
     * 关联表名
     */
    @Schema(description = "关联表名")
    private String relevancyTable;
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
     * 规则描述
     */
    @Schema(description = "规则描述")
    private String description;
    /**
     * 停用/启用表示 停用:0 启用:1
     */
    @Schema(description = "停用/启用表示 停用:0 启用:1")
    private String enable;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


}
