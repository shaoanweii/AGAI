package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description: ins_business_tag
 * @Date: 2021-03-30
 * @Version: V1.0
 */
@Data
@TableName("ins_business_tag")
@Tag(name = "ins_business_tag对象", description = "ins_business_tag")
public class InsBusinessTagEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
//    @TableId(type = IdType.ASSIGN_ID)
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
    @TableField(exist = false)
    private String code;
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
    /**
     * 行业
     */
    //@Excel(name = "行业", width = 15, dicCode = "industry")
    //@Dict(dicCode = "industry")
    @Schema(description = "行业")
    private String industryId;
    /**
     * 关联部门
     */
    //@Excel(name = "关联部门", width = 15, dicCode = "department")
    //@Dict(dicCode = "department")
    @Schema(description = "关联部门")
    @TableField(exist = false)
    private String relatedDepartments;
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
    private String brand;
    private String tagType;
    @Schema(description = "能源类型")
    private String associatedEnergy;
    @Schema(description = "是否应用")
    private boolean enable;

    @Schema(description = "是否兜底")
    private Integer other;
}
