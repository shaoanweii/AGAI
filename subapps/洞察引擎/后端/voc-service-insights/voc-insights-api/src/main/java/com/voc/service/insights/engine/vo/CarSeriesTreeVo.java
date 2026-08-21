package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 上午9:15
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarSeriesTreeVo {
    /**
     * 竞品品牌编码
     */
    @Schema(description = "竞品品牌编码")
    private String code;
    /**
     * 竞品品牌名称
     */
    @Schema(description = "竞品品牌名称")
    private String name;
    /**
     * 竞品品牌名称
     */
    @Schema(description = "竞品品牌名称")
    private String value;


    private List<CarSeriesTreeVo> child;;
}
