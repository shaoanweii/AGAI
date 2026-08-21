package com.voc.service.insights.engine.alert.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据监控-告警数据表(AltCoreData)实体类
 *
 * @author leiww
 * @since 2024-04-26 10:42:22
 */
@Data
@TableName("alt_alarm_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AltAlarmDataEntity  implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id")
    private String id;
    /**
     * 告警节点
     */
    @TableField(value = "data_type")
    private String dataType;
    /**
     * 任务配置id
     */
    @TableField(value = "task_id")
    private String taskId;
    /**
     * 渠道id
     */
    @TableField(value = "channel_id")
    private String channelId;
    /**
     * 客户id
     */
    @TableField(value = "client_id")
    private String clientId;
    /**
     * 告警等级
     */
    @TableField(value = "level")
    private String level;
    /**
     * 推送状态 已推送完成：1 ，未推送：0，未处理：-1
     */
    @TableField(value = "push_status")
    private Integer pushStatus;
    /**
     * 推送信息
     */
    @TableField(value = "push_list")
    private Integer pushList;
    /**
     * 推送信息
     */
    @TableField(value = "push_msg")
    private String pushMsg;
    /**
     * 状态:待查看、处理中、已完成、已关闭
     */
    @TableField(value = "`status`")
    private Integer status;
    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
    /**
     * 修改人
     */
    @TableField(value = "update_by")
    private String updateBy;

}

