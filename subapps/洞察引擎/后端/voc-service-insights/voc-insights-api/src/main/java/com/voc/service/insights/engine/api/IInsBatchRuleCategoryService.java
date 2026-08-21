package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.model.InsBatchRuleCategoryModel;

import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * 批量规则分类服务接口
 * 提供分类的增删改查功能
 */
public interface IInsBatchRuleCategoryService {

    /**
     * 查询分类树
     * 构建完整的分类树形结构，包括顶级分类和二级分类
     * 支持按名称模糊查询
     *
     * @param searchKey 搜索关键词
     * @return 分类树列表
     */
    List<InsBatchRuleCategoryModel> queryCategoryTree(String searchKey);

    /**
     * 新增分类
     * 支持新增顶级分类和二级分类
     *
     * @param model 分类模型
     * @return 是否成功
     */
    boolean insertCategory(InsBatchRuleCategoryModel model);

    /**
     * 编辑分类
     * 支持修改分类名称、状态等信息
     *
     * @param model 分类模型
     * @return 是否成功
     */
    boolean updateCategory(InsBatchRuleCategoryModel model);

    /**
     * 删除分类
     * 检查是否有子分类或关联规则，有则不允许删除
     *
     * @param categoryId 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(String categoryId);

    /**
     * 检查分类名称是否重复
     * 在同一父分类下，分类名称不能重复
     *
     * @param name 分类名称
     * @param parentId 父分类ID
     * @param excludeId 排除的分类ID（编辑时使用）
     * @return 是否重复
     */
    boolean checkCategoryName(String name, String parentId, String excludeId);

    /**
     * 根据分类ID列表查询规则数量
     * 用于前端展示分类下的规则数量
     *
     * @param categoryIds 分类ID列表
     * @return 分类ID与规则数量的映射
     */
    Map<String, Integer> countByCategoryIds(Set<String> categoryIds);
}
