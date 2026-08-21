package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 规则条件配置表实体类（当前生效）
 */
@TableName("ins_closed_rule_condition")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsClosedRuleConditionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 条件主键（60位字符串，业务层生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
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
    @TableField(value = "`option`")
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
}