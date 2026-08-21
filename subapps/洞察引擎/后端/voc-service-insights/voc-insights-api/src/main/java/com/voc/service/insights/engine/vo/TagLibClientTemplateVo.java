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
 * @创建者: fanrong
 * @创建时间: 2024/12/3 下午2:54
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagLibClientTemplateVo implements Serializable {
    /**
     * 标签类型
     */
    @ExcelProperty(value = "标签类型", order = 0)
    @ColumnWidth(20)
    private String tagType;
    /**
     * 标签一级分类
     */
    @ExcelProperty(value = "标签一级分类", order = 1)
    @ColumnWidth(20)
    private String firstTypeCategory;
    /**
     * 标签二级分类
     */
    @ExcelProperty(value = "标签二级分类", order = 2)
    @ColumnWidth(20)
    private String secondTypeCategory;
    /**
     * 标签三级分类
     */
    @ExcelProperty(value = "标签三级分类", order = 3)
    @ColumnWidth(20)
    private String threeTypeCategory;
    /**
     * 标签四级分类
     */
    @ExcelProperty(value = "标签四级分类", order = 4)
    @ColumnWidth(20)
    private String fourTypeCategory;
    /**
     * 标签定义
     */
    @ExcelProperty(value = "标签定义", order = 5)
    @ColumnWidth(20)
    private String tagDesc;
    /**
     * 能源分类
     */
    @ExcelProperty(value = "关联能源分类", order = 6)
    @ColumnWidth(20)
    private String energyCategory;
    /**
     * 车辆类型
     */
    @ExcelProperty(value = "关联车辆类型", order = 7)
    @ColumnWidth(20)
    private String carType;
    @Schema(description = "严重性")
    @ExcelProperty(value = "严重性等级", order = 8)
    @ColumnWidth(20)
    private String seriousness;
    @Schema(description = "用户旅途")
    @ExcelProperty(value = "关联用户旅程", order = 9)
    @ColumnWidth(20)
    private String userJourney;
    @Schema(description = "启用状态")
    @ExcelProperty(value = "启用状态", order = 10)
    @ColumnWidth(20)
    private String status;

}
