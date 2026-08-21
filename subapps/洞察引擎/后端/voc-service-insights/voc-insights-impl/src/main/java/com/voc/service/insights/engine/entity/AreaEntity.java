package com.voc.service.insights.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 fanrong
 * @创建时间 2024/9/13 9:32
 * @描述：
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaEntity {
    /**
     * 城市编码
     */
    private String areaCode;
    /**
     * 城市名称
     */
    private String areaName;
}
