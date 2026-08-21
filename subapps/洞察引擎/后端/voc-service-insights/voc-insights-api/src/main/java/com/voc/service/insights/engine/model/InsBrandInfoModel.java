package com.voc.service.insights.engine.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import com.voc.service.insights.engine.vo.BrandInfoVo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time"),
        @SortField(source = "name", targer = "name"),
        @SortField(source = "automark", targer = "automark")
})
public class InsBrandInfoModel extends Page  implements Serializable {
    /**
     * 主键id
     */
    private String id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "编码")
    private String code;
    @Schema(description = "英文名称")
    private String nameEn;
    @Schema(description = "车企")
    private String automark;
    @Schema(description = "车企id")
    private String automarkId;
    @Schema(description = "图片")
    private String img;
    @Schema(description = "别名")
    private String alias;
    @Schema(description = "排除词")
    private String exclusionWords;
    @Schema(description = "是否核心")
    private Integer isCore;
    @Schema(description = "本竞品类型")
    private Integer competitiveType;
    @Schema(description = "本竞品关系")
    private List<BrandInfoVo> competitiveProduct;
    @Schema(description = "操作人")
    private String operator;
    @Schema(description = "状态")
    private String status;
    @Schema(description = "车企集合")
    private List<String> automarkList;
    private List<String> ids;
    @Schema(description = "排序")
    private int orderBy;
    @Schema(description = "品牌过滤")
    private Set<String> brandFilters;

    @Schema(description = "不为当前数据id")
    private String notIdFilter;
    @Schema(description = "车系名称过滤")
    private String nameFilter;
}