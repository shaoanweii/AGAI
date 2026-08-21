package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/26 上午10:53
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsRiskWarningResultData {
    private String id; // 主键
    private String riskId; // 风险点Id
    private String risk; // 风险code或用户Id
    @ExcelProperty(value = "关联品牌", order = 1)
    @ColumnWidth(10)
    private String brandCodeName;
    @ExcelProperty(value = "涉及车系", order = 2)
    @ColumnWidth(10)
    private String carSeriesName;
    private String riskType; // 风险类型
    @ExcelProperty(value = "风险问题", order = 3)
    @ColumnWidth(10)
    private String riskName;
    @ExcelProperty(value = "风险等级", order = 4)
    @ColumnWidth(10)
    private String riskLevel;
    @ExcelProperty(value = "聚焦问题", order = 5)
    @ColumnWidth(10)
    private String focusName;
    @ExcelProperty(value = "观点热词", order = 6)
    @ColumnWidth(10)
    private String opinionWords;
    @ExcelProperty(value = "负面提及量", order = 7)
    @ColumnWidth(10)
    private String negativeNum;
    @ExcelProperty(value = "投诉提及量", order = 8)
    @ColumnWidth(10)
    private String complainNum;
    @ExcelProperty(value = "净情感值", order = 9)
    @ColumnWidth(10)
    private String emotionNum;
    @ExcelProperty(value = "风险词提及量", order = 10)
    @ColumnWidth(10)
    private String riskWordsNum;
    @ExcelProperty(value = "发声用户数", order = 11)
    @ColumnWidth(10)
    private String userNum;
    @ExcelProperty(value = "发声渠道", order = 12)
    @ColumnWidth(10)
    private String channelNum;
    @ExcelProperty(value = "洞察周期", order = 13)
    @ColumnWidth(10)
    private String statisticType;
    @ExcelProperty(value = "预警时间", order = 14)
    @ColumnWidth(10)
    private String createTime;
}
