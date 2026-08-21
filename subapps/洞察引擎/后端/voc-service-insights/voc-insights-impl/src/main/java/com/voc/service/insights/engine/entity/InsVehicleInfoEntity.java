package com.voc.service.insights.engine.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/23 14:08
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsVehicleInfoEntity implements Serializable {
    /**
     * 类型名称
     */
    private String carType;
    /**
     * 车辆级别
     */
    private String carLevel;
    /**
     * 描述
     */
    private String description;
}
