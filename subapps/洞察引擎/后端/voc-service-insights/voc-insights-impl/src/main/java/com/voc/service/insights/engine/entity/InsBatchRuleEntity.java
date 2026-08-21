package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 批量规则实体
 * 对应数据库表：ins_batch_rule
 */
@Data
@TableName("ins_batch_rule")
public class InsBatchRuleEntity {

    /**
     * 规则ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 品牌编码
     */
    private String brandCode;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 预警周期：hourly/daily/weekly/monthly
     */
    private String alertType;

    /**
     * 预警频次
     */
    private String alertFrequency;

    /**
     * 预警时间
     */
    private String alertTime;

    /**
     * 预警cron表达式
     */
    private String alertCron;

    /**
     * 维度配置（JSON格式）
     */
    private String dimensionConfig;

    /**
     * 指标配置（JSON格式）
     */
    private String indicatorConfig;

    /**
     * 处理优先级
     */
    private String processPriority;

    /**
     * 审核人员（JSON格式）
     */
    private String auditor;

    /**
     * 审核方式：auto/manual
     */
    private String auditMethod;

    /**
     * 业务责任人（JSON格式）
     */
    private String mainResponder;

    /**
     * 抄送人员（JSON格式）
     */
    private String ccPersonnel;

    /**
     * 是否启用：enabled/disabled
     */
    private String isEnabled;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 创建人（JSON格式）
     */
    private String creator;

    /**
     * 更新人（JSON格式）
     */
    private String updater;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
