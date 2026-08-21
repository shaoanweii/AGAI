package com.voc.service.insights.engine.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsCarSceneCategoryService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.entity.InsCarSceneCategoryEntity;
import com.voc.service.insights.engine.entity.InsCarSceneEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsCarSceneMapper;
import com.voc.service.insights.engine.mapper.InsCarSceneCategoryMapper;
import com.voc.service.insights.engine.model.InsCarSceneCategoryModel;
import com.voc.service.insights.engine.vo.InsCarSceneCategoryVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InsCarSceneCategoryServiceImpl extends ServiceImpl<InsCarSceneCategoryMapper, InsCarSceneCategoryEntity>
        implements IInsCarSceneCategoryService {
    private static final Logger log = LoggerFactory.getLogger(InsCarSceneCategoryServiceImpl.class);
    private static final String STATUS_DISABLED = "0";
    private static final String STATUS_ENABLED = "1";
    private static final String NODE_TYPE_CATEGORY = "category";
    private static final String NODE_TYPE_SCENE = "scene";

    @Resource
    private InsConvertMapperService insConvertMapperService;

    @Resource
    private InsCarSceneMapper insCarSceneMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCarSceneCategory(InsCarSceneCategoryModel model) {
        log.info("新增用车场景分类开始, model={},", JSONObject.toJSONString(model));
        this.checkParameter(model);
        final String userId = ServiceContextHolder.getUserId();
        InsCarSceneCategoryEntity entity = insConvertMapperService.carSceneCategoryModelConvertEntity(model);
        entity.setId(IdWorker.getId());
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreateBy(userId);
        entity.setUpdateBy(userId);
        boolean save = this.save(entity);
        if (save) {
            log.info("用车场景分类保存成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_CAR_SCENE_CATEGORY_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCarSceneCategory(InsCarSceneCategoryModel model) {
        log.info("更新用车场景分类开始, model={},", JSONObject.toJSONString(model));
        this.checkParameter(model);
        Assert.hasText(model.getId(), "用车场景分类id不能为空");
        final String userId = ServiceContextHolder.getUserId();
        InsCarSceneCategoryEntity currentEntity = this.getById(model.getId());
        Assert.notNull(currentEntity, "用车场景分类不存在");
        LocalDateTime now = LocalDateTime.now();
        InsCarSceneCategoryEntity entity = insConvertMapperService.carSceneCategoryModelConvertEntity(model);
        entity.setUpdateTime(now);
        entity.setUpdateBy(userId);
        boolean b = this.updateById(entity);
        if (!b) {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_CAR_SCENE_CATEGORY_ERROR);
        }
        this.syncStatusToDescendantsAndScenesIfNeeded(model.getId(), currentEntity.getStatus(), model.getStatus(), userId, now);
        log.info("用车场景分类更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCarSceneCategory(InsCarSceneCategoryModel model) {
        log.info("删除用车场景分类开始, model={},", JSONObject.toJSONString(model));
        Assert.notNull(model, "用车场景分类参数不能为空");
        Assert.hasText(model.getId(), "用车场景分类id不能为空");

        QueryWrapper<InsCarSceneCategoryEntity> childQueryWrapper = new QueryWrapper<>();
        childQueryWrapper.eq("patent_id", model.getId());
        childQueryWrapper.ne("id", model.getId());
        long childCount = this.count(childQueryWrapper);
        log.info("删除用车场景分类校验下级分类数量, id={}, childCount={}", model.getId(), childCount);
        Assert.isTrue(childCount == 0, "当前分类下存在下级分类，不允许删除");

        QueryWrapper<InsCarSceneEntity> sceneQueryWrapper = new QueryWrapper<>();
        sceneQueryWrapper.eq("category_id", model.getId());
        Long sceneCount = insCarSceneMapper.selectCount(sceneQueryWrapper);
        log.info("删除用车场景分类校验场景数量, id={}, sceneCount={}", model.getId(), sceneCount);
        Assert.isTrue(sceneCount == null || sceneCount == 0, "当前分类下存在用车场景数据，不允许删除");

        boolean removed = this.removeById(model.getId());
        if (removed) {
            log.info("用车场景分类删除成功");
            return;
        }
        throw new BussinessException(InsCommonErrorEnum.REMOVE_CATEGORY_ERROR);
    }

    @Override
    public List<InsCarSceneCategoryVo> findCarSceneCategoryList(InsCarSceneCategoryModel model) {
        log.info("查询用车场景分类列表开始, model={},", JSONObject.toJSONString(model));
        QueryWrapper<InsCarSceneCategoryEntity> queryWrapper = this.buildQueryWrapper(model);
        queryWrapper.orderByAsc("create_time");
        List<InsCarSceneCategoryEntity> matchedList = this.list(queryWrapper);
        if (matchedList == null || matchedList.isEmpty()) {
            log.info("查询用车场景分类列表结束, 无匹配数据");
            return new ArrayList<>();
        }

        List<InsCarSceneCategoryEntity> entityList = matchedList;
        if (matchedList.stream().anyMatch(e -> e.getLevel() != null && e.getLevel() != 1)) {
            entityList = this.appendAncestorNodes(matchedList);
        }

        List<InsCarSceneCategoryVo> voList = insConvertMapperService.carSceneCategoryEntityListConvertVoList(entityList);
        voList.forEach(vo -> vo.setNodeType(NODE_TYPE_CATEGORY));
        Map<String, Integer> sceneCountMap = this.buildCategorySceneCountMap(voList);
        voList.forEach(vo -> vo.setLeafCount(sceneCountMap.getOrDefault(vo.getId(), 0)));
        List<InsCarSceneCategoryVo> result = this.buildTree(voList);
        log.info("查询用车场景分类列表结束, resultSize={}", result.size());
        return result;
    }

    @Override
    public List<InsCarSceneCategoryVo> findCarSceneCategoryTree(InsCarSceneCategoryModel model) {
        log.info("查询用车场景分类树开始, model={},", JSONObject.toJSONString(model));
        List<InsCarSceneCategoryVo> categoryTree = this.findCarSceneCategoryList(model);
        if (categoryTree == null || categoryTree.isEmpty()) {
            log.info("查询用车场景分类树结束, 无分类数据");
            return new ArrayList<>();
        }

        List<InsCarSceneCategoryVo> categoryNodes = this.flattenCategoryTree(categoryTree);
        this.appendCarSceneNodes(categoryNodes, model);
        log.info("查询用车场景分类树结束, rootSize={}", categoryTree.size());
        return categoryTree;
    }

    private QueryWrapper<InsCarSceneCategoryEntity> buildQueryWrapper(InsCarSceneCategoryModel model) {
        log.info("构建用车场景分类查询条件, model={},", JSONObject.toJSONString(model));
        QueryWrapper<InsCarSceneCategoryEntity> queryWrapper = new QueryWrapper<>();
        if (model == null) {
            return queryWrapper;
        }
        if (StringUtils.isNotBlank(model.getId())) {
            queryWrapper.eq("id", model.getId());
        }
        if (StringUtils.isNotBlank(model.getPatentId())) {
            queryWrapper.eq("patent_id", model.getPatentId());
        }
        if (StringUtils.isNotBlank(model.getCategoryName())) {
            queryWrapper.like("category_name", model.getCategoryName());
        }
        if (StringUtils.isNotBlank(model.getCategoryDescription())) {
            queryWrapper.like("category_description", model.getCategoryDescription());
        }
        if (model.getLevel() != null) {
            queryWrapper.eq("level", model.getLevel());
        }
        if (StringUtils.isNotBlank(model.getSynonyms())) {
            queryWrapper.like("synonyms", model.getSynonyms());
        }
        if (StringUtils.isNotBlank(model.getStatus())) {
            queryWrapper.eq("status", model.getStatus());
        }
        if (model.getIdsList() != null && !model.getIdsList().isEmpty()) {
            queryWrapper.in("id", model.getIdsList());
        }
        return queryWrapper;
    }

    private List<InsCarSceneCategoryVo> buildTree(List<InsCarSceneCategoryVo> voList) {
        log.info("组装用车场景分类树开始, inputSize={}", voList == null ? 0 : voList.size());
        if (voList == null || voList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, InsCarSceneCategoryVo> nodeMap = new LinkedHashMap<>();
        for (InsCarSceneCategoryVo vo : voList) {
            vo.setChildren(new ArrayList<>());
            if (StringUtils.isNotBlank(vo.getId())) {
                nodeMap.putIfAbsent(vo.getId(), vo);
            }
        }

        List<InsCarSceneCategoryVo> roots = new ArrayList<>();
        for (InsCarSceneCategoryVo vo : voList) {
            String parentId = vo.getPatentId();
            if (StringUtils.isBlank(parentId) || StringUtils.equals(parentId, vo.getId())) {
                roots.add(vo);
                continue;
            }
            InsCarSceneCategoryVo parent = nodeMap.get(parentId);
            if (parent == null) {
                roots.add(vo);
                continue;
            }
            parent.getChildren().add(vo);
        }
        roots.forEach(this::accumulateLeafCount);
        log.info("组装用车场景分类树结束, rootSize={}", roots.size());
        return roots;
    }

    private List<InsCarSceneCategoryVo> flattenCategoryTree(List<InsCarSceneCategoryVo> categoryTree) {
        List<InsCarSceneCategoryVo> result = new ArrayList<>();
        if (categoryTree == null || categoryTree.isEmpty()) {
            return result;
        }
        for (InsCarSceneCategoryVo category : categoryTree) {
            this.flattenCategoryNode(category, result);
        }
        return result;
    }

    private void flattenCategoryNode(InsCarSceneCategoryVo category, List<InsCarSceneCategoryVo> result) {
        if (category == null) {
            return;
        }
        category.setNodeType(NODE_TYPE_CATEGORY);
        result.add(category);
        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return;
        }
        for (InsCarSceneCategoryVo child : category.getChildren()) {
            this.flattenCategoryNode(child, result);
        }
    }

    private void appendCarSceneNodes(List<InsCarSceneCategoryVo> categoryNodes, InsCarSceneCategoryModel model) {
        log.info("追加用车场景节点开始, categorySize={}", categoryNodes == null ? 0 : categoryNodes.size());
        if (categoryNodes == null || categoryNodes.isEmpty()) {
            return;
        }
        List<String> categoryIds = categoryNodes.stream()
                .map(InsCarSceneCategoryVo::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return;
        }

        QueryWrapper<InsCarSceneEntity> sceneQueryWrapper = new QueryWrapper<>();
        sceneQueryWrapper.in("category_id", categoryIds);
        if (model != null && StringUtils.isNotBlank(model.getStatus())) {
            sceneQueryWrapper.eq("status", model.getStatus());
        }
        sceneQueryWrapper.orderByAsc("create_time");
        List<InsCarSceneEntity> sceneList = insCarSceneMapper.selectList(sceneQueryWrapper);
        if (sceneList == null || sceneList.isEmpty()) {
            log.info("追加用车场景节点结束, 无场景数据");
            return;
        }

        Map<String, List<InsCarSceneCategoryVo>> sceneNodeMap = sceneList.stream()
                .filter(scene -> StringUtils.isNotBlank(scene.getCategoryId()))
                .map(this::buildCarSceneNode)
                .collect(Collectors.groupingBy(InsCarSceneCategoryVo::getPatentId, LinkedHashMap::new, Collectors.toList()));
        for (InsCarSceneCategoryVo category : categoryNodes) {
            List<InsCarSceneCategoryVo> sceneNodes = sceneNodeMap.get(category.getId());
            if (sceneNodes == null || sceneNodes.isEmpty()) {
                continue;
            }
            if (category.getChildren() == null) {
                category.setChildren(new ArrayList<>());
            }
            if (category.getLevel() != null) {
                sceneNodes.forEach(sceneNode -> sceneNode.setLevel(category.getLevel() + 1));
            }
            category.getChildren().addAll(sceneNodes);
        }
        log.info("追加用车场景节点结束, sceneSize={}", sceneList.size());
    }

    private InsCarSceneCategoryVo buildCarSceneNode(InsCarSceneEntity scene) {
        String sceneName = scene.getSceneName();
        return InsCarSceneCategoryVo.builder()
                .id(scene.getId())
                .patentId(scene.getCategoryId())
                .categoryId(scene.getCategoryId())
                .categoryName(sceneName)
                .categoryDescription(scene.getSceneDescription())
                .typeName(sceneName)
                .sceneName(sceneName)
                .sceneDescription(scene.getSceneDescription())
                .synonyms(scene.getSynonyms())
                .status(scene.getStatus())
                .nodeType(NODE_TYPE_SCENE)
                .leafCount(0)
                .children(new ArrayList<>())
                .build();
    }

    private int accumulateLeafCount(InsCarSceneCategoryVo vo) {
        log.info("累加分类场景数量, id={}", vo == null ? null : vo.getId());
        int total = vo.getLeafCount() == null ? 0 : vo.getLeafCount();
        if (vo.getChildren() == null || vo.getChildren().isEmpty()) {
            vo.setLeafCount(total);
            return total;
        }
        for (InsCarSceneCategoryVo child : vo.getChildren()) {
            total += this.accumulateLeafCount(child);
        }
        vo.setLeafCount(total);
        return total;
    }

    private Map<String, Integer> buildCategorySceneCountMap(List<InsCarSceneCategoryVo> voList) {
        log.info("统计分类下场景数量开始, categorySize={}", voList == null ? 0 : voList.size());
        if (voList == null || voList.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> categoryIds = voList.stream()
                .map(InsCarSceneCategoryVo::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Map<String, Object>> countRows = insCarSceneMapper.countSceneByCategoryIds(categoryIds);
        if (countRows == null || countRows.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> sceneCountMap = new HashMap<>();
        for (Map<String, Object> row : countRows) {
            Object categoryIdObj = row.get("categoryId");
            if (categoryIdObj == null) {
                continue;
            }
            String categoryId = String.valueOf(categoryIdObj);
            Object countObj = row.get("sceneCount");
            int count = countObj == null ? 0 : Integer.parseInt(String.valueOf(countObj));
            sceneCountMap.put(categoryId, count);
        }
        log.info("统计分类下场景数量结束, resultSize={}", sceneCountMap.size());
        return sceneCountMap;
    }

    private List<InsCarSceneCategoryEntity> appendAncestorNodes(List<InsCarSceneCategoryEntity> matchedList) {
        log.info("补齐父级分类开始, matchedSize={}", matchedList == null ? 0 : matchedList.size());
        Map<String, InsCarSceneCategoryEntity> nodeMap = new LinkedHashMap<>();
        for (InsCarSceneCategoryEntity entity : matchedList) {
            if (StringUtils.isNotBlank(entity.getId())) {
                nodeMap.put(entity.getId(), entity);
            }
        }

        List<InsCarSceneCategoryEntity> allCategories = this.list();
        if (allCategories == null || allCategories.isEmpty()) {
            return new ArrayList<>(nodeMap.values());
        }
        Map<String, InsCarSceneCategoryEntity> allNodeMap = allCategories.stream()
                .filter(e -> StringUtils.isNotBlank(e.getId()))
                .collect(Collectors.toMap(InsCarSceneCategoryEntity::getId, e -> e, (v1, v2) -> v1));

        for (InsCarSceneCategoryEntity entity : matchedList) {
            Set<String> visited = new HashSet<>();
            String parentId = entity.getPatentId();
            while (StringUtils.isNotBlank(parentId) && visited.add(parentId)) {
                InsCarSceneCategoryEntity parent = allNodeMap.get(parentId);
                if (parent == null || StringUtils.isBlank(parent.getId())) {
                    break;
                }
                nodeMap.putIfAbsent(parent.getId(), parent);
                if (StringUtils.equals(parent.getPatentId(), parent.getId())) {
                    break;
                }
                parentId = parent.getPatentId();
            }
        }
        List<InsCarSceneCategoryEntity> result = new ArrayList<>(nodeMap.values());
        log.info("补齐父级分类结束, resultSize={}", result.size());
        return result;
    }

    private void syncStatusToDescendantsAndScenesIfNeeded(String categoryId, String oldStatus, String newStatus,
                                                          String userId, LocalDateTime updateTime) {
        if (!this.shouldCascadeStatusChange(oldStatus, newStatus)) {
            return;
        }
        List<String> categoryIds = this.findCategoryAndDescendantIds(categoryId);
        if (categoryIds.isEmpty()) {
            return;
        }

        List<String> descendantIds = categoryIds.stream()
                .filter(id -> !StringUtils.equals(id, categoryId))
                .collect(Collectors.toList());
        if (!descendantIds.isEmpty()) {
            UpdateWrapper<InsCarSceneCategoryEntity> categoryUpdateWrapper = new UpdateWrapper<>();
            categoryUpdateWrapper.lambda().in(InsCarSceneCategoryEntity::getId, descendantIds);
            categoryUpdateWrapper.lambda().set(InsCarSceneCategoryEntity::getStatus, newStatus);
            categoryUpdateWrapper.lambda().set(InsCarSceneCategoryEntity::getUpdateBy, userId);
            categoryUpdateWrapper.lambda().set(InsCarSceneCategoryEntity::getUpdateTime, updateTime);
            this.baseMapper.update(null, categoryUpdateWrapper);
        }

        UpdateWrapper<InsCarSceneEntity> sceneUpdateWrapper = new UpdateWrapper<>();
        sceneUpdateWrapper.lambda().in(InsCarSceneEntity::getCategoryId, categoryIds);
        sceneUpdateWrapper.lambda().set(InsCarSceneEntity::getStatus, newStatus);
        sceneUpdateWrapper.lambda().set(InsCarSceneEntity::getUpdateBy, userId);
        sceneUpdateWrapper.lambda().set(InsCarSceneEntity::getUpdateTime, updateTime);
        insCarSceneMapper.update(null, sceneUpdateWrapper);
    }

    private boolean shouldCascadeStatusChange(String oldStatus, String newStatus) {
        return StringUtils.equals(oldStatus, STATUS_ENABLED) && StringUtils.equals(newStatus, STATUS_DISABLED)
                || StringUtils.equals(oldStatus, STATUS_DISABLED) && StringUtils.equals(newStatus, STATUS_ENABLED);
    }

    private List<String> findCategoryAndDescendantIds(String categoryId) {
        Set<String> categoryIds = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(categoryId)) {
            categoryIds.add(categoryId);
        }
        List<String> mapperCategoryIds = insCarSceneMapper.findCategoryAndDescendantIds(categoryId);
        if (mapperCategoryIds != null && !mapperCategoryIds.isEmpty()) {
            categoryIds.addAll(mapperCategoryIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        }
        return new ArrayList<>(categoryIds);
    }

    private void checkParameter(InsCarSceneCategoryModel model) {
        Assert.notNull(model, "用车场景分类参数不能为空");
        Assert.hasText(model.getCategoryName(), "分类名称不允许为空");
        if (StringUtils.isNotBlank(model.getCategoryDescription())) {
            Assert.isTrue(model.getCategoryDescription().length() <= 200, "分类描述长度不允许超过200");
        }
        if (StringUtils.isNotBlank(model.getSynonyms())) {
            Assert.isTrue(model.getSynonyms().length() <= 1000, "同义词长度不允许超过1000");
        }
        Assert.hasText(model.getStatus(), "状态不允许为空");
        QueryWrapper<InsCarSceneCategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsCarSceneCategoryEntity::getCategoryName, model.getCategoryName());
        if (StringUtils.isNotBlank(model.getId())) {
            queryWrapper.lambda().ne(InsCarSceneCategoryEntity::getId, model.getId());
        }
        long count = this.count(queryWrapper);
        Assert.isTrue(count == 0, "分类名称不允许重复");
    }
}
