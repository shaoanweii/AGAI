package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import com.voc.service.insights.engine.entity.InsRegionDetailEntity;
import com.voc.service.insights.engine.entity.InsRegionEntity;
import com.voc.service.insights.engine.model.InsRegionConfigModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:31
 * @描述:
 **/
public interface InsRegionConfigDao {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:21
     * @描述   新增区域分类
     * @param regionEntity
     * @return void
     **/
    void saveRegionCategory(String clientId,InsRegionEntity regionEntity);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:21
     * @描述   新增区域
     * @param regionDetailEntity
     * @return void
     **/
    void saveRegion(String clientId,InsRegionDetailEntity regionDetailEntity);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:22
     * @描述   更新区域分类
     * @param regionDetailEntity
     * @return void
     **/
    void updateRegionCategory(String clientId,InsRegionEntity regionDetailEntity);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:22
     * @描述   更新区域
     * @param regionDetailEntity
     * @return void
     **/
    void updateRegion(String clientId,InsRegionDetailEntity regionDetailEntity);
    /**
     * @param regionCategoryId
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:24
     * @描述 删除区域分类
     **/
    void deleteRegionCategory(String clientId,String regionCategoryId);
    /**
     * @param regionConfigModel
     * @return java.util.List<com.voc.service.insights.engine.vo.RegionConfigVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:25
     * @描述 获取区域分类列表
     **/
    List<InsRegionEntity> findRegionCategoryList(InsRegionConfigModel regionConfigModel);
    List<InsRegionEntity> findRegionCategoryListHierarchical(InsRegionConfigModel regionConfigModel);

    List<InsRegionEntity> findAllRegionCategoryList(InsRegionConfigModel regionConfigModel);
    /**
     * @param regionConfigModel
     * @return java.util.List<com.voc.service.insights.engine.vo.RegionConfigVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:26
     * @描述 根据区域分类id获取区域列表
     **/
    List<InsProvinceAreaInfoEntity> findRegionList(InsRegionConfigModel regionConfigModel);
    List<InsProvinceAreaInfoEntity> findRegionListByProvinceCode(InsRegionConfigModel regionConfigModel);
    List<InsProvinceAreaInfoEntity> findRegionListByProvinceCode1(InsRegionConfigModel regionConfigModel);

    List<String> findRegionCategoryChildIdsByParentId(String clientId,String regionId);

    Integer findRegionChildCountByRegionIds(String clientId, List<String> regionIds);

    List<InsRegionEntity> findRegionCategoryByName(String clientId, String name, String parentId);

    List<InsRegionDetailEntity> findRegionByName(String clientId, String name);
}
