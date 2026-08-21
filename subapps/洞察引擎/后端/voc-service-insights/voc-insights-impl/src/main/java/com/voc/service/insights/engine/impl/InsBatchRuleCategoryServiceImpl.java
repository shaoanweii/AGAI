package com.voc.service.insights.engine.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.IInsBatchRuleCategoryService;
import com.voc.service.insights.engine.entity.InsBatchRuleCategoryEntity;
import com.voc.service.insights.engine.mapper.InsBatchRuleCategoryMapper;
import com.voc.service.insights.engine.mapper.InsBatchRuleMapper;
import com.voc.service.insights.engine.model.InsBatchRuleCategoryModel;
import com.voc.service.insights.engine.vo.InsBatchRuleCountVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量规则分类服务实现
 * 提供分类的增删改查功能
 */
@Slf4j
@Service
public class InsBatchRuleCategoryServiceImpl extends ServiceImpl<InsBatchRuleCategoryMapper, InsBatchRuleCategoryEntity> implements IInsBatchRuleCategoryService {

    @Resource
    private InsBatchRuleMapper insBatchRuleMapper;

    /**
     * 顶级分类的parentId
     */
    private static final String TOP_PARENT_ID = "0";

    @Override
    public List<InsBatchRuleCategoryModel> queryCategoryTree(String searchKey) {
        // 1. 查询所有分类
        List<InsBatchRuleCategoryEntity> allCategories = baseMapper.selectAllCategories();

        // 2. 转换为 Model
        List<InsBatchRuleCategoryModel> allModels = allCategories.stream()
                .map(e -> BeanUtil.copyProperties(e, InsBatchRuleCategoryModel.class))
                .collect(Collectors.toList());

        // 3. 构建多级树形结构
        List<InsBatchRuleCategoryModel> tree = buildTree(allModels, TOP_PARENT_ID);

        // 4. 根据 searchKey 过滤（只保留匹配分支）
        if (StrUtil.isNotBlank(searchKey)) {
            tree = filterTreeWithPath(tree, searchKey);
        }

        // 5. 统计规则数量
        if (CollUtil.isNotEmpty(tree)) {
            Set<String> categoryIds = new HashSet<>();
            collectCategoryIds(tree, categoryIds);
            Map<String, Integer> ruleCountMap = countByCategoryIds(categoryIds);
            setRuleCount(tree, ruleCountMap);
        }

        return tree;
    }

