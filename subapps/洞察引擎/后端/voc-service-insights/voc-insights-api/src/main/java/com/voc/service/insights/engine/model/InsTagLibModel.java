package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/20 下午4:18
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SortFieldConvert(fields = {
        @SortField(source = "tagCode", targer = "tag_code"),
        @SortField(source = "appClient", targer = "app_client")
})
public class InsTagLibModel extends Page implements Serializable {
    /**
     * id
     */
    @Schema(description = "id")
    private String id;
    /**
     * 标签所属分类
     */
    @Schema(description = "标签所属分类")
    @Builder.Default
    private String tagParentId = "0";
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
    /**
     * 应用客户
     */
    private List<String> appClient;

    /**
     * 标签类型集合
     */
    private List<String> tagTypeList;
    private String energy;

}
