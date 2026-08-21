package com.voc.service.risk.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BatchRuleDataVo implements Serializable {

    private String ruleId;
    private String ruleName;
    private String categoryId;
    private String categoryName;
    private String brandCode;
    private String brandName;
    private String alertType;
    private String alertFrequency;
    private String alertTime;
    private String alertCron;
    private String dimensionConfig;
    private String indicatorConfig;
    private String processPriority;
    private String auditor;
    private String auditMethod;
    private String mainResponder;
    private String ccPersonnel;
    private String isEnabled;
    private Integer version;
    private String creator;
    private String updater;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}