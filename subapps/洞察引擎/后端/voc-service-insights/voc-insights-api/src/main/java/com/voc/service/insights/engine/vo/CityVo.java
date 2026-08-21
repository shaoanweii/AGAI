package com.voc.service.insights.engine.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 11:24
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityVo implements Serializable {
    /**
     * 城市名称
     */
    private String cityName;
    /**
     * 城市编码
     */
    private String cityCode;
}
