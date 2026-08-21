package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: liuhb
 * @创建时间: 2024/4/15 09:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsResultDataListVo implements Serializable {

    @ExcelProperty(value = "原文Id", order = 1)
    @ColumnWidth(10)
    private String originalId;

    @Schema(description = "品牌")
    @ExcelProperty(value = "品牌", order = 2)
    private String brandName;

    @Schema(description = "车系")
    @ExcelProperty(value = "车系", order = 3)
    private String carSeriesName;

    @Schema(description = "声音片段")
    @ExcelProperty(value = "声音片段", order = 4)
    @ColumnWidth(20)
    private String originalTextScene;

    @Schema(description = "业务标签")
    @ExcelProperty(value = "业务标签", order = 5)
    @ColumnWidth(15)
    private String businessEndLevelLabel;

    @Schema(description = "业务分类")
    @ExcelProperty(value = "业务分类", order = 6)
    private String businessCategory;

    @Schema(description = "质量标签")
    @ExcelProperty(value = "质量标签", order = 7)
    @ColumnWidth(15)
    private String qualityEndLevelLabel;

    @Schema(description = "质量分类")
    @ExcelProperty(value = "质量分类", order = 8)
    private String qualityCategory;

    @Schema(description = "观点热词")
    @ExcelProperty(value = "观点热词", order = 9)
    private String opinionKeywords;

    @Schema(description = "情感")
    @ExcelProperty(value = "情感", order = 10)
    private String sentiment;

    @Schema(description = "意图")
    @ExcelProperty(value = "意图", order = 11)
    private String intention;

    @Schema(description = "渠道名称")
    @ExcelProperty(value = "渠道名称", order = 12)
    @ColumnWidth(15)
    private String channelName;

    @Schema(description = "发布时间")
    @ExcelProperty(value = "发布时间", order = 13)
    @ColumnWidth(15)
    private String publishTime;

    @Schema(description = "数据状态")
    @ExcelProperty(value = "数据状态", order = 14)
    @ColumnWidth(15)
    private String dataStatus;
    @Schema(description = "数据链接")
    @ExcelProperty(value = "数据链接", order = 15)
    @ColumnWidth(15)
    private String url;
    @Schema(description = "元数据类型")
    @ExcelProperty(value = "元数据类型", order = 16)
    @ColumnWidth(15)
    private String metaDataType;

}
