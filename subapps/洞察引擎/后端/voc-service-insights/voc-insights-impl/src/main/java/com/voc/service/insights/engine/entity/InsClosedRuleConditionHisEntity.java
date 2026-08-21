package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.insights.engine.model.InsClosedRuleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则条件配置历史表实体类（记录变更）
 */
@TableName("ins_closed_rule_condition_his")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsClosedRuleConditionHisEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 条件主键（60位字符串，业务层生成）
     */
    private String conditionId;

    /**
     * 关联规则主表ID（60位字符串）
     */
    private String ruleId;

    /**
     * 条件类型，如“品牌车系”“意图”
     */
    private String conditionType;

    /**
     * 操作符，如“equal=等于”“in=包含”
     */
    private String operator;

    /**
     * 选项类型，如“选项”“值”“词库”
     */
    private String option;

    /**
     * value类型：string=字符串，array=数组
     */
    private String valueType;

    /**
     * 条件值（根据value_type存储不同结构）
     */
    private String value;

    /**
     * 条件排序（控制多条件展示顺序）
     */
    private Integer sortOrder;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 本次编辑操作人
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private InsClosedRuleModel.InsClosedRuleUser editUser;

    /**
     * 本次编辑时间（历史记录生成时间）
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editTime;
}