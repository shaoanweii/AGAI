package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.voc.service.insights.engine.entity.InsCarSceneEntity;
import com.voc.service.insights.engine.model.InsCarSceneModel;
import com.voc.service.insights.engine.vo.InsCarSceneOperatorVo;
import com.voc.service.insights.engine.vo.InsCarSceneVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InsCarSceneMapper extends BaseMapper<InsCarSceneEntity> {
    IPage<InsCarSceneVo> findCarSceneList(IPage<InsCarSceneVo> page, @Param("model") InsCarSceneModel model);

    List<String> findCarSceneOperatorUserIds();

    List<InsCarSceneOperatorVo> findVisibleCarSceneOperatorList(@Param("userIds") List<String> userIds,@Param("appId")String appId);

    List<String> findCategoryAndDescendantIds(@Param("categoryId") String categoryId);

    List<java.util.Map<String, Object>> countSceneByCategoryIds(@Param("categoryIds") List<String> categoryIds);
}
