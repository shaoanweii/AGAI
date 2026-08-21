package com.voc.service.insights.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/12 上午10:24
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaModel {
    /**
     * 城市编码
     */
    private String areaCode;
    /**
     * 城市名称
     */
    private String areaName;
}
