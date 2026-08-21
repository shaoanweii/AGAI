package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsCarSceneCategoryEntity;
import com.voc.service.insights.engine.model.InsCarSceneCategoryModel;
import com.voc.service.insights.engine.vo.InsCarSceneCategoryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InsCarSceneCategoryMapper extends BaseMapper<InsCarSceneCategoryEntity> {

    IPage<InsCarSceneCategoryVo> findCarSceneCategoryPage(IPage<InsCarSceneCategoryVo> page,
                                                           @Param("model") InsCarSceneCategoryModel model);

    List<InsCarSceneCategoryVo> findCarSceneCategoryList(@Param("model") InsCarSceneCategoryModel model);
}
