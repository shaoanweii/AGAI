package com.voc.service.analysis.model;

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
public class ResultDataListModel implements Serializable {

    @ExcelProperty(value = "渠道名称", order = 1)
    @ColumnWidth(10)
    private String channelName;

    @Schema(description = "品牌")
    @ExcelProperty(value = "品牌", order = 2)
    private String brandName;

    @Schema(description = "车系")
    @ExcelProperty(value = "车系", order = 3)
    private String carSeriesName;

    @Schema(description = "本品车系")
    @ExcelProperty(value = "本品车系", order = 4)
    @ColumnWidth(15)
    private String ownCarSeriesList;


    @Schema(description = "竞品车系")
    @ExcelProperty(value = "竞品车系", order = 5)
    @ColumnWidth(15)
    private String competitorsCarSeriesList;


    @Schema(description = "同时提及车系")
    @ExcelProperty(value = "同时提及车系", order = 6)
    @ColumnWidth(15)
    private String mentionCarSeries;

    @Schema(description = "声音片段")
    @ExcelProperty(value = "声音片段", order = 7)
    @ColumnWidth(20)
    private String originalTextScene;

    @Schema(description = "标签分类")
    @ExcelProperty(value = "标签分类", order = 8)
    private String labelTypeName;

    @Schema(description = "一级标签")
    @ExcelProperty(value = "一级标签", order = 9)
    private String labelTypeLevelFirst;

    @Schema(description = "二级标签")
    @ExcelProperty(value = "二级标签", order = 10)
    private String labelTypeLevelSecond;

    @Schema(description = "三级标签")
    @ExcelProperty(value = "三级标签", order = 11)
    private String labelTypeLevelThree;

    @Schema(description = "四级标签")
    @ExcelProperty(value = "四级标签", order = 12)
    private String labelTypeLevelFour;

    @Schema(description = "五级标签")
    @ExcelProperty(value = "五级标签", order = 13)
    private String labelTypeLevelFive;

    @Schema(description = "观点")
    @ExcelProperty(value = "观点", order = 14)
    private String opinion;

    @Schema(description = "观点热词")
    @ExcelProperty(value = "观点热词", order = 15)
    private String topic;

    @Schema(description = "情感")
    @ExcelProperty(value = "情感", order = 16)
    private String sentiment;

    @Schema(description = "意图")
    @ExcelProperty(value = "意图", order = 17)
    private String intention;

    @Schema(description = "数据状态")
    @ExcelProperty(value = "数据状态", order = 18)
    @ColumnWidth(15)
    private String dataStatus;
    @Schema(description = "数据链接")
    private String url;
    @Schema(description = "元数据类型")
    private String metaDataType;

    private String newId;

}
