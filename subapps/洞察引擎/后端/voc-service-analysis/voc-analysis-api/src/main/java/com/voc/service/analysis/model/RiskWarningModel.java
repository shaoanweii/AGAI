package com.voc.service.analysis.model;

import lombok.Data;

import java.io.Serializable;


@Data
public class RiskWarningModel implements Serializable {
    private String clientId;

    private String statisticType;
}
