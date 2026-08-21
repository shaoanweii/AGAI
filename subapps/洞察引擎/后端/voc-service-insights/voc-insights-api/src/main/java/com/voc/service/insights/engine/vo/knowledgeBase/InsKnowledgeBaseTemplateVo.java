package com.voc.service.insights.engine.vo.knowledgeBase;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InsKnowledgeBaseTemplateVo implements Serializable {

    @ExcelProperty(value = "原文内容", index = 0)
    @ColumnWidth(value = 15)
    private String content;
    @ExcelProperty(value = "评价主体", index = 1)
    @ColumnWidth(value = 15)
    private String subject;
    @ExcelProperty(value = "评价属性", index = 2)
    @ColumnWidth(value = 15)
    private String aspect;
    @ExcelProperty(value = "评价描述*", index = 3)
    @ColumnWidth(value = 15)
    private String description;
    @ExcelProperty(value = "观点名称", index = 4)
    @ColumnWidth(value = 15)
    private String opinion;
    @ExcelProperty(value = "归一观点", index = 5)
    @ColumnWidth(value = 15)
    private String topic;
    @ExcelProperty(value = "业务标签", index = 6)
    @ColumnWidth(value = 15)
    private String businessTag;
    @ExcelProperty(value = "质量标签", index = 7)
    @ColumnWidth(value = 15)
    private String qualityTag;
    @ExcelProperty(value = "场景标签", index = 8)
    @ColumnWidth(value = 15)
    private String scenarioTag;
    @ExcelProperty(value = "严重性等级", index = 9)
    @ColumnWidth(value = 15)
    private String severityLevel;
    @ExcelProperty(value = "情感", index = 10)
    private String sentiment;
    @ExcelProperty(value = "意图", index = 11)
    private String intention;


}
