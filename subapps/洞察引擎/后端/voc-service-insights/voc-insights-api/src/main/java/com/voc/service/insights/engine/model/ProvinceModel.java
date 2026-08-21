package com.voc.service.insights.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/12 上午10:23
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceModel {
    /**
     * 省份编码
     */
    private String provinceCode;
    /**
     * 省份名称
     */
    private String provinceName;
    /**
     * 城市列表
     */
    private List<AreaModel> areas;
}
