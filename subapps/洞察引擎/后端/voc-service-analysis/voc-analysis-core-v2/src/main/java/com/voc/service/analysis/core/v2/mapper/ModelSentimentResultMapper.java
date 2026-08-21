package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.ModelSentimentResultEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author 
 * @version 1.0.0
 * @ClassName ModelSentimentResultMapper
 * @description 模型情感分析结果Mapper接口
 * @createTime 
 */
@Mapper
public interface ModelSentimentResultMapper extends BaseMapper<ModelSentimentResultEntity> {
    
}
