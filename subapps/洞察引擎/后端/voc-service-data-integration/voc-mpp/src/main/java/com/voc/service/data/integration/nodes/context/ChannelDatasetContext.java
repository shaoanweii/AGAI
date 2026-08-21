package com.voc.service.data.integration.nodes.context;

import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Title: AiWorkflowDefaultContext
 * @Package: com.voc.service.model.nodes.context
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/29 14:28
 * @Version:1.0
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class ChannelDatasetContext implements Serializable {
    String workId;
    @Schema(description = "渠道标识")
//    String channelId;

    String channelType;

    @Schema(description = "客户标识")
    String clientId;

    @Builder.Default
    List<DataIntegrationRecordModel> channelDataset = new ArrayList<>();

    @Builder.Default
    List<DataIntegrationRecordModel> successfulDataset = new ArrayList<>();

    @Builder.Default
    List<DataIntegrationRecordModel> failedDataset = new ArrayList<>();
    @Builder.Default
    Date startDate = new Date();
    @Builder.Default
    Date endDate = new Date();

}
