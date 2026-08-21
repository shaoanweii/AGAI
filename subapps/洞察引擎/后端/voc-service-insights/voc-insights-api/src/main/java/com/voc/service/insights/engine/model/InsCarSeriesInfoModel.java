package com.voc.service.insights.engine.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.vo.CarInfoVo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time"),
        @SortField(source = "name", targer = "name"),
        @SortField(source = "brandName", targer = "brand_code")
})
@EqualsAndHashCode(callSuper = false)
public class InsCarSeriesInfoModel extends Page  implements Serializable {
    private String id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "英文名称")
    private String nameEn;
    @Schema(description = "品牌id")
    private String brandId;
    @Schema(description = "品牌编码")
    private String brandCode;
    @Schema(description = "图片")
    private String img;
    @Schema(description = "别名")
    private String alias;
    @Schema(description = "排除词")
    private String exclusionWords;
    @Schema(description = "是否核心车系 0非核心 1核心")
    private Integer isCore;
    @Schema(description = "本竞品类型 1本品，2竞品，3非关注范围")
    private Integer competitiveType;
    @Schema(description = "关联本竞品 选择本品时绑定竞品车系，选择竞品时绑定本品车系（多对多）")
    private List<CarInfoVo> competitiveProduct;
    @Schema(description = "是否新车 0非新车 1新车")
    private Integer isNewCar;
    @Schema(description = "新车开始时间")
    private LocalDate startTime;
    @Schema(description = "新车结束时间")
    private LocalDate endTime;
    @Schema(description = "预热开始时间")
    private LocalDate preheatStartTime;
    @Schema(description = "预热结束时间")
    private LocalDate preheatEndTime;
    @Schema(description = "上市开始时间")
    private LocalDate launchStartTime;
    @Schema(description = "上市结束时间")
    private LocalDate launchEndTime;
    @Schema(description = "稳定开始时间")
    private LocalDate stableStartTime;
    @Schema(description = "稳定结束时间")
    private LocalDate stableEndTime;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "操作人")
    private String operator;
    private List<String> ids;
    private String code;
    @Schema(description = "车系编码集")
    private List<String> codes;
    @Schema(description = "品牌编码集")
    private List<String> brandCodes;
    @Schema(description = "排序")
    private int orderBy;
}