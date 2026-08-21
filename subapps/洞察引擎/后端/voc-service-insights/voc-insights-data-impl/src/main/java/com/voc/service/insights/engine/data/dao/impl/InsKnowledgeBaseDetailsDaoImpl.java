package com.voc.service.insights.engine.data.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.insights.engine.data.dao.InsKnowledgeBaseDetailsDao;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBaseDetails;
import com.voc.service.insights.engine.data.mapper.InsKnowledgeBaseDetailsMapper;
import com.voc.service.insights.engine.model.knowledgeBase.InsKnowledgeBaseModel;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InsKnowledgeBaseDetailsDaoImpl extends ServiceImpl<InsKnowledgeBaseDetailsMapper, InsKnowledgeBaseDetails> implements InsKnowledgeBaseDetailsDao {
    @Override
    public void saveBatch(List<InsKnowledgeBaseDetails> sub) {
        this.baseMapper.saveBatch(sub);
    }

    @Override
    public List<InsKnowledgeBaseDetails> findByBatchId(InsKnowledgeBaseModel knowledgeBaseModel) {
        QueryWrapper<InsKnowledgeBaseDetails> query = new QueryWrapper<>();
        query.lambda().eq(InsKnowledgeBaseDetails::getKnowledgeBaseId, knowledgeBaseModel.getId());
        query.lambda().eq(InsKnowledgeBaseDetails::getInputBatchId, knowledgeBaseModel.getInputBatchId());
        query.lambda().eq(InsKnowledgeBaseDetails::getDataValidity, "1");
        query.lambda().select(InsKnowledgeBaseDetails::getId);
        return super.list(query);
    }

    @Override
    public boolean updateBatch(List<InsKnowledgeBaseDetails> valueChunk) {
        return super.updateBatchById(valueChunk, 100);
    }
}
