package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author xy
 * @version 1.0.0
 * @ClassName NewCarSeriesConditionVo
 * @Description 新车上市-车系筛选条件返回VO（包含新品车系和对比车系）
 * @createTime 2026/04/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新车上市-车系筛选条件VO")
public class NewCarSeriesConditionVo implements Serializable {

    @Schema(description = "新品车系（自有品牌）")
    private List<BrandInfoVo> newCarSeries;

    @Schema(description = "对比车系（自有+竞品品牌）")
    private List<BrandInfoVo> compareCarSeries;
}
