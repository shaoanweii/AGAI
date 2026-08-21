package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsAttributeLabelEntity;
import com.voc.service.insights.engine.model.InsAttributeLabelModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/4/9 13:40
 * @描述:
 **/
@Mapper
@Repository
public interface InsAttributeLabelMapper extends BaseMapper<InsAttributeLabelEntity> {
    IPage<InsAttributeLabelEntity> findAttributeLabelList(IPage<InsAttributeLabelEntity> page, @Param("model") InsAttributeLabelModel model);

    List<InsAttributeLabelEntity> findAllAttributeLabelList(@Param("model") InsAttributeLabelModel model);
}
