package com.voc.service.risk.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


@Data
public class BatchWarningTaskRunModel implements Serializable {

    private String ruleId;

    private String startTime;

    private String endTime;

    private String batchId;

    @Schema(description = "时间检查 0不检查时间 1检查时间")
    private Integer timeCheck;
}
