package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @Description: ins_business_tag
 * @Date: 2021-03-30
 * @Version: V1.0
 */
@Data
@Tag(name = "ins_business_tag对象", description = "ins_business_tag")
public class InsBusinessTagListVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "一级名称")
    private String firstDimensionName;

    @Schema(description = "一级Code")
    private String firstDimensionCode;

    @Schema(description = "二级名称")
    private String secondDimensionName;
    @Schema(description = "二级Code")
    private String secondDimensionCode;
    @Schema(description = "三级名称")
    private String thirdDimensionName;
    @Schema(description = "三级Code")
    private String thirdDimensionCode;
    @Schema(description = "四级名称")
    private String topicName;
    @Schema(description = "四级Code")
    private String topicCode;

    @Schema(description = "是否应用")
    private Integer enable;

    @Schema(description = "部门")
    private Set<String> departments;
    @Schema(description = "角色")
    private Set<String> roles;
    @Schema(description = "角色Ids")
    private Set<String> rolesIds;


    /**
     * 编号
     */
    @Schema(description = "编号")
    private String id;
    /**
     * 名称
     */
    //@Excel(name = "名称", width = 15)
    @Schema(description = "名称")
    private String name;
    /**
     * 英文名称
     */
    //@Excel(name = "英文名称", width = 15)
    @Schema(description = "英文名称")
    private String nameEn;
    /**
     * 编码
     */
    //@Excel(name = "编码", width = 15)
    @Schema(description = "编码")
    private String tagCode;
    /**
     * 排序
     */
    //@Excel(name = "排序", width = 15)
    @Schema(description = "排序")
    private Integer orderBy;
    /**
     * 层级
     */
    //@Excel(name = "层级", width = 15)
    @Schema(description = "层级")
    private String hierarchy;
    private String industryId;
    /**
     * createTime
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "createTime")
    private LocalDateTime createTime;
    /**
     * 标签数
     */
    //@Excel(name = "标签数", width = 15)
    @Schema(description = "标签数")
    private Integer tagData;
    /**
     * 备注
     */
    //@Excel(name = "备注", width = 15)
    @Schema(description = "相关描述")
    private String relatedDescription;
    /**
     * yndel
     */
    //@Excel(name = "yndel", width = 15)
    @Schema(description = "yndel")
    private String yndel;
    /**
     * 是否有子节点
     */
    //@Excel(name = "是否有子节点", width = 15)
    @Schema(description = "是否有子节点")
    private String hasChild;
    /**
     * 父级节点
     */
    //@Excel(name = "父级节点", width = 15)
    @Schema(description = "父级节点")
    private String pid;

}
