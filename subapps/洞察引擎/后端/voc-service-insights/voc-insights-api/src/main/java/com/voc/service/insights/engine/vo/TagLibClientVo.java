package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.voc.service.insights.engine.api.annotation.Dict;
import com.voc.service.insights.engine.api.annotation.TagLibAttribute;
import com.voc.service.insights.engine.api.annotation.TagType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/22 上午9:40
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagLibClientVo  implements Serializable {
    /**
     * id
     */
    @Schema(description = "id")
    private String id;
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
     * 标签类型名称
     */
    @Schema(description = "标签类型名称")
    private String tagTypeName;
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
     * 应用客户
     */
//    @Client
    @Schema(description = "应用客户")
    private String appClient;

    @Schema(description = "标签库层级名称")
    private String tagLibNameHierarchical;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String operateUser;

    /**
     * 同义词
     */
    @Schema(description = "同义词")
    private String synonyms;

    @Schema(description = "是否存在末级观点")
    @Builder.Default
    private Boolean hasFinalTopic = Boolean.FALSE;
}