    /**
     * 构建树形结构（递归）
     *
     * @param allModels 所有分类列表
     * @param parentId  父分类ID
     * @return 树形结构
     */
    private List<InsBatchRuleCategoryModel> buildTree(List<InsBatchRuleCategoryModel> allModels, String parentId) {
        return allModels.stream()
                .filter(node -> parentId.equals(node.getParentId()))
                .peek(node -> node.setChildren(buildTree(allModels, node.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 过滤树，保留匹配节点及其所有祖先节点（只保留匹配分支）
     *
     * @param tree      原始树
     * @param searchKey 搜索关键字
     * @return 过滤后的树
     */
    private List<InsBatchRuleCategoryModel> filterTreeWithPath(List<InsBatchRuleCategoryModel> tree, String searchKey) {
        if (CollUtil.isEmpty(tree)) {
            return Collections.emptyList();
        }

        List<InsBatchRuleCategoryModel> result = new ArrayList<>();
        for (InsBatchRuleCategoryModel node : tree) {
            // 递归过滤子节点
            List<InsBatchRuleCategoryModel> filteredChildren = filterTreeWithPath(node.getChildren(), searchKey);

            // 当前节点是否匹配关键字（name字段模糊匹配）
            boolean selfMatch = StrUtil.containsIgnoreCase(node.getName(), searchKey);

            // 如果自己匹配或子节点有匹配结果，保留此节点
            if (selfMatch || CollUtil.isNotEmpty(filteredChildren)) {
                node.setChildren(filteredChildren);
                result.add(node);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertCategory(InsBatchRuleCategoryModel model) {
        // 1. 处理父分类ID，为空则设为顶级分类
        if (ObjectUtil.isEmpty(model.getParentId())) {
            model.setParentId(TOP_PARENT_ID);
        }
        
        // 2. 检查分类名称是否重复
        if (checkCategoryName(model.getName(), model.getParentId(), null)) {
            throw new BussinessException("分类名称重复，请确认");
        }
        
        // 3. 构建实体
        InsBatchRuleCategoryEntity entity = BeanUtil.copyProperties(model, InsBatchRuleCategoryEntity.class);
        entity.setId(IdWorker.getId());
        entity.setType("batchRule");
        entity.setStatus(ObjectUtil.isEmpty(entity.getStatus()) ? "Enabled" : entity.getStatus());
        entity.setSortOrder(ObjectUtil.isEmpty(entity.getSortOrder()) ? 0 : entity.getSortOrder());
        entity.setDelFlag(0);
        
        // 4. 保存分类
        return save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(InsBatchRuleCategoryModel model) {
        // 1. 检查分类是否存在
        InsBatchRuleCategoryEntity existingCategory = getById(model.getId());
        if (ObjectUtil.isEmpty(existingCategory)) {
            throw new BussinessException("分类不存在");
        }
        
        // 2. 检查分类名称是否重复
        if (checkCategoryName(model.getName(), model.getParentId(), model.getId())) {
            throw new BussinessException("分类名称重复，请确认");
        }
        
        // 3. 检查不能将自己设为子分类
        if (model.getId().equals(model.getParentId())) {
            throw new BussinessException("不能将分类设为自身的子分类");
        }
        
        // 4. 构建实体
        InsBatchRuleCategoryEntity entity = BeanUtil.copyProperties(model, InsBatchRuleCategoryEntity.class);
        
        // 5. 更新分类
        return updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(String categoryId) {
        // 1. 检查分类是否存在
        InsBatchRuleCategoryEntity category = getById(categoryId);
        if (ObjectUtil.isEmpty(category)) {
            throw new BussinessException("分类不存在");
        }
        
        // 2. 检查是否有子分类
        int childCount = baseMapper.countChildren(categoryId);
        if (childCount > 0) {
            throw new BussinessException("该分类下存在子分类，无法删除");
        }
        
        // 3. 检查是否有关联规则
        int ruleCount = insBatchRuleMapper.countByCategoryId(categoryId);
        if (ruleCount > 0) {
            throw new BussinessException("该分类下存在规则，无法删除");
        }
        
        // 4. 软删除分类
        category.setDelFlag(1);
        return updateById(category);
    }

    @Override
    public boolean checkCategoryName(String name, String parentId, String excludeId) {
        // 处理父分类ID，为空则设为顶级分类
        if (ObjectUtil.isEmpty(parentId)) {
            parentId = TOP_PARENT_ID;
        }
        
        // 检查名称是否重复
        int count = baseMapper.checkCategoryName(name, parentId, excludeId);
        return count > 0;
    }

    @Override
    public Map<String, Integer> countByCategoryIds(Set<String> categoryIds) {
        if (CollUtil.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        
        // 查询每个分类的规则数量
        List<InsBatchRuleCountVo> countVos = insBatchRuleMapper.countByCategoryIds(categoryIds);
        return countVos.stream()
                .collect(Collectors.toMap(InsBatchRuleCountVo::getCategoryId, InsBatchRuleCountVo::getCount));
    }

    /**
     * 递归收集所有分类ID
     */
    private void collectCategoryIds(List<InsBatchRuleCategoryModel> categories, Set<String> categoryIds) {
        if (CollUtil.isEmpty(categories)) {
            return;
        }
        
        for (InsBatchRuleCategoryModel category : categories) {
            categoryIds.add(category.getId());
            collectCategoryIds(category.getChildren(), categoryIds);
        }
    }

    /**
     * 递归设置每个分类的规则数量
     */
    private void setRuleCount(List<InsBatchRuleCategoryModel> categories, Map<String, Integer> ruleCountMap) {
        if (CollUtil.isEmpty(categories)) {
            return;
        }
        
        for (InsBatchRuleCategoryModel category : categories) {
            category.setRuleCount(ruleCountMap.getOrDefault(category.getId(), 0));
            setRuleCount(category.getChildren(), ruleCountMap);
        }
    }
}
