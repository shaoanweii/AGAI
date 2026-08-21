package com.voc.service.insights.engine.model.alert;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据监控-监控数据表(AltMonitoringData)请求返回实体类
 *
 * @author leiww
 * @since 2024-04-26 15:11:35
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Tag(name = "AltMonitoringData", description = "数据监控-监控数据表")
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time")
})
public class AltMonitoringDataModel extends Page  implements Serializable {

    /**
     * 主键
     */
    @Schema(description = "主键")
    private String id;
    /**
     * 接收处理标识
     */
    @Schema(description = "接收处理标识")
    private String workId;
    /**
     * 任务id
     */
    @Schema(description = "任务id")
    private String taskId;
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
     * 接收时间
     */
    @Schema(description = "接收时间")
    private LocalDateTime createTime;
    /**
     * 接收时间
     */
    @Schema(description = "原数据落地时间")
    private LocalDateTime metaDateCreateTime;
    /**
     * 数据集大小
     */
    @Schema(description = "数据集大小")
    private Long dataSize;
    /**
     * 数据来源- metaData,nlpData,pushData
     */
    @Schema(description = "数据来源- metaData,nlpData,pushData")
    private String dataType;
    /**
     * 链路标识
     */
    @Schema(description = "链路标识")
    private String tid;



}

