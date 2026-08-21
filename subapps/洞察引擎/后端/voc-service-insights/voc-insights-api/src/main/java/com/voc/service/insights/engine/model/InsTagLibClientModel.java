package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import com.voc.service.insights.engine.api.annotation.SortField;
import com.voc.service.insights.engine.api.annotation.SortFieldConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.assertj.core.internal.Lists;

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
        @SortField(source = "tagType", targer = "itc.tag_type"),
        @SortField(source = "updateTime", targer = "itc.update_time"),
        @SortField(source = "createTime", targer = "itc.create_time"),
        @SortField(source = "tagLibNameHierarchical", targer = "itc.tag_parent_id"),
})
public class InsTagLibClientModel extends Page implements Serializable {
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
     * 同义词
     */
    @Schema(description = "同义词")
    private String synonyms;
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
    private List<String> codes;

    /**
     * 标签类型集合
     */
    private List<String> tagTypeList;

    private String energy;

    private List<String> tagStatusList;

    private Integer level;

    //情感
    @Schema(description = "情感")
    private String emotion;
    //意图
    @Schema(description = "意图")
    private String intention;
    //客户问题分级(S、A、B、C等)
    @Schema(description = "客户问题分级(S、A、B、C等)")
    private String tagCustomerIssueClassification;
    /**
     * 代码状态(有效、无效等)
     */
    @Schema(description = "代码状态(有效、无效等)")
    private String tagCodeStatus;

    /**
     * 事件清晰度(印象、事实)
     */
    @Schema(description = "事件清晰度(印象、事实)")
    private String eventClarity;
    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String operateUser;

    /**
     * 标识字段
     */
    @Schema(description = "标识字段")
    private String identifier;

    private String tagLibNameHierarchical;

    @Schema(description = "是否存在直属末级")
    private Boolean hasFinalCategory;
}
