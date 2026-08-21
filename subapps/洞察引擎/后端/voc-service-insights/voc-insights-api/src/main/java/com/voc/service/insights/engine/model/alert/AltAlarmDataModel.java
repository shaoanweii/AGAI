package com.voc.service.insights.engine.model.alert;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.IConditionFilters;
import com.voc.service.insights.engine.api.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 数据监控-告警数据表(AltCoreData)请求返回实体类
 *
 * @author leiww
 * @since 2024-04-26 10:42:23
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Tag(name = "AltAlarmData", description = "数据监控-告警数据表")
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class AltAlarmDataModel extends Page implements Serializable {

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 告警节点
     */
    @Dict(code = IConditionFilters.ALARM_NODE)
    @Schema(description = "告警节点")
    private String dataType;
    /**
     * 任务配置id
     */
    @Schema(description = "任务配置id")
    private String taskId;

    /**
     * 任务配置id
     */
    @Schema(description = "任务配置名称")
    private String taskIdTEXT;

    /**
     * 渠道id
     */
    @Channel
    @Schema(description = "渠道id")
    private String channelId;
    /**
     * 客户id
     */
    @Client
    @Schema(description = "客户id")
    private String clientId;
    /**
     * 告警等级
     */
    @Dict(code = IConditionFilters.ALARM_LEVEL)
    @Schema(description = "告警等级")
    private String level;
    /**
     * 推送状态 已推送完成：1 ，未推送：0，未处理：-1
     */
    @Schema(description = "推送状态 已推送完成：1 ，未推送：0，未处理：-1")
    private Integer pushStatus;
    /**
     * 推送信息
     */
    @Schema(description = "推送信息")
    private Integer pushList;
    /**
     * 推送信息
     */
    @Schema(description = "推送信息")
    private String pushMsg;
    /**
     * 状态：待查看、处理中、已完成、已关闭
     */
    @Schema(description = "状态：待查看、处理中、已完成、已关闭")
    private Integer status;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateBy;

    /**
     * 客户过滤条件
     */
    @Schema(description = "客户过滤条件")
    private Set<String> clientFilters;
    /**
     * 渠道过滤条件
     */
    @Schema(description = "渠道过滤条件")
    private Set<String> channelFilters;
    /**
     * 告警状态过滤条件
     */
    @Schema(description = "告警节点过滤条件")
    private Set<String> dataTypeFilters;
    /**
     * 告警等级过滤条件
     */
    @Schema(description = "告警等级过滤条件")
    private Set<String> levelFilters;
    /**
     * 处理时效
     */
    @Schema(description = "处理时效")
    private String timeliness;

}

