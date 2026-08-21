package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsAutomarkEntity;
import com.voc.service.insights.engine.model.InsAutomarkModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/11 15:23
 * @描述:
 **/
@Mapper
@Repository
public interface InsAutomarkMapper extends BaseMapper<InsAutomarkEntity> {
    IPage<InsAutomarkEntity> findAutomarkList(IPage<InsAutomarkEntity> page, @Param("model") InsAutomarkModel model);
}
