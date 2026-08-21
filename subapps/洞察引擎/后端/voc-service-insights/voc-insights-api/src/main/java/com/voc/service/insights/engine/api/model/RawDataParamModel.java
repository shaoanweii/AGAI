package com.voc.service.insights.engine.api.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawDataParamModel extends Page implements Serializable {

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "客户ID")
    @NotBlank(message = "客户ID不能为空")
    private String clientId;

    @Schema(description = "workIdList")
    private Set<String> workIdList;

    @Schema(description = "渠道ID")
    private List<String> channelIdList;

    @Schema(description = "数据状态")
    private List<String> dataStatus;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "时间")
    private String date;

    @Schema(description = "时间")
    private List<String> dateList;

    @Schema(description = "1本地上传 2系统集成")
    private Integer showType;

    private String taskId;
    private String fileName;
}
