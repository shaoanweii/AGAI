package com.voc.service.insights.engine.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.vo.AutomarkVo;
import com.voc.service.insights.engine.vo.BrandInfoVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/11 15:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
@EqualsAndHashCode(callSuper = false)
public class InsAutomarkModel extends Page implements Serializable {
    private String id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "英文名称")
    private String nameEn;
    @Schema(description = "图片")
    private String img;
    @Schema(description = "是否核心")
    private Integer isCore;
    @Schema(description = "本竞品类型 1本品，2竞品，3非关注范围")
    private Integer competitiveType;
    @Schema(description = "本竞品关系")
    private List<AutomarkVo> competitiveProduct;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "操作人")
    private String operator;

    private List<String> ids;

}
