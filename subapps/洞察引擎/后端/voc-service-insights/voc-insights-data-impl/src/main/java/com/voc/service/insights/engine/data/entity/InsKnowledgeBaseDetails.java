package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库明细表(InsKnowledgeBaseDetails)实体类
 *
 * @author makejava
 * @since 2024-09-06 14:51:56
 */
@Data
@TableName("ins_knowledge_base_details")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InsKnowledgeBaseDetails implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private String id;
    /**
     * 知识库id
     */
    private String knowledgeBaseId;
    /**
     * 内容
     */
    private String content;
    /**
     * 情感：正面、负面、中性
     */
    private String sentiment;
    /**
     * 意图：表扬、咨询、抱怨、投诉、其他
     */
    private String intention;
    /**
     * 创建时间（导入时间）
     */
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    private LocalDateTime createTime;
    /**
     * 修改用户
     */
    private String updateBy;
    /**
     * 创建用户
     */
    private String createBy;
    /**
     * 向量id
     */
    private String vectorId;
    @TableField(exist = false)
    private List<Float> embedding;
    /**
     * 评价主体
     */
    private String subject;
    /**
     * 评价属性
     */
    private String aspect;
    /**
     * 评价描述
     */
    private String description;
    /**
     * 观点名称:1. 该数据由主体+描述拼接而成
     */
    private String opinion;
    /**
     * 归一观点:多个观点聚合归一的名称
     */
    private String topic;
    /**
     * 业务标签
     */
    private String businessTag;
    /**
     * 质量标签
     */
    private String qualityTag;
    /**
     * 场景标签
     */
    private String scenarioTag;
    /**
     * 向量数据库中的数据集名称
     */
    private String collectionName;

    private String severityLevel;
    /**
     * 数据有效性(0:无效 1：有效)
     */
    private String dataValidity;
//    向量状态：0：待生成，1已生成
    private String vectorState;
    private String inputBatchId;

}

