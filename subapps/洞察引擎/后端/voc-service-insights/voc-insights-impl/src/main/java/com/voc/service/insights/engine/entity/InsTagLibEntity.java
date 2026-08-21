package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/21 下午2:03
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_tag_info")
public class InsTagLibEntity  implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 标签所属分类
     */
    private String tagParentId;
    /**
     * 标签名称
     */
    private String tagName;
    /**
     * 标签英文名称
     */
    private String tagNameEn;
    /**
     * 标签编码
     */
    private String tagCode;
    /**
     * 标签类型
     */
    private String tagType;
    /**
     * 层级
     */
    private Integer level;
    /**
     * 标签属性
     */
    private String tagAttribute;
    //情感
    private String emotion;
    //意图
    private String intention;
    /**
     * 能源类型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> energyType;
    /**
     * 车辆类型
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> carType;
    /**
     * 标签状态 禁用:0 启用:1
     */
    private String tagStatus;
    /**
     * 标签定义
     */
    private String tagDescription;
    /**
     * 严重性
     */
    private String seriousness;
    /**
     * 用户旅途1(看车、购车等)
     */
    private String userJourney1;

    /**
     * 用户旅途2(高速路、高原等)
     */
    private String userJourney2;

    /**
     * 场景属性(舒适性/材质/异响)
     */
    private String scenarioAttr;
    /**
     * 事件清晰度(印象、事实)
     */
    private String eventClarity;
    /**
     * 主责部门
     */
    private String d2cResponsibleDept;
    /**
     * 责任部门
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> d2cAccountableDept;
    /**
     * 抄送部门
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> d2cCcDept;
    /**
     * 代码的精准性(精准、有待提升等)
     */
    private String tagAccuracy;
    /**
     * 客户问题分级(S、A、B、C等)
     */
    private String tagCustomerIssueClassification;
    /**
     * 问题程度(高、中、低)
     */
    private String tagIssueSeverity;
    /**
     * 代码状态(有效、无效等)
     */
    private String tagCodeStatus;
    /**
     * 业务领域(产品质量、产品设计、服务体验)
     */
    private String tagBusinessDomain;
    /**
     * 需推送的高价值建议标识
     */
    private String tagHighValueFlag;
    /**
     * 需回复的抱怨标识
     */
    private String tagComplaintFlagNeedingReply;
    /**
     * 针对五级明细高质量VOC标识
     */
    private String tagHighQualityVocFlag;
    /**
     * 新能源特有/燃油特有
     */
    private String tagNewEnergyOrFuel;
    /**
     * 是否需要闭环的(短平快、通用等)
     */
    private String tagNeedForvclosedLoop;
    /**
     * 应用客户
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> appClient;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 更新人
     */
    private String updateUser;
    @TableField(exist = false)
    private String userJourneys;
    @TableField(exist = false)
    private String appClients;
    @TableField(exist = false)
    private String carTypes;
    @TableField(exist = false)
    private String energyTypes;
    private String sort;

    /**
     * 敏感类型
     */
    private String susceptiveType;
}
