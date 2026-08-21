package com.voc.service.insights.engine.alert.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据监控-任务执行状态记录表
 *
 * @author cuick
 * @since 2024-04-26 10:42:22
 */
@Data
@TableName("alt_task_execution_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NotNull
public class AlertTaskExecutionRecordEntity  implements Serializable {

    /**
     * 主键id
     */
    private String id;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 渠道id
     */
    private String channelId;
    /**
     * 客户id
     */
    private String clientId;
    /**
     * 数据源类型
     */
    private String dataType;
    /**
     * 已完成：1， 未完成：0
     */
    private String status;
    /**
     * 执行时间
     */
    private LocalDateTime startTime;
    /**
     * 完成时间
     */
    private LocalDateTime stopTime;
    /**
     * 链路标识
     */
    private String tid;

}

