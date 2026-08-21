package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import com.voc.service.insights.engine.entity.InsRegionDetailEntity;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 下午2:44
 * @描述:
 **/
@Mapper
@Repository
public interface InsRegionDetailMapper extends BaseMapper<InsRegionDetailEntity> {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 下午3:01
     * @描述   更新区域详情
     * @param regionDetailEntity
     * @return void
     **/
    void updateRegion(@Param("regionDetailEntity") InsRegionDetailEntity regionDetailEntity);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 下午3:13
     * @描述   根据区域id获取区域详情
     * @param regionId
     * @return com.voc.service.insights.engine.entity.InsRegionDetailEntity
     **/
    InsRegionDetailEntity findRegionDetailByRegionId(@Param("regionId")String regionId);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/12 上午9:09
     * @描述   根据区域分类id删除区域分类
     * @param regionCategoryId
     * @return void
     **/
    void deleteRegionCategory(@Param("regionCategoryId")String regionCategoryId);
    /**
     * @param regionConfigModel
     * @return java.util.List<com.voc.service.insights.engine.entity.InsRegionDetailEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/12 上午9:53
     * @描述 获取区域列表
     **/
    List<InsProvinceAreaInfoEntity> findRegionList(@Param("regionConfigModel") InsRegionConfigModel regionConfigModel);

    Integer findRegionChildCountByRegionIds(@Param("regionIds") List<String> regionIds);

    List<InsRegionDetailEntity> findRegionByName(@Param("name") String name);

    List<InsProvinceAreaInfoEntity> findRegionListByProvinceCode(@Param("regionConfigModel")InsRegionConfigModel regionConfigModel);

    List<InsProvinceAreaInfoEntity> findRegionListByProvinceCode1(@Param("regionConfigModel")InsRegionConfigModel regionConfigModel);
}
