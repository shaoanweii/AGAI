package com.voc.service.insights.engine.alert.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据监控-任务配置表(AltTaskConfigData)实体类
 *
 * @author leiww
 * @since 2024-04-30 17:11:55
 */
@Data
@TableName("alt_task_config_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AltTaskConfigDataEntity extends Model<AltTaskConfigDataEntity>  implements Serializable {

    /**
     * 主键
     */
    @TableField(value = "id")
    private String id;
    /**
     * 任务名称
     */
    @TableField(value = "name")
    private String name;
    /**
     * 渠道标识
     */
    @TableField(value = "channel_id")
    private String channelId;
    /**
     * 客户标识
     */
    @TableField(value = "client_id")
    private String clientId;
    /**
     * 节点类型
     */
    @TableField(value = "data_type")
    private String dataType;
    /**
     * 调度时间
     */
    @TableField(value = "scheduled_time")
    private LocalDateTime scheduledTime;
    /**
     * 任务周期（每天:day，每周:week，每月: month）
     */
    @TableField(value = "period")
    private String period;
    /**
     * 周期数：对比的历史周期数量，如任务周期为每日，周期数填写7，则对比历史7日的落库数量均值。
     */
    @TableField(value = "period_number")
    private String periodNumber;
    /**
     * 告警标识
     */
    @TableField(value = "alarm_id")
    private String alarmId;
    /**
     * 告警等级
     */
    @TableField(value = "alarm_level")
    private String alarmLevel;
    /**
     * 告警比对规则
     */
    @TableField(value = "alarm_rulel")
    private String alarmRulel;
    /**
     * 升高: i、降低: d
     */
    @TableField(value = "alarm_ompare")
    private String alarmOmpare;
    /**
     * 升高值
     */
    @TableField(value = "ompare_rise")
    private Long ompareRise;
    /**
     * 降低值
     */
    @TableField(value = "ompare_reduce")
    private Long ompareReduce;
    /**
     * 接收时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
    /**
     * 启用日期
     */
    @TableField(value = "enable_time")
    private LocalDateTime enableTime;
    /**
     * 停用日期
     */
    @TableField(value = "disable_time")
    private LocalDateTime disableTime;
    /**
     * 处理时效
     */
    @TableField(value = "timeliness")
    private String timeliness;
}

