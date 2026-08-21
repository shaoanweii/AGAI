package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBaseDetails;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识库明细表(InsKnowledgeBaseDetails)实体类
 *
 * @author makejava
 * @since 2024-09-06 14:51:56
 */
@Mapper
@Repository
public interface InsKnowledgeBaseDetailsMapper extends BaseMapper<InsKnowledgeBaseDetails> {

    void saveBatch(List<InsKnowledgeBaseDetails> details);

    void batchUpdate(List<InsKnowledgeBaseDetails> list);
}

