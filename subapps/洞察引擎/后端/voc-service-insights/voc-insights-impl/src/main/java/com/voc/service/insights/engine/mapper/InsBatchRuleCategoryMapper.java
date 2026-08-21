package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsBatchRuleCategoryEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 批量规则分类Mapper
 * 对应数据库表：ins_batch_rule_category
 */
public interface InsBatchRuleCategoryMapper extends BaseMapper<InsBatchRuleCategoryEntity> {

    /**
     * 根据父分类ID查询子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<InsBatchRuleCategoryEntity> selectListByParentId(@Param("parentId") String parentId);

    /**
     * 根据父分类ID和名称模糊查询子分类列表
     *
     * @param parentId 父分类ID
     * @param name 分类名称（模糊查询）
     * @return 子分类列表
     */
    List<InsBatchRuleCategoryEntity> selectListByParentIdAndName(@Param("parentId") String parentId, @Param("name") String name);

    /**
     * 检查分类名称是否重复
     * 在同一父分类下，分类名称不能重复
     *
     * @param name 分类名称
     * @param parentId 父分类ID
     * @param excludeId 排除的分类ID（编辑时使用）
     * @return 重复数量
     */
    int checkCategoryName(@Param("name") String name, @Param("parentId") String parentId, @Param("excludeId") String excludeId);

    /**
     * 根据分类ID查询是否有子分类
     *
     * @param categoryId 分类ID
     * @return 子分类数量
     */
    int countChildren(@Param("categoryId") String categoryId);

    /**
     * 查询所有分类（用于构建多级树）
     *
     * @return 所有分类列表
     */
    List<InsBatchRuleCategoryEntity> selectAllCategories();
}
