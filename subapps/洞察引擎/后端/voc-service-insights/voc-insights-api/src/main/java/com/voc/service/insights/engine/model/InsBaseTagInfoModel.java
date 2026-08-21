package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "createTime", targer = "create_time"),
        @SortField(source = "updateTime", targer = "update_time")
})
public class InsBaseTagInfoModel extends Page  implements Serializable {
    /**
     * id
     */
    @Schema(description = "id")
    private String id;
    /**
     * 应用客户
     */
    @Schema(description = "应用客户")
    private String appClient;
    /**
     * 标签所属分类
     */
    @Schema(description = "标签所属分类")
    private String tagParentId;
    /**
     * 标签名称
     */
    @Schema(description = "标签名称")
    private String tagName;
    /**
     * 标签英文名称
     */
    @Schema(description = "标签英文名称")
    private String tagNameEn;
    /**
     * 标签编码
     */
    @Schema(description = "标签编码")
    private String tagCode;
    /**
     * 标签类型
     */
    @Schema(description = "标签类型")
    private String tagType;
    /**
     * 标签属性
     */
    @Schema(description = "标签属性")
    private String tagAttribute;
    /**
     * 能源类型
     */
    @Schema(description = "能源类型")
    private List<String> energyType;
    /**
     * 车辆类型
     */
    @Schema(description = "车辆类型")
    private List<String> carType;
    /**
     * 标签状态 禁用:0 启用:1
     */
    @Schema(description = "标签状态 禁用:0 启用:1")
    private String tagStatus;
    /**
     * 标签定义
     */
    @Schema(description = "标签定义")
    private String tagDescription;
    /**
     * 严重性
     */
    @Schema(description = "严重性")
    private String seriousness;
    /**
     * 用户旅途
     */
    @Schema(description = "用户旅途")
    private List<String> userJourney;
    /**
     * 标签所属分类集合
     */
    private List<String> tagParentIds;

    private List<String> ids;

    /**
     * 标签类型集合
     */
    private List<String> tagTypeList;

}
