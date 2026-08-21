package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 规则预警推送配置表实体类
 */
@TableName("ins_closed_rule_alert")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsClosedRuleAlertEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 预警配置主键（60位字符串，业务层生成）
     */
    private String alertPushId;

    /**
     * 关联规则主表ID（60位字符串，唯一约束）
     */
    private String ruleId;

    /**
     * 预警周期：hourly=时，daily=日，weekly=周，monthly=月
     */
    private String alertType;

    /**
     * 预警频次，如“周期是时的 2 4 8 16，周期是日的 固定为0，周期是周的 1 2 3 ... 6 7，周期是月的 1 2 3 ... 30 31”
     */
    private String alertFrequency;

    /**
     * 预警时间，如“周期是时的直接为0，其余的都是时分秒 08:00:00”
     */
    private String alertTime;

    /**
     * 预警的cron表达式，如：0 0 0/2 * * ?
     */
    private String alertCron;

    /**
     * 预警渠道（多选，存储渠道标识数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> alertChannel;
}