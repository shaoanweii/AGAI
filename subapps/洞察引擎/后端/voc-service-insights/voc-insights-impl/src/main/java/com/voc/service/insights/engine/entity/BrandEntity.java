package com.voc.service.insights.engine.entity;

import com.voc.service.insights.engine.model.CarSeriesModel;
import com.voc.service.insights.engine.model.CompetitiveProductModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/14 上午9:38
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandEntity {
    /**
     * 车系
     */
    private List<CarSeriesEntity> carSeries;
    /**
     * 竞品
     */
    private List<CompetitiveProductEntity> competitiveProduct;
}
