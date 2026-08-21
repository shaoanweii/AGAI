package com.voc.service.insights.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 fanrong
 * @创建时间 2024/9/13 9:32
 * @描述：
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceEntity{
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
    private List<AreaEntity> areas;
}
