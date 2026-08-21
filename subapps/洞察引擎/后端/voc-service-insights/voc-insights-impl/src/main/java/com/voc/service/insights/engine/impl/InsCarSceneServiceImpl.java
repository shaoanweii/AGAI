package com.voc.service.insights.engine.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsCarSceneService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.entity.InsCarSceneCategoryEntity;
import com.voc.service.insights.engine.entity.InsCarSceneEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.listener.CarSceneExcelListener;
import com.voc.service.insights.engine.listener.TagClientExcelListener;
import com.voc.service.insights.engine.mapper.InsCarSceneCategoryMapper;
import com.voc.service.insights.engine.mapper.InsCarSceneMapper;
import com.voc.service.insights.engine.model.InsCarSceneExcelModel;
import com.voc.service.insights.engine.model.InsCarSceneModel;
import com.voc.service.insights.engine.model.TagLibExcelModel;
import com.voc.service.insights.engine.vo.InsCarSceneOperatorVo;
import com.voc.service.insights.engine.vo.InsCarSceneVo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InsCarSceneServiceImpl extends ServiceImpl<InsCarSceneMapper, InsCarSceneEntity>
        implements IInsCarSceneService {
    private static final Logger log = LoggerFactory.getLogger(InsCarSceneServiceImpl.class);
    private static final String SYNONYM_SPLIT_REGEX = "[、,，;；\\n\\r]+";

    @Resource
    private InsConvertMapperService insConvertMapperService;
    @Autowired
    private InsCarSceneCategoryMapper carSceneCategoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCarScene(InsCarSceneModel model) {
        log.info("新增用车场景开始, model={},", JSONObject.toJSONString(model));
        this.checkParameter(model);
        String userId = ServiceContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();

        InsCarSceneEntity entity = insConvertMapperService.carSceneModelConvertEntity(model);
        entity.setId(IdWorker.getId());
        entity.setCreateBy(userId);
        entity.setUpdateBy(userId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        boolean save = this.save(entity);
        if (save) {
            log.info("新增用车场景成功");
            return;
        }
        throw new BussinessException(InsCommonErrorEnum.SAVE_CAR_SCENE_ERROR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCarScene(InsCarSceneModel model) {
        log.info("修改用车场景开始, model={},", JSONObject.toJSONString(model));
        Assert.notNull(model, "用车场景参数不能为空");
        Assert.hasText(model.getId(), "用车场景id不能为空");
        this.checkParameter(model);

        InsCarSceneEntity entity = insConvertMapperService.carSceneModelConvertEntity(model);
        entity.setUpdateBy(ServiceContextHolder.getUserId());
        entity.setUpdateTime(LocalDateTime.now());

        boolean update = this.updateById(entity);
        if (update) {
            log.info("修改用车场景成功");
            return;
        }
        throw new BussinessException(InsCommonErrorEnum.UPDATE_CAR_SCENE_ERROR);
    }

    @Override
    public IPage<InsCarSceneVo> findCarSceneList(InsCarSceneModel model) {
        log.info("分页查询用车场景开始, model={},", JSONObject.toJSONString(model));
        // 用车场景不再使用分类关系，列表按场景自身条件直接查询。
        IPage<InsCarSceneVo> page = new Page<>(model.getPageNum(), model.getPageSize());
        IPage<InsCarSceneVo> result = this.baseMapper.findCarSceneList(page, model);
        log.info("分页查询用车场景结束, total={}", result == null ? 0 : result.getTotal());
        return result;
    }

    @Override
    public List<InsCarSceneOperatorVo> findCarSceneOperatorList(Boolean isAllVisible) {
        log.info("查询用车场景操作人列表开始, isAllVisible={}", isAllVisible);
        List<String> userIds = new ArrayList<>();
        if (Boolean.TRUE.equals(isAllVisible)) {
            List<String> operatorUserIds = this.baseMapper.findCarSceneOperatorUserIds();
            if (ObjectUtils.isEmpty(operatorUserIds)) {
                return List.of();
            }
            userIds.addAll(operatorUserIds);
        } else {
            userIds.add(ServiceContextHolder.getUserId());
        }
        final String systemId = ServiceContextHolder.getSystemId();
        List<InsCarSceneOperatorVo> result = this.baseMapper.findVisibleCarSceneOperatorList(userIds,systemId);
        log.info("查询用车场景操作人列表结束, size={}", result == null ? 0 : result.size());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchChangeStatus(InsCarSceneModel model) {
        log.info("批量修改用车场景状态开始, model={},", JSONObject.toJSONString(model));
        Assert.notNull(model, "参数不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIds()), "id集合不能为空");
        Assert.hasText(model.getStatus(), "状态不能为空");

        UpdateWrapper<InsCarSceneEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsCarSceneEntity::getId, model.getIds());
        updateWrapper.lambda().set(InsCarSceneEntity::getStatus, model.getStatus());
        updateWrapper.lambda().set(InsCarSceneEntity::getUpdateBy, ServiceContextHolder.getUserId());
        updateWrapper.lambda().set(InsCarSceneEntity::getUpdateTime, LocalDateTime.now());

        boolean update = this.update(updateWrapper);
        if (update) {
            log.info("批量修改用车场景状态成功");
            return;
        }
        throw new BussinessException(InsCommonErrorEnum.BATCH_CHANGE_STATUS_ERROR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMoveCarScene(InsCarSceneModel model) {
        log.info("批量移动用车场景开始, model={},", JSONObject.toJSONString(model));
        Assert.notNull(model, "参数不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIds()), "id集合不能为空");
        Assert.hasText(model.getCategoryId(), "目标用车场景分类id不能为空");

        UpdateWrapper<InsCarSceneEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsCarSceneEntity::getId, model.getIds());
        updateWrapper.lambda().set(InsCarSceneEntity::getCategoryId, model.getCategoryId());
        updateWrapper.lambda().set(InsCarSceneEntity::getUpdateBy, ServiceContextHolder.getUserId());
        updateWrapper.lambda().set(InsCarSceneEntity::getUpdateTime, LocalDateTime.now());

        boolean update = this.update(updateWrapper);
        if (update) {
            log.info("批量移动用车场景成功");
            return;
        }
        throw new BussinessException(InsCommonErrorEnum.BATCH_MOVE_TAGLIB_CLIENT_ERROR);
    }

    @Override
    public void analyzeExcelData(List<InsCarSceneExcelModel> list) {
        list.stream().forEach(e->{
            final String categoryName = e.getCategoryName();
            QueryWrapper<InsCarSceneCategoryEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(InsCarSceneCategoryEntity::getCategoryName, categoryName);
            InsCarSceneCategoryEntity insCarSceneCategoryEntity = carSceneCategoryMapper.selectOne(queryWrapper);
            String categoryId = null;
            if(ObjectUtils.isEmpty(insCarSceneCategoryEntity)){
                final String id = IdWorker.getId();
                InsCarSceneCategoryEntity build = InsCarSceneCategoryEntity.builder()
                        .id(id)
                        .categoryName(categoryName)
                        .patentId("0")
                        .level(2)
                        .status("1")
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .createBy(ServiceContextHolder.getUserId())
                        .updateBy(ServiceContextHolder.getUserId())
                        .build();
                int insert = carSceneCategoryMapper.insert(build);
                if(insert>0){
                    log.info("新增分类[{}]成功",categoryName);
                    categoryId = id;
                }else{
                    log.error("新增分类[{}]失败",categoryName);
                }
            }else{
                categoryId = insCarSceneCategoryEntity.getId();
            }

            final String sceneName = e.getSceneName();
            QueryWrapper<InsCarSceneEntity> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.lambda().eq(InsCarSceneEntity::getSceneName, sceneName);
            InsCarSceneEntity one = this.getOne(queryWrapper2);
            List<InsCarSceneEntity> carSceneEntityList = new ArrayList<>();
            if(ObjectUtils.isEmpty(one)){
                if(ObjectUtils.isNotEmpty(e.getSynonyms())){
                    if(e.getSynonyms().contains("、")){
                        String[] split = e.getSynonyms().split("、");
                        for(String synonym : split){
                            InsCarSceneEntity build = InsCarSceneEntity.builder()
                                    .id(IdWorker.getId())
                                    .categoryId(categoryId)
                                    .sceneName(sceneName)
                                    .synonyms(synonym)
                                    .status("1")
                                    .createTime(LocalDateTime.now())
                                    .updateTime(LocalDateTime.now())
                                    .createBy(ServiceContextHolder.getUserId())
                                    .updateBy(ServiceContextHolder.getUserId())
                                    .build();
                            carSceneEntityList.add(build);
                        }
                    }else{
                        InsCarSceneEntity build = InsCarSceneEntity.builder()
                                .id(IdWorker.getId())
                                .categoryId(categoryId)
                                .sceneName(sceneName)
                                .synonyms(e.getSynonyms())
                                .status("1")
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .createBy(ServiceContextHolder.getUserId())
                                .updateBy(ServiceContextHolder.getUserId())
                                .build();
                        carSceneEntityList.add(build);
                    }
                }

                boolean save = this.saveBatch(carSceneEntityList);
                if(save){
                    log.info("新增用车场景[{}]成功",categoryName);
                }else{
                    log.info("新增用车场景[{}]失败",categoryName);
                }
            }

        });
    }

    @Override
    public void uploadExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), InsCarSceneExcelModel.class, new CarSceneExcelListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkParameter(InsCarSceneModel model) {
        Assert.notNull(model, "用车场景参数不能为空");
        Assert.hasText(model.getSceneName(), "用车场景名称不允许为空");
        Assert.hasText(model.getStatus(), "状态不允许为空");
        if (StringUtils.isNotBlank(model.getSceneDescription())) {
            Assert.isTrue(model.getSceneDescription().length() <= 200, "用车场景描述长度不允许超过200");
        }
        if (StringUtils.isNotBlank(model.getSynonyms())) {
            Assert.isTrue(model.getSynonyms().length() <= 1000, "同义词长度不允许超过1000");
            this.checkSynonymsDuplicate(model.getSynonyms());
        }
        QueryWrapper<InsCarSceneEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsCarSceneEntity::getSceneName, model.getSceneName());
        if (StringUtils.isNotBlank(model.getId())) {
            queryWrapper.lambda().ne(InsCarSceneEntity::getId, model.getId());
        }
        long count = this.count(queryWrapper);
        Assert.isTrue(count == 0, "用车场景名称不允许重复");
    }

    private void checkSynonymsDuplicate(String synonyms) {
        List<String> synonymList = Arrays.stream(synonyms.split(SYNONYM_SPLIT_REGEX))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Set<String> synonymSet = new LinkedHashSet<>();
        for (String synonym : synonymList) {
            Assert.isTrue(synonymSet.add(synonym), "同义词中不允许有重复");
        }
    }
}
