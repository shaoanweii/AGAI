package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsCarSceneCategoryModel;
import com.voc.service.insights.engine.vo.InsCarSceneCategoryVo;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/3/5
 * @描述: 用车场景分类服务接口
 **/
public interface IInsCarSceneCategoryService {

    /**
     * 新增用车场景分类
     *
     * @param model 用车场景分类
     */
    void saveCarSceneCategory(InsCarSceneCategoryModel model);

    /**
     * 修改用车场景分类
     *
     * @param model 用车场景分类
     */
    void updateCarSceneCategory(InsCarSceneCategoryModel model);

    /**
     * 删除用车场景分类
     *
     * @param model 用车场景分类
     */
    void deleteCarSceneCategory(InsCarSceneCategoryModel model);


    /**
     * 查询用车场景分类列表
     *
     * @param model 查询参数
     * @return 分类列表
     */
    List<InsCarSceneCategoryVo> findCarSceneCategoryList(InsCarSceneCategoryModel model);

    /**
     * 查询用车场景分类树，包含分类下的用车场景
     *
     * @param model 查询参数
     * @return 分类场景树
     */
    List<InsCarSceneCategoryVo> findCarSceneCategoryTree(InsCarSceneCategoryModel model);
}
