package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsRegionEntity;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 下午2:42
 * @描述:
 **/
@Mapper
@Repository
public interface InsRegionMapper extends BaseMapper<InsRegionEntity> {
    /**
     * @创建者: fanrong
     * @创建时间: 2024/9/11 下午2:42
     * @描述: 更新区域分类
     **/
    void updateRegionCategory(@Param("region") InsRegionEntity regionDetailEntity);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/12 上午9:12
     * @描述   根据区域分类id删除区域分类
     * @param regionCategoryId
     * @return void
     **/
    void deleteRegionCategory(@Param("regionCategoryId") String regionCategoryId);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/12 上午9:51
     * @描述   获取区域分类列表
     * @param regionConfigModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsRegionEntity>
     **/
    List<InsRegionEntity> findRegionCategoryList(InsRegionConfigModel regionConfigModel);

    List<InsRegionEntity> findAllRegionCategoryList(InsRegionConfigModel regionConfigModel);

    List<String> findRegionChildIdsByParentId(@Param("regionId") String regionId);

    List<InsRegionEntity> findRegionCategoryByName(@Param("name") String name, @Param("parentId") String parentId);

    List<InsRegionEntity> findRegionCategoryListHierarchical(@Param("regionConfigModel") InsRegionConfigModel regionConfigModel);
}
