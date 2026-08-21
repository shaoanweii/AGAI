package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName EntityDataModel
 * @createTime 2024年02月26日 14:34
 * @Copyright cuick
 * 模型数据分析流程中参数请求实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Tag(name = "数据分析请求参数实体对象", description = "数据分析请求参数实体对象")
public class AlgorithmDataModel implements Serializable {
    @Schema(description = "编号")
    String id;
    @Schema(description = "文本类型")
    String source;
    @Schema(description = "模型类型")
    String modelType;
    @Schema(description = "标题")
    String title;
    @Schema(description = "车系")
    String carSeries;
    @Schema(description = "品牌")
    String brand;
    @Schema(description = "内容")
    String content;

}
