package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.TagType;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class InsTagInfoListVo implements Serializable {

    private String id;
    private String firstDimensionName;
    @TagType
    private String type;
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
    private List<String> applyList;
    @Dict(code = InsightsConstants.ENABLE_CODE)
    private String enable;
    @Dict(code = InsightsConstants.SOURCE)
    private String source;
    @Dict(code = InsightsConstants.SERIOUSNESS)
    private String seriousness;
    private String labelType;
    /**
     * 描述
     */
    private String description;

}
