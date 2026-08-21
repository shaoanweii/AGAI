package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.insights.engine.model.InsClosedRuleModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 闭环规则主表实体类
 */
@TableName("ins_closed_rule")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsClosedRuleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 规则主键（60位字符串，业务层生成）
     */
    @TableId
    private String ruleId;

    /**
     * 规则名称，如“单点_口碑舆情_xxx”
     */
    private String ruleName;

    /**
     * 数据来源，如：["pd_post_bdtb", "pd_post_zgqczlw_ts", "pd_post_zgqcw"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> dataSource;

    /**
     * 规则类型：single=单点，batch=批量
     */
    private String ruleType;

    /**
     * 分类ID（60位字符串，关联分类表主键）
     */
    private String categoryType;

    /**
     * 分类名称
     */
    @TableField(exist = false)
    private String categoryTypeName;

    /**
     * 品牌编码
     */
    private String brandCode;

    /**
     * 品牌名称
     */
    @TableField(exist = false)
    private String brandName;

    /**
     * 事件等级，如“S”
     */
    private String eventLevel;

    /**
     * 处理优先级，如“exigency(紧急)”
     */
    private String processPriority;

    /**
     * 审核方式：manual=手动，auto=自动
     */
    private String auditMethod;

    /**
     * 审核部门（仅当audit_method=manual时有效）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleDept auditDepartment;

    /**
     * 审核人（仅当audit_method=manual时有效）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleUser auditor;

    /**
     * 主责部门
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleDept mainDepartment;

    /**
     * 主责人
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleUser mainResponder;

    /**
     * 抄送人员（可多选）
     */
    @TableField(typeHandler = JacksonTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private List<InsClosedRuleModel.InsClosedRuleCcPersonnel> ccPersonnel;

    /**
     * 确认方式：manual=手动，auto=自动
     */
    private String confirmMethod;

    /**
     * 确认部门（仅当confirm_method=manual时有效）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleDept confirmDepartment;

    /**
     * 确认人（仅当confirm_method=manual时有效）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleUser confirmer;

    /**
     * 规则是否启用
     */
    private String isEnabled;

    /**
     * 规则版本号，每次更新自增
     */
    private Integer version;

    /**
     * 创建人
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleUser creator;

    /**
     * 最后更新人
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleUser updater;

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
}