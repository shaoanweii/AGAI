package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/6/19 下午1:29
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsDataSourceSearchCriteriaVo implements Serializable {
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 车型
     */
    private List<BrandCarVo> brandCarModelList;
    /**
     * 业务标签
     */
    private List<TagLibCategoryVo> BIZ;
    /**
     * 质量标签
     */
    private List<TagLibCategoryVo> QY;
    private List<TagLibCategoryVo> SERVICE;
    private List<TagLibCategoryVo> tagLibCategoryVos;

    private String clientId;

    private List<BrandTreeVo> brandVos;
    private List<String> mentionCarSeriesList;

    private String defaultStartTime;

    private String defaultEndTime;

}
