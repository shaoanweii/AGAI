package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(25)
public class InsLabeRecordPageExcelVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "声音ID",order = 3)
    @Schema(description = "声音ID")
    private String newId;

    @ExcelProperty(value = "-级渠道",order = 6)
    @Schema(description = "级渠道")
    private String channelFirst;

    @ExcelProperty(value = "二级渠道",order = 7)
    @Schema(description = "二级渠道")
    private String channelSecond;

    @ExcelProperty(value = "三级渠道",order = 8)
    @Schema(description = "三级渠道")
    private String channelThree;

    @ExcelProperty(value = "声音片段内容",order = 12)
    @Schema(description = "声音片段内容")
    private String originalTextScene;

    @ExcelProperty(value = "车系",order = 11)
    @Schema(description = "车系")
    private String carSeriesName;

    @ExcelProperty(value = "分类明细",order = 16)
    @Schema(description = "分类明细")
    private String textLabel;

    @ExcelProperty(value = "情感",order = 13)
    @Schema(description = "情感")
    private String sentiment;

    @ExcelProperty(value = "意图",order = 14)
    @Schema(description = "意图")
    private String intentionType;

    @ExcelProperty(value = "观点",order = 15)
    @Schema(description = "观点")
    private String topic;

    @ExcelProperty(value = "发声时间",order = 4)
    @Schema(description = "发声时间")
    private String publishTime;

    @ExcelProperty(value = "纠错时间",order = 1)
    @Schema(description = "纠错时间")
    private String operateTime;

    @ExcelProperty(value = "纠错人",order = 2)
    @Schema(description = "纠错人")
    private String operateUser;

    @ExcelProperty(value = "审核状态",order = 19)
    @Schema(description = "审核状态")
    private String auditStatusText;

    @ExcelProperty(value = "错误类型",order = 17)
    @Schema(description = "错误类型")
    private String errorTypeText;

    @ExcelProperty(value = "纠错明细",order = 18)
    @Schema(description = "纠错明细")
    private String correctionInfo;

    @ExcelProperty(value = "发声人",order = 5)
    @Schema(description = "发声人")
    private String userName;

    @ExcelProperty(value = "区域",order = 9)
    @Schema(description = "区域")
    private String areaName;

    @ExcelProperty(value = "省市",order = 10)
    @Schema(description = "省市")
    private String cityName;

}
