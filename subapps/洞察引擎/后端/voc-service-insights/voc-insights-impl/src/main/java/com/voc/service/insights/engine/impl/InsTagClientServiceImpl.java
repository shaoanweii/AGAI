package com.voc.service.insights.engine.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.IdWorker;
import com.voc.service.insights.engine.api.IInsTagClientService;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.entity.InsTagClientEntity;
import com.voc.service.insights.engine.entity.InsTagInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsTagClientMapper;
import com.voc.service.insights.engine.mapper.InsTagInfoMapper;
import com.voc.service.insights.engine.model.InsTagClientBatchModel;
import com.voc.service.insights.engine.model.InsTagClientModel;
import com.voc.service.insights.engine.model.InsTagInfoQueryModel;
import com.voc.service.insights.engine.vo.ConditionDetailsVo;
import com.voc.service.insights.engine.vo.InsTagClientVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InsTagClientServiceImpl extends ServiceImpl<InsTagClientMapper, InsTagClientEntity> implements IInsTagClientService {

    @Autowired
    private InsTagClientMapper insTagClientMapper;
    @Autowired
    InsConvertMapperService insConvertMapperService;
    @Autowired
    InsTagInfoMapper tagInfoMapper;

    @Override
    public PageInfo queryInsClientInfo(InsTagInfoQueryModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsTagClientVo> voList = insTagClientMapper.queryInsTagClientInfo(model);
        PageInfo<InsTagClientVo> page = new PageInfo<>(voList);
        page.setList(voList);
        return page;
    }

    @Override
    public Boolean insert(InsTagClientModel model) {
        //必填项校验
        this.checkParameter(model);
        if (ObjectUtils.isEmpty(model.getId())) {
            model.setId(IdWorker.getId());
        }
        InsTagClientEntity insTagClientEntity = insConvertMapperService.converTo(model);
        insTagClientEntity.setCreateTime(LocalDateTime.now());
        insTagClientEntity.setUpdateTime(LocalDateTime.now());
        return this.save(insTagClientEntity);
    }

    @Override
    public Boolean update(InsTagClientModel model) {
        //必填项校验
        this.checkParameter(model);
        InsTagClientEntity insTagClientEntity = insConvertMapperService.converTo(model);
        insTagClientEntity.setUpdateTime(LocalDateTime.now());
        return this.updateById(insTagClientEntity);
    }

    @Override
    public Boolean deleteByIds(List<Serializable> ids) {
        return this.removeByIds(ids);
    }

    @Override
    public InsTagClientVo queryVoById(Serializable id) {
        return insTagClientMapper.queryInsTagClientVoById(id);
    }

    @Override
    public Boolean insertBatch(InsTagClientBatchModel model) {
        //必填项校验
        this.checkParameter(model);

        //新增分类-默认延用标签库末级标签
        if ("Category".equals(model.getLabelType()) && model.getIsUse() == 0 && ObjectUtils.isNotEmpty(model.getTagParentVos())) {
            List<InsTagClientEntity> entityList = new ArrayList<>();
            this.tagClientCopy(model, entityList, model.getTagParentVos(), "0", "0");
            return this.saveBatch(entityList);
        }

        // 新增分类/标签
        Assert.hasLength(model.getParentId(), "所属分类不允许为空");
        List<InsTagClientModel> tagInfoList = model.getTagInfoList();
        if (tagInfoList != null && tagInfoList.size() > 0) {
            tagInfoList.stream().forEach(s -> {
                if (ObjectUtils.isNotEmpty(s.getId())) {
                    s.setTagId(s.getId());
                }
                if (ObjectUtils.isEmpty(s.getCode())) {
                    s.setCode(getTagCode(model.getType(), model.getParentId(), model.getClientId()));
                }
                s.setId(IdWorker.getId());
                s.setClientId(model.getClientId());
                s.setType(model.getType());
                s.setLabelType(model.getLabelType());
                s.setParentId(model.getParentId());
                s.setEnable(model.getEnable());
                s.setAssociationStatus(model.getAssociationStatus());
                s.setCreateTime(LocalDateTime.now());
                s.setUpdateTime(LocalDateTime.now());
            });
            return this.saveBatch(insConvertMapperService.convertTagClientEntityToList(tagInfoList));
        }

        return null;
    }

    @Override
    public List<ConditionDetailsVo> queryTagClientTree(InsTagInfoQueryModel model) {
        Assert.hasLength(model.getClientId(), "应用客户不允许为空");
        Assert.hasLength(model.getType(), "标签类型不允许为空");
        InsTagClientEntity entity = new InsTagClientEntity();
        List<InsTagClientEntity> list = this.list(new QueryWrapper<>(entity).lambda().eq(InsTagClientEntity::getClientId, model.getClientId()).eq(InsTagClientEntity::getType, model.getType()).eq(InsTagClientEntity::getEnable, 1));
        if (ObjectUtils.isEmpty(list)) {
            return null;
        }
        List<InsTagClientModel> models = insConvertMapperService.convertTagClientModelToList(list);
        //获取顶级标签
        List<InsTagClientModel> topTage = models.stream().filter(e -> "0".equalsIgnoreCase(e.getParentId())).collect(Collectors.toList());
        List<String> topId = models.stream().filter(e -> "0".equalsIgnoreCase(e.getParentId())).map(InsTagClientModel::getId).collect(Collectors.toList());
        //将全部标签放入map中，用于递归时使用，减少数据库查询
        Map<String, List<InsTagClientModel>> tageMap = models.stream().collect(Collectors.groupingBy(InsTagClientModel::getParentId));
        List<ConditionDetailsVo> tagTree = this.tagTree(topTage, tageMap);
        List<ConditionDetailsVo> collect = tagTree.stream().filter(e -> topId.contains(e.getKey())).collect(Collectors.toList());
        return collect;
    }

    private List<ConditionDetailsVo> tagTree(List<InsTagClientModel> topTage, Map<String, List<InsTagClientModel>> tageMap) {
        if (ObjectUtils.isEmpty(topTage)) {
            return null;
        }
        List<ConditionDetailsVo> tageVos = new ArrayList<>();
        for (InsTagClientModel tage : topTage) {
            ConditionDetailsVo cel = ConditionDetailsVo.builder().key(tage.getId()).value(tage.getName()).build();
            List<InsTagClientModel> tageInfoModels = tageMap.get(tage.getId());
            List<ConditionDetailsVo> conditionDetailsVos = this.tagTree(tageInfoModels, tageMap);
            cel.setChildren(conditionDetailsVos);
            tageVos.add(cel);
        }
        return tageVos;
    }

    private List<InsTagClientEntity> tagClientCopy(InsTagClientBatchModel model, List<InsTagClientEntity> entityList, List<ConditionDetailsVo> tagParentVos, String parentId, String oldParentId) {
        // 末级标签copy
        if (ObjectUtils.isEmpty(tagParentVos)) {
            if (model.getIsUse() != 0) {
                return entityList;
            }
            List<InsTagInfoEntity> list = tagInfoMapper.selectList(new QueryWrapper<>(new InsTagInfoEntity()).lambda().eq(InsTagInfoEntity::getType, model.getType()).eq(InsTagInfoEntity::getParentId, oldParentId));
            list.stream().forEach(s -> {
                String id = IdWorker.getId();
                InsTagClientEntity insTagClientEntity = new InsTagClientEntity();
                BeanUtils.copyProperties(s, insTagClientEntity);
                insTagClientEntity.setClientId(model.getClientId());
                insTagClientEntity.setId(id);
                insTagClientEntity.setParentId(parentId);
//                insTagClientEntity.setAssociationStatus(model.getAssociationStatus());
                insTagClientEntity.setTagId(s.getId());
                insTagClientEntity.setCreateTime(LocalDateTime.now());
                insTagClientEntity.setUpdateTime(LocalDateTime.now());
                entityList.add(insTagClientEntity);
            });
            return entityList;
        }
        for (ConditionDetailsVo vo : tagParentVos) {
            InsTagInfoEntity insTagInfoEntity = tagInfoMapper.selectById(vo.getKey());
            if (ObjectUtils.isEmpty(insTagInfoEntity)) {
                return entityList;
            }
            String id = IdWorker.getId();
            InsTagClientEntity insTagClientEntity = new InsTagClientEntity();
            BeanUtils.copyProperties(insTagInfoEntity, insTagClientEntity);
            InsTagClientEntity entity = insTagClientMapper.selectOne(new QueryWrapper<>(insTagClientEntity).lambda().eq(InsTagClientEntity::getClientId, model.getClientId()).eq(InsTagClientEntity::getType, model.getType()).eq(InsTagClientEntity::getCode, insTagClientEntity.getCode()));
            if (ObjectUtils.isEmpty(entity)) {
                insTagClientEntity.setClientId(model.getClientId());
                insTagClientEntity.setId(id);
                insTagClientEntity.setParentId(parentId);
//                insTagClientEntity.setAssociationStatus(model.getAssociationStatus());
                insTagClientEntity.setTagId(insTagInfoEntity.getId());
                insTagClientEntity.setCreateTime(LocalDateTime.now());
                insTagClientEntity.setUpdateTime(LocalDateTime.now());
                entityList.add(insTagClientEntity);
            } else {
                id = entity.getId();
            }

            this.tagClientCopy(model, entityList, vo.getChildren(), id, insTagInfoEntity.getId());
        }
        return entityList;
    }

    /**
     * 必填项校验
     *
     * @param model
     */
    private void checkParameter(InsTagClientModel model) {
        Assert.hasLength(model.getName(), "标签名称不允许为空");
        Assert.hasLength(model.getType(), "标签类型不允许为空");
        Assert.hasLength(model.getParentId(), "所属分类不允许为空");
        Assert.hasLength(model.getClientId(), "应用客户不允许为空");
        Assert.hasLength(model.getLabelType(), "新增类型不允许为空");

    }

    private void checkParameter(InsTagClientBatchModel model) {
        Assert.hasLength(model.getType(), "标签类型不允许为空");
        Assert.hasLength(model.getClientId(), "应用客户不允许为空");
        Assert.hasLength(model.getLabelType(), "新增类型不允许为空");
    }

    /**
     * 获取新增标签code
     *
     * @param type  标签类型
     * @param pCode 父标签code
     * @return
     */
    public String getTagCode(String type, String pCode, String clientId) {
        Assert.hasLength(type, "type类型不允许为空");
        Assert.hasLength(pCode, "父标签pCode不允许为空");
        Assert.hasLength(clientId, "应用客户ID不允许为空");
        String ROOT_PID_VALUE = "0";
        String tagCode = null;
        String pre = "GWM";
        if (InsightsConstants.QUALITY_TAG_TYPE.equalsIgnoreCase(type)) {
            pre = "Q";
        }

        /*
         * 分成三种情况
         * 1.数据库无数据
         * 2.添加子节点，无兄弟元素
         * 3.添加子节点有兄弟元素
         * */
        //找同类 确定上一个最大的code值
        LambdaQueryWrapper<InsTagClientEntity> query = new LambdaQueryWrapper<InsTagClientEntity>().eq(InsTagClientEntity::getClientId, clientId).eq(InsTagClientEntity::getParentId, pCode).eq(InsTagClientEntity::getType, type).isNotNull(InsTagClientEntity::getCode).orderByDesc(InsTagClientEntity::getCode);
//        query.eq(InsTagInfoEntity::getEnable,1);
        List<InsTagClientEntity> list = this.list(query);
        if (list == null || list.size() == 0) {
            if (ROOT_PID_VALUE.equals(pCode)) {
                //情况1
                tagCode = pre + "1001";
            } else {
                //情况2
                tagCode = pCode + "001";
            }
        } else {
            //情况3
            String oldCode = list.get(0).getCode();
            int len = InsightsConstants.QUALITY_TAG_TYPE.equalsIgnoreCase(type) ? 1 : 3;
            int newCode = Integer.parseInt(oldCode.substring(len, oldCode.length()));
            newCode += 1;
            tagCode = pre + newCode;
        }
        return tagCode;
    }

}
