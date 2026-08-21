package com.voc.service.insights.engine.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 闭环规则完整模型（包含条件配置和预警配置）
 */
@Data
public class InsClosedRuleModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 规则主键（60位字符串，业务层生成）
     */
    @Schema(description = "规则主键, 更新时必填")
    @Size(max = 60, message = "规则主键长度不能超过60个字符")
    private String ruleId;

    /**
     * 规则名称，如“单点_口碑舆情_xxx”
     */
    @Schema(description = "规则名称", required = true)
    @Size(max = 30, message = "规则名称长度不能超过30个字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$", message = "规则名称只能包含中文、英文、数字")
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /**
     * 数据来源，如：["pd_post_bdtb", "pd_post_zgqczlw_ts", "pd_post_zgqcw"]
     */
    @Schema(description = "数据来源", required = true)
    @NotEmpty(message = "数据来源不能为空")
    private List<String> dataSource;

    /**
     * 规则类型：single=单点，batch=批量
     */
    @Schema(description = "规则类型,数据字典-closedRuleType", required = true)
    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    /**
     * 分类ID（60位字符串，关联分类表主键）
     */
    @Schema(description = "分类ID", required = true)
    @NotBlank(message = "分类ID不能为空")
    private String categoryType;

    @Schema(description = "分类名称")
    private String categoryTypeName;

    /**
     * 品牌编码
     */
    @Schema(description = "品牌编码", required = true)
    @NotBlank(message = "品牌编码不能为空")
    private String brandCode;

    /**
     * 品牌名称
     */
    @Schema(description = "品牌名称")
    private String brandName;

    /**
     * 事件等级，如“S”
     */
    @Schema(description = "事件等级,数据字典-closedRuleLevel", required = true)
    @NotBlank(message = "事件等级不能为空")
    private String eventLevel;

    /**
     * 处理优先级，如“p0(紧急)”
     */
    @Schema(description = "处理优先级,数据字典-closedRulePriority", required = true)
    @NotBlank(message = "处理优先级不能为空")
    private String processPriority;

    /**
     * 审核方式：manual=手动，auto=自动
     */
    @Schema(description = "审核方式,数据字典-closedRuleAuditMethod", required = true)
    @NotBlank(message = "审核方式不能为空")
    private String auditMethod;

    /**
     * 审核部门
     */
    @Schema(description = "审核部门")
    @Valid
    @NotNull(message = "审核部门不能为空")
    private InsClosedRuleDept auditDepartment;

    /**
     * 审核人（仅当audit_method=manual时有效）
     */
    @Schema(description = "审核人")
    @Valid
    @NotNull(message = "审核人不能为空")
    private InsClosedRuleUser auditor;

    /**
     * 主责部门
     */
    @Schema(description = "主责部门", required = true)
    @NotNull(message = "主责部门不能为空")
    @Valid
    private InsClosedRuleDept mainDepartment;

    /**
     * 主责人
     */
    @Schema(description = "主责人", required = true)
    @NotNull(message = "主责人不能为空")
    @Valid
    private InsClosedRuleUser mainResponder;

    /**
     * 抄送人员（可多选）
     */
    @Schema(description = "抄送人员（可多选）")
    @Valid
    @Size(max = 500, message = "抄送人员长度不能超过500个")
    private List<InsClosedRuleCcPersonnel> ccPersonnel;

    /**
     * 确认方式：manual=手动，auto=自动
     */
    @Schema(description = "确认方式,数据字典-closedRuleConfirmMethod", required = true)
    @NotBlank(message = "确认方式不能为空")
    private String confirmMethod;

    /**
     * 确认部门
     */
    @Schema(description = "确认部门")
    @Valid
    private InsClosedRuleDept confirmDepartment;

    /**
     * 确认人（仅当confirm_method=manual时有效）
     */
    @Schema(description = "确认人")
    @Valid
    private InsClosedRuleUser confirmer;

    /**
     * 规则是否启用
     */
    @Schema(description = "规则是否启用,数据字典-closedRuleEnabledStatus", required = true)
    @NotBlank(message = "规则是否启用不能为空")
    private String isEnabled;

    /**
     * 规则版本号，每次更新自增
     */
    private Integer version;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private InsClosedRuleUser creator;

    /**
     * 最后更新人
     */
    @Schema(description = "最后更新人")
    private InsClosedRuleUser updater;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后更新时间（自动更新）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 条件配置列表
    @Schema(description = "条件配置列表", required = true)
    @NotEmpty(message = "条件配置列表不能为空")
    @Valid
    private List<InsClosedRuleConditionModel> conditions;

    // 预警配置
    @Schema(description = "预警配置")
    @Valid
    private InsClosedRuleAlertModel ruleAlert;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InsClosedRuleUser implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 用户ID
         */
        @Schema(description = "用户ID", required = true)
        @NotNull(message = "用户ID不能为空")
        private String id;

        /**
         * 工号
         */
        @Schema(description = "工号", required = true)
        @NotNull(message = "工号不能为空")
        private String employeeId;

        /**
         * 用户姓名
         */
        @Schema(description = "用户姓名", required = true)
        @NotNull(message = "用户姓名不能为空")
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InsClosedRuleDept implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 组织ID
         */
        @Schema(description = "组织ID", required = true)
        @NotNull(message = "组织ID不能为空")
        private String id;

        /**
         * 部门编号
         */
        @Schema(description = "部门编号", required = true)
        @NotNull(message = "部门编号不能为空")
        private String deptNo;

        /**
         * 部门名称
         */
        @Schema(description = "部门名称", required = true)
        @NotNull(message = "部门名称不能为空")
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InsClosedRuleCcPersonnel implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 组织ID
         */
        @Schema(description = "组织ID", required = true)
        @NotNull(message = "抄送人组织ID不能为空")
        private String id;

        /**
         * 部门编号
         */
        @Schema(description = "部门编号", required = true)
        @NotNull(message = "抄送人部门编号不能为空")
        private String deptNo;

        /**
         * 部门名称
         */
        @Schema(description = "部门名称", required = true)
        @NotNull(message = "抄送人部门名称不能为空")
        private String deptName;

        /**
         * 是否部门全部人员
         */
        @Schema(description = "是否部门全部人员", required = true)
        @NotNull(message = "抄送人是否部门全部人员不能为空")
        private Boolean isAll;

        /**
         * 用户ID
         */
        @Schema(description = "用户ID")
        private String userId;

        /**
         * 工号
         */
        @Schema(description = "工号")
        private String employeeId;

        /**
         * 用户姓名
         */
        @Schema(description = "用户姓名")
        private String userName;
    }
}