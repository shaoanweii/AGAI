package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/27 下午4:16
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarInfoVo  implements Serializable {
    /**
     * 车系名称
     */
    private String name;
    /**
     * 车系编码
     */
    private String code;
    /**
     * 车系id
     */
    private String id;

    @Schema(description = "展示图片")
    private String img;

    @Schema(description = "是否默认")
    private Boolean isDefault = false;

    @Schema(description = "关联本竞品 选择本品时绑定竞品车系，选择竞品时绑定本品车系（多对多）")
    private List<CarInfoVo> competitiveProduct;

    /**
     * 预热开始时间
     */
    private LocalDate preheatStartTime;
    /**
     * 预热结束时间
     */
    private LocalDate preheatEndTime;
    /**
     * 上市开始时间
     */
    private LocalDate launchStartTime;
    /**
     * 上市结束时间
     */
    private LocalDate launchEndTime;
    /**
     * 稳定开始时间
     */
    private LocalDate stableStartTime;
    /**
     * 稳定结束时间
     */
    private LocalDate stableEndTime;
}
