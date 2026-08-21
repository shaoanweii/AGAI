package com.voc.service.insights.engine.model.knowledgeBase;

import com.voc.service.common.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 知识库明细表(InsKnowledgeBaseDetails)实体类
 *
 * @author makejava
 * @since 2024-09-06 14:51:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class KnowledgeBaseDetailFilterModel extends Page implements Serializable {
    /**
     * 主键id
     */
    private String id;
    private List<String> ids;
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
    private List<String> sentiment;
    /**
     * 意图：表扬、咨询、抱怨、投诉、其他
     */
    private List<String> intention;
    /**
     * 创建时间（导入时间）
     */
    private Date updateTime;
    /**
     * 更新时间
     */
    private Date createTime;
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
    private List<String> businessTag;
    /**
     * 质量标签
     */
    private List<String> qualityTag;
    /**
     * 场景标签
     */
    private List<String> scenarioTag;
    /**
     * 向量数据库中的数据集名称
     */
    private String collectionName;


}

