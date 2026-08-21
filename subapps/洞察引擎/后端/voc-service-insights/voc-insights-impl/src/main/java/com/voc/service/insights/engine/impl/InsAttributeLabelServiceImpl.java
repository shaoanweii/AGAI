package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsAttributeLabelService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.entity.InsAttributeLabelEntity;
import com.voc.service.insights.engine.entity.InsTagLibClientEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.listener.AttributeLabelExcelListener;
import com.voc.service.insights.engine.listener.AutomarkExcelListener;
import com.voc.service.insights.engine.mapper.InsAttributeLabelMapper;
import com.voc.service.insights.engine.mapper.InsTagLibClientMapper;
import com.voc.service.insights.engine.model.InsAttributeLabelModel;
import com.voc.service.insights.engine.model.InsAutomarkExcelModel;
import com.voc.service.insights.engine.vo.InsAttributeLabelVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2026/4/9 13:40
 * @描述:
 **/
@Service
public class InsAttributeLabelServiceImpl extends ServiceImpl<InsAttributeLabelMapper, InsAttributeLabelEntity> implements IInsAttributeLabelService {
    private static final Logger log = LoggerFactory.getLogger(InsAttributeLabelServiceImpl.class);
    @Autowired
    private InsConvertMapperService convertMapperService;
    @Autowired
    private InsTagLibClientMapper tagLibClientMapper;

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public void saveAttributeLabel(InsAttributeLabelModel model) {
        // 保存前先校验参数和名称唯一性
        this.checkParameter(model);
        log.info("开始新增属性标签, name:{}, status:{}", model.getName(), model.getStatus());

        // 获取当前操作人和当前时间
        final String userId = ServiceContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // 通过MapStruct转换待保存实体，再补充公共字段
        InsAttributeLabelEntity entity = convertMapperService.attributeLabelModelConvertEntity(model);
        entity.setId(IdWorker.getId());
        entity.setCreateUser(userId);
        entity.setUpdateUser(userId);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        // 执行保存
        boolean save = this.save(entity);
        if (!save) {
            log.error("新增属性标签失败, name:{}, status:{}", model.getName(), model.getStatus());
            throw new BussinessException(InsCommonErrorEnum.SAVE_ATTRIBUTE_LABEL_ERROR);
        }
        log.info("新增属性标签成功, id:{}, name:{}", entity.getId(), entity.getName());
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public void updateAttributeLabel(InsAttributeLabelModel model) {
        // 编辑时主键不能为空
        Assert.hasLength(model.getId(), "id不能为空");

        // 先校验数据是否存在
        InsAttributeLabelEntity source = this.getById(model.getId());
        if (ObjectUtils.isEmpty(source)) {
            log.warn("编辑属性标签失败, 数据不存在, id:{}", model.getId());
            throw new BussinessException(InsCommonErrorEnum.ATTRIBUTE_LABEL_NOT_EXIST);
        }

        // 校验编辑参数和名称唯一性
        this.checkParameter(model);
        log.info("开始编辑属性标签, id:{}, oldName:{}, newName:{}, status:{}", model.getId(), source.getName(), model.getName(), model.getStatus());

        final String userId = ServiceContextHolder.getUserId();
        // 通过MapStruct转换待更新实体，再补充更新字段
        InsAttributeLabelEntity entity = convertMapperService.attributeLabelModelConvertEntity(model);
        entity.setUpdateUser(userId);
        entity.setUpdateTime(LocalDateTime.now());

        // 执行更新
        boolean update = this.updateById(entity);
        if (!update) {
            log.error("编辑属性标签失败, id:{}, name:{}", model.getId(), model.getName());
            throw new BussinessException(InsCommonErrorEnum.UPDATE_ATTRIBUTE_LABEL_ERROR);
        }
        this.refreshTopicScenarioAttr(source, entity.getName());
        log.info("编辑属性标签成功, id:{}, name:{}", model.getId(), model.getName());
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public void batchChangeStatus(InsAttributeLabelModel model) {
        // 批量修改状态时必须传入id集合和目标状态
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIds()), "ids不能为空");
        Assert.hasLength(model.getStatus(), "状态不能为空");

        log.info("开始批量修改属性标签状态, ids:{}, status:{}", model.getIds(), model.getStatus());
        final String userId = ServiceContextHolder.getUserId();
        // 构建批量更新条件
        UpdateWrapper<InsAttributeLabelEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsAttributeLabelEntity::getId, model.getIds());
        updateWrapper.lambda().set(InsAttributeLabelEntity::getStatus, model.getStatus());
        updateWrapper.lambda().set(InsAttributeLabelEntity::getUpdateUser, userId);
        updateWrapper.lambda().set(InsAttributeLabelEntity::getUpdateTime, LocalDateTime.now());

