package com.voc.service.insights.engine.api;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建者: fanrong
 * @创建时间: 2024/8/15 下午4:15
 * @描述:
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagConvertTemplate {
    @ExcelProperty(value = "car", order = 0)
    private String car;
    @ExcelProperty(value = "dim", order = 1)
    private String dim;
    @ExcelProperty(value = "business_tag_name", order = 2)
    private String business_tag_name;
    @ExcelProperty(value = "business_tag_code", order = 3)
    private String business_tag_code;
//    @ExcelProperty(value = "quality_tag_name", order = 2)
//    private String quality_tag_name;
//    @ExcelProperty(value = "quality_tag_code", order = 3)
//    private String quality_tag_code;
//    @ExcelProperty(value = "severity_level", order = 4)
//    private String severity_level;
}
