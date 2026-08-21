package com.voc.service.insights.engine.model.alert;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Title: AlertTaskModel
 * @Package: com.voc.service.insights.engine.model
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/25 11:30
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertTaskModel implements Serializable {
    /**
     * 主键
     */
    @Schema(description = "主键")
    private String id;
    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String name;
    /**
     * 渠道标识
     */
    @Schema(description = "渠道标识")
    private String channelId;
    /**
     * 客户标识
     */
    @Schema(description = "客户标识")
    private String clientId;
    /**
     * 节点类型
     */
    @Schema(description = "节点类型")
    private String dataType;
    /**
     * 调度时间
     */
    @Schema(description = "调度时间")
    private LocalDateTime scheduledTime;
    /**
     * 任务周期
     */
    @Schema(description = "任务周期")
    private String period;
    /**
     * 周期数：对比的历史周期数量，如任务周期为每日，周期数填写7，则对比历史7日的落库数量均值。（每天:day，每周:week，每月: month）
     */
    @Schema(description = "周期数：对比的历史周期数量，如任务周期为每日，周期数填写7，则对比历史7日的落库数量均值。（每天:day，每周:week，每月: month）")
    private String periodNumber;
    /**
     * 告警标识
     */
    @Schema(description = "告警标识")
    private String alarmId;
    /**
     * 告警等级
     */
    @Schema(description = "告警等级")
    private String alarmLevel;
    /**
     * 告警比对规则
     */
    @Schema(description = "告警比对规则")
    private String alarmRulel;
    /**
     * 升高: i、降低: d
     */
    @Schema(description = "升高: i、降低: d")
    private String alarmOmpare;
    /**
     * 升高值
     */
    @Schema(description = "升高值")
    private Long ompareRise;
    /**
     * 降低值
     */
    @Schema(description = "降低值")
    private Long ompareReduce;
    /**
     * 接收时间
     */
    @Schema(description = "接收时间")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    /**
     * 启用日期
     */
    @Schema(description = "启用日期")
    private LocalDateTime enableTime;
    /**
     * 停用日期
     */
    @Schema(description = "停用日期")
    private LocalDateTime disableTime;
    /**
     * 处理时效
     */
    @Schema(description = "处理时效")
    private String timeliness;

    @Schema(description = "统计数据")
    private Long dataSize;
}
