package com.voc.service.insights.engine.data.dao;

import com.voc.service.insights.engine.data.entity.InsKnowledgeBaseDetails;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseModel;

import java.util.List;

/**
 * 知识库明细表(InsKnowledgeBaseDetails)表数据库访问层
 *
 * @author makejava
 * @since 2024-09-06 14:51:56
 */
public interface InsKnowledgeBaseDetailsDao {
    void saveBatch(List<InsKnowledgeBaseDetails> sub);

    List<InsKnowledgeBaseDetails> findByBatchId(InsKnowledgeBaseModel knowledgeBaseModel);

    boolean updateBatch(List<InsKnowledgeBaseDetails> valueChunk);
}

