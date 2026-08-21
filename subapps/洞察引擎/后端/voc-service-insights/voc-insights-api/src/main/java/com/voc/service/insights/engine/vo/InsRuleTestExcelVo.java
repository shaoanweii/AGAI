package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/13 10:05
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsRuleTestExcelVo implements Serializable {


    @Schema(description = "渠道名称")
    @ExcelProperty(value = "渠道名称", order = 0)
    @ColumnWidth(20)
    private String channelName;


    @Schema(description = "品牌名称")
    @ExcelProperty(value = "品牌名称", order = 1)
    @ColumnWidth(20)
    private String brandName;

    @Schema(description = "车系名称")
    @ExcelProperty(value = "车系名称", order = 2)
    @ColumnWidth(20)
    private String carSeriesName;

    @Schema(description = "内容类型")
    @ExcelProperty(value = "内容类型", order = 3)
    @ColumnWidth(20)
    private String contentType;


    @Schema(description = "标题")
    @ExcelProperty(value = "标题", order = 4)
    @ColumnWidth(20)
    private String title;

    @Schema(description = "内容")
    @ExcelProperty(value = "内容", order = 5)
    @ColumnWidth(20)
    private String content;

    @Schema(description = "情感")
    @ExcelProperty(value = "情感", order = 6)
    @ColumnWidth(20)
    private String sentiment;

    @Schema(description = "意图")
    @ExcelProperty(value = "意图", order = 7)
    @ColumnWidth(20)
    private String intention;

    @Schema(description = "dom标签")
    @ExcelProperty(value = "全领域标签一级", order = 8)
    @ColumnWidth(20)
    private String domTagFirst;
    @Schema(description = "dom标签")
    @ExcelProperty(value = "全领域标签二级", order = 9)
    @ColumnWidth(20)
    private String domTagSecond;
    @Schema(description = "dom标签")
    @ExcelProperty(value = "全领域标签三级", order = 10)
    @ColumnWidth(20)
    private String domTagThree;
    @Schema(description = "dom标签")
    @ExcelProperty(value = "全领域标签四级", order = 11)
    @ColumnWidth(20)
    private String domTagFour;

    @Schema(description = "主题")
    @ExcelProperty(value = "观点", order = 12)
    @ColumnWidth(20)
    private String topic;

    @Schema(description = "发布人名称")
    @ExcelProperty(value = "发布用户昵称", order = 13)
    @ColumnWidth(20)
    private String publishUserName;

    @Schema(description = "发布人id")
    @ExcelProperty(value = "发布用户ID", order = 14)
    @ColumnWidth(20)
    private String publishUserId;

    @Schema(description = "发布用户昵称")
    @ExcelProperty(value = "主贴用户ID", order = 15)
    @ColumnWidth(20)
    private String mainUserId;

    @Schema(description = "主贴发布人名称")
    @ExcelProperty(value = "主贴用户名称", order = 16)
    @ColumnWidth(20)
    private String mainUserName;
}
