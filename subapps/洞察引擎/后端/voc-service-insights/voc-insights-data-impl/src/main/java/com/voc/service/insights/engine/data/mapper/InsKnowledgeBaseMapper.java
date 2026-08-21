package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.data.entity.InsKnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 知识库表(InsKnowledgeBase)实体类
 *
 * @author makejava
 * @since 2024-09-06 14:51:57
 */
@Mapper
@Repository
public interface InsKnowledgeBaseMapper extends BaseMapper<InsKnowledgeBase> {

}

