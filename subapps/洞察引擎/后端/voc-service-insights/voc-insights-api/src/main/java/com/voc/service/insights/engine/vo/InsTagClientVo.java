package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import lombok.Data;

import java.io.Serializable;

@Data
public class InsTagClientVo  implements Serializable {
    private String id;
    @Dict(code = InsightsConstants.TAG_TYPE)
    private String type;
    private String firstDimensionName;
    private String firstDimensionNameEn;
    private String firstDimensionCode;
    private String firstId;
    private String secondDimensionName;
    private String secondDimensionNameEn;
    private String secondDimensionCode;
    private String secondId;
    private String thirdDimensionName;
    private String thirdDimensionNameEn;
    private String thirdDimensionCode;
    private String thirdId;
    private String name;
    private String nameEn;
    private String code;
    private String associationStatus;
    @Dict(code = InsightsConstants.ENABLE_CODE)
    private String enable;
    @Dict(code = InsightsConstants.SOURCE)
    private String source;
    private String labelType;
    /**
     * 描述
     */
    private String description;
    private String clientId;
}
