package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.AreaModel;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import com.voc.service.insights.engine.vo.RegionConfigVo;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:08
 * @描述:
 **/
public interface IInsRegionConfigService {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:21
     * @描述   新增区域分类
     * @param regionConfigModel
     * @return void
     **/
    void saveRegionCategory(InsRegionConfigModel regionConfigModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:21
     * @描述   新增区域
     * @param regionConfigModel
     * @return void
     **/
    void saveRegion(InsRegionConfigModel regionConfigModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:22
     * @描述   更新区域分类
     * @param regionConfigModel
     * @return void
     **/
    void updateRegionCategory(InsRegionConfigModel regionConfigModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:22
     * @描述   更新区域
     * @param regionConfigModel
     * @return void
     **/
    void updateRegion(InsRegionConfigModel regionConfigModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:24
     * @描述   删除区域分类
     * @param regionConfigModel
     * @return void
     **/
    void deleteRegionCategory(InsRegionConfigModel regionConfigModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:25
     * @描述   获取区域分类列表
     * @param regionConfigModel
     * @return java.util.List<com.voc.service.insights.engine.vo.RegionConfigVo>
     **/
    PageInfo findRegionCategoryList(InsRegionConfigModel regionConfigModel);
    /**
     * @param regionConfigModel
     * @return java.util.List<com.voc.service.insights.engine.vo.RegionConfigVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/11 上午9:26
     * @描述 根据区域分类id获取区域列表
     **/
    PageInfo findRegionList(InsRegionConfigModel regionConfigModel);
    List<AreaModel> findRegionAllList(InsRegionConfigModel regionConfigModel);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/19 下午1:34
     * @描述   获取区分树
     * @param regionConfigModel
     * @return java.util.List<com.voc.service.insights.engine.vo.RegionConfigVo>
     **/
    List<RegionConfigVo> findRegionTree(InsRegionConfigModel regionConfigModel);
    List<RegionConfigVo> findRegionTreeByIds(InsRegionConfigModel regionConfigModel);
    List<RegionConfigVo> findRegionTreeByProvinceIds(InsRegionConfigModel regionConfigModel);
    List<RegionConfigVo> findRegionTreeByProvinceIds1(InsRegionConfigModel regionConfigModel);

    List<RegionConfigVo> findAllRegionDetail(InsRegionConfigModel regionConfigModel);

}
