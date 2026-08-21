package com.voc.service.insights.engine.api.model;

import lombok.Data;

import java.io.Serializable;


@Data
public class WarningTaskRunModel implements Serializable {

    private String ruleId;

    private String startTime;

    private String endTime;

    private String batchId;
}