        // 执行批量更新
        boolean update = this.update(updateWrapper);
        if (!update) {
            log.error("批量修改属性标签状态失败, ids:{}, status:{}", model.getIds(), model.getStatus());
            throw new BussinessException(InsCommonErrorEnum.BATCH_CHANGE_STATUS_ERROR);
        }
        log.info("批量修改属性标签状态成功, count:{}, status:{}", model.getIds().size(), model.getStatus());
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public IPage<InsAttributeLabelVo> findAttributeLabelList(InsAttributeLabelModel model) {
        // 初始化分页对象
        IPage<InsAttributeLabelEntity> page = new Page<>(model.getPageNum(), model.getPageSize());

        // 对查询条件做去空格处理
        String name = StrUtil.trim(model.getName());
        String status = StrUtil.trim(model.getStatus());
        model.setName(name);
        model.setStatus(status);

        log.info("分页查询属性标签列表, pageNum:{}, pageSize:{}, name:{}, status:{}", model.getPageNum(), model.getPageSize(), name, status);

        // 通过XML自定义SQL查询后转换为VO返回
        IPage<InsAttributeLabelVo> result = this.baseMapper.findAttributeLabelList(page, model).convert(convertMapperService::attributeLabelEntityConvertVo);
        log.info("分页查询属性标签列表完成, total:{}", result.getTotal());
        return result;
    }

    @Override
    @SwitchClientDS(objectAttribute = "model.clientId")
    public List<InsAttributeLabelVo> findAllAttributeLabelList(InsAttributeLabelModel model) {
        // 对查询条件做去空格处理，状态为空时默认查询已启用
        String name = StrUtil.trim(model.getName());
        String status = StrUtil.trim(model.getStatus());
        model.setName(name);
        model.setStatus(StrUtil.isBlank(status) ? "1" : status);

        log.info("查询属性标签列表, name:{}, status:{}", model.getName(), model.getStatus());
        List<InsAttributeLabelEntity> entityList = this.baseMapper.findAllAttributeLabelList(model);
        List<InsAttributeLabelVo> result = convertMapperService.attributeLabelEntityConvertVoList(entityList);
        log.info("查询属性标签列表完成, count:{}", ObjectUtils.isEmpty(result) ? 0 : result.size());
        return result;
    }

    @Override
    public void uploadExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), InsAttributeLabelModel.class, new AttributeLabelExcelListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void analyzeExcelData(List<InsAttributeLabelModel> list) {
//        List<InsAttributeLabelEntity> collect = list.stream().map(e -> {
//            LocalDateTime now = LocalDateTime.now();
//            return InsAttributeLabelEntity.builder()
//                    .id(IdWorker.getId())
//                    .name(e.getName())
//                    .status("1")
//                    .createTime(now)
//                    .updateTime(now)
//                    .build();
//        }).collect(Collectors.toList());
//        this.saveBatch(collect);

    }

    private void checkParameter(InsAttributeLabelModel model) {
        // 去除前后空格，避免空白字符影响校验
        model.setName(StrUtil.trim(model.getName()));
        model.setStatus(StrUtil.trim(model.getStatus()));

        // 校验基础必填项
        Assert.hasText(model.getName(), "名称不能为空");
        Assert.hasText(model.getStatus(), "状态不能为空");

        // 校验名称唯一性，编辑时排除自身
        QueryWrapper<InsAttributeLabelEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsAttributeLabelEntity::getName, model.getName());
        if (StrUtil.isNotBlank(model.getId())) {
            queryWrapper.lambda().ne(InsAttributeLabelEntity::getId, model.getId());
        }
        InsAttributeLabelEntity one = this.getOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(one)) {
            log.warn("属性标签名称重复, id:{}, name:{}", model.getId(), model.getName());
        }
        Assert.isTrue(ObjectUtils.isEmpty(one), "属性标签名称不允许重复");
    }

    private void refreshTopicScenarioAttr(InsAttributeLabelEntity source, String currentName) {
        if (ObjectUtils.isEmpty(source) || StrUtil.equals(source.getName(), currentName)) {
            return;
        }

        List<InsTagLibClientEntity> affectedTopicList = this.findAffectedTopicList(source.getId(), source.getName());
        if (CollectionUtil.isEmpty(affectedTopicList)) {
            log.info("属性标签名称变更后未匹配到需同步的观点, attributeLabelId:{}, oldName:{}, newName:{}", source.getId(), source.getName(), currentName);
            return;
        }

        String userId = ServiceContextHolder.getUserId();
        LocalDateTime now = LocalDateTime.now();
        int updateCount = 0;
        for (InsTagLibClientEntity topic : affectedTopicList) {
            String scenarioAttr = StrUtil.replace(topic.getScenarioAttr(), source.getName(), currentName);
            if (StrUtil.equals(topic.getScenarioAttr(), scenarioAttr)) {
                continue;
            }
            UpdateWrapper<InsTagLibClientEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(InsTagLibClientEntity::getId, topic.getId());
            updateWrapper.lambda().set(InsTagLibClientEntity::getScenarioAttr, scenarioAttr);
            updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateUser, userId);
            updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateTime, now);
            updateCount += tagLibClientMapper.update(null, updateWrapper);
        }
        log.info("属性标签名称变更后同步话题场景属性完成, attributeLabelId:{}, oldName:{}, newName:{}, matchCount:{}, updateCount:{}",
                source.getId(), source.getName(), currentName, affectedTopicList.size(), updateCount);
    }

    private List<InsTagLibClientEntity> findAffectedTopicList(String attributeLabelId, String oldName) {
        QueryWrapper<InsTagLibClientEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(InsTagLibClientEntity::getScenarioAttr, oldName);
        queryWrapper.apply("JSON_CONTAINS(attribute_label_ids, JSON_ARRAY({0}))", attributeLabelId);
        return tagLibClientMapper.selectList(queryWrapper);
    }
}
