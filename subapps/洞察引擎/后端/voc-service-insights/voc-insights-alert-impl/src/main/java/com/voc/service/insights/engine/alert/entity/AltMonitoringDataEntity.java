package com.voc.service.insights.engine.alert.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据监控-监控数据表(AltMonitoringData)实体类
 *
 * @author leiww
 * @since 2024-04-26 15:11:35
 */
@Data
@TableName("alt_monitoring_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AltMonitoringDataEntity implements Serializable {

    /**
     * 主键
     */
    @TableId(value = "id")
    private String id;
    /**
     * 接收处理标识
     */
    @TableField(value = "work_id")
    private String workId;
    /**
     * 任务id
     */
    @TableField(value = "task_id")
    private String taskId;
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
     * 接收时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "meta_date_create_time")
    private LocalDateTime metaDateCreateTime;
    /**
     * 数据集大小
     */
    @TableField(value = "data_size")
    private Long dataSize;
    /**
     * 数据来源- metaData,nlpData,pushData
     */
    @TableField(value = "data_type")
    private String dataType;
    /**
     * 链路标识
     */
    @TableField(value = "tid")
    private String tid;


}

