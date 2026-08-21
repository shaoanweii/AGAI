package com.voc.service.insights.engine.vo.knowledgeBase;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识库表(InsKnowledgeBase)实体类
 *
 * @author makejava
 * @since 2024-09-06 14:51:57
 */
@Data
public class InsKnowledgeBaseVo implements Serializable {
    /**
     * 主键id
     */
    private String id;
    /**
     * 知识库名称
     */
    private String name;
    /**
     * 数据格式
     */
    private String format;
    /**
     * 数据总数
     */
    private Integer count;
    /**
     * ⽤户客户ID
     */
    private String clientId;
    /**
     * 项目Id
     */
    private String projectId;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
     * 对应向量数据库里的数据集名称
     */
    private String collectionName;


}

