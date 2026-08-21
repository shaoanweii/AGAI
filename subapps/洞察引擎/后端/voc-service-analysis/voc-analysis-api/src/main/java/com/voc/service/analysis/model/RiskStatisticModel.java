package com.voc.service.analysis.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;


@Data
public class RiskStatisticModel implements Serializable {
    private String beginTime;
    private String endTime;
    private String tagType;
    private String groupType;
    private Class destClass;
    private String methodName;
    private String methodType;
    private String busType;
    private String statisticType;
    private String dateType;
    private boolean isBaidu = false;
    private String isRisk;
    private String brand;
    private String brandCode;
    private String clientId;
    private Set<String> labelTypeLevelFourDisableList;
}
