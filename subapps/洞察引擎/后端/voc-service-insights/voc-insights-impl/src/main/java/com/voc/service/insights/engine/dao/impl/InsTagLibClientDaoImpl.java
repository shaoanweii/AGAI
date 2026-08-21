package com.voc.service.insights.engine.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsTagLibClientDao;
import com.voc.service.insights.engine.entity.InsTagLibClientEntity;
import com.voc.service.insights.engine.enums.TagAttribute;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsTagLibClientMapper;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.model.InsTopicModel;
import com.voc.service.insights.engine.vo.TagClientVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import com.voc.service.insights.engine.vo.TagLibTopicVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/5/24 上午9:51
 * @描述:
 **/
@Repository
public class InsTagLibClientDaoImpl extends ServiceImpl<InsTagLibClientMapper, InsTagLibClientEntity> implements InsTagLibClientDao {
    private static final Logger log = LoggerFactory.getLogger(InsTagLibClientDaoImpl.class);
    @Autowired
    private InsTagLibClientMapper tagLibClientMapper;
    @Autowired
    InsConvertMapperService convertMapperService;

    @Override
    public Boolean checkTagLibName(String tagLibClientName, String tagLibClientId, String tagType,String identifier) {
        Assert.hasLength(tagLibClientName, "标签名称不能为空");
        Assert.hasLength(tagType, "标签分类不能为空");
        InsTagLibClientEntity insTagLibClientEntity = tagLibClientMapper.checkTagLibName(tagLibClientName, tagType,identifier);
        if (ObjectUtils.isNotEmpty(insTagLibClientEntity)) {
            if (ObjectUtils.isNotEmpty(tagLibClientId) && tagLibClientId.equals(insTagLibClientEntity.getId())) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void saveTagLibClient(InsTagLibClientEntity tagLibClientEntity) {
        int insert = tagLibClientMapper.insert(tagLibClientEntity);
        if (insert > 0) {
            log.info("保存客户标签信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public void updateTagLibClient(InsTagLibClientEntity tagLibClientEntity) {
        int update = tagLibClientMapper.updateById(tagLibClientEntity);
        if (update > 0) {
            log.info("更新客户标签信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public void deleteTagLibClient(String tagLibClientId) {
        int delete = tagLibClientMapper.deleteById(tagLibClientId);
        if (delete > 0) {
            log.info("删除客户标签信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.DELETE_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public List<InsTagLibClientEntity> findTagLibByQueryWrapper(String type, String pid) {
        LambdaQueryWrapper<InsTagLibClientEntity> query = new LambdaQueryWrapper<InsTagLibClientEntity>().eq(InsTagLibClientEntity::getTagParentId, pid).eq(InsTagLibClientEntity::getTagType, type).isNotNull(InsTagLibClientEntity::getTagCode).orderByDesc(InsTagLibClientEntity::getTagCode);
        return this.list(query);
    }

    @Override
    public List<InsTagLibClientEntity> findTagLibChildNodeByParentId(String pid) {
        LambdaQueryWrapper<InsTagLibClientEntity> queryWrapper = new LambdaQueryWrapper<InsTagLibClientEntity>().eq(InsTagLibClientEntity::getId, pid).isNotNull(InsTagLibClientEntity::getTagCode);
        return this.list(queryWrapper);
    }

    @Override
    public InsTagLibClientEntity findTagLibClientById(String id) {
        Assert.hasLength(id, "标签id不能为空");
        return tagLibClientMapper.findTagLibClientById(id);
    }

    @Override
    public List<InsTagLibClientEntity> findTagLibClientList(InsTagLibClientModel tagLibClientModel) {
        return tagLibClientMapper.findTagLibClientList(tagLibClientModel);
    }

    @Override
    public List<InsTagLibClientEntity> findCategoryList(InsTagLibClientModel tagLibClientModel) {
        LambdaQueryWrapper<InsTagLibClientEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InsTagLibClientEntity::getIdentifier, TagAttribute.CATEGORY.getCode());
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getTagType())) {
            queryWrapper.eq(InsTagLibClientEntity::getTagType, tagLibClientModel.getTagType());
        }
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getTagTypeList())) {
            queryWrapper.in(InsTagLibClientEntity::getTagType, tagLibClientModel.getTagTypeList());
        }
        queryWrapper.orderByAsc(InsTagLibClientEntity::getTagType)
                .orderByAsc(InsTagLibClientEntity::getLevel)
                .orderByAsc(InsTagLibClientEntity::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    public IPage<InsTagLibClientEntity> findExperienceCodeList(IPage<InsTagLibClientEntity> page, InsTagLibClientModel tagLibClientModel) {
        return tagLibClientMapper.findExperienceCodeList(page, tagLibClientModel);
    }

    @Override
    public Long countExperienceCodeList(InsTagLibClientModel tagLibClientModel) {
        return tagLibClientMapper.countExperienceCodeList(tagLibClientModel);
    }



    @Override
    public List<InsTagLibClientEntity> findFinalTagLibClientBaseList(InsTagLibClientModel tagLibClientModel) {
        LambdaQueryWrapper<InsTagLibClientEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(InsTagLibClientEntity::getId,
                InsTagLibClientEntity::getTagName,
                InsTagLibClientEntity::getTagCode,
                InsTagLibClientEntity::getTagStatus,
                InsTagLibClientEntity::getEmotion,
                InsTagLibClientEntity::getIntention);
        queryWrapper.eq(InsTagLibClientEntity::getTagAttribute, TagAttribute.FINAL_LABEL.getCode());
        queryWrapper.eq(InsTagLibClientEntity::getTagType, tagLibClientModel.getTagType());
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getIds())) {
            queryWrapper.in(InsTagLibClientEntity::getId, tagLibClientModel.getIds());
        }
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getTagStatusList())) {
            queryWrapper.in(InsTagLibClientEntity::getTagStatus, tagLibClientModel.getTagStatusList());
        }
        if (ObjectUtils.isNotEmpty(tagLibClientModel.getTagName())) {
            queryWrapper.like(InsTagLibClientEntity::getTagName, tagLibClientModel.getTagName());
        }
        queryWrapper.orderByAsc(InsTagLibClientEntity::getCreateTime, InsTagLibClientEntity::getId);
        return this.list(queryWrapper);
    }


    @Override
    public String findTagLibClientNameHierarchical(String id) {
        return tagLibClientMapper.findTagLibClientNameHierarchical(id);
    }

    @Override
    public List<InsTagLibClientEntity> findTagLibClientHierarchical(List<String> ids) {
        return tagLibClientMapper.findTagLibClientHierarchical(ids);
    }

    @Override
    @SwitchClientDS
    public List<InsTagLibClientEntity> findTagLibClientHierarchicalByCodes(List<String> codes, String clientId) {
        return tagLibClientMapper.findTagLibClientHierarchicalByCodes(codes);
    }

    @Override
    public void saveBatchTagLibClient(List<InsTagLibClientEntity> tagLibClientEntityList, String clientId) {
        boolean saveBatch = this.saveOrUpdateBatch(tagLibClientEntityList);
        if (saveBatch) {
            log.info("批量保存客户标签信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_BATCH_TAGLIB_CLIENT_ERROR);
        }

    }

    @Override
    public List<String> findCalledTagLibClient(InsTagLibClientModel tagLibClientModel) {
        return tagLibClientMapper.findCalledTagLibClient(tagLibClientModel);
    }

    @Override
    @SwitchClientDS
    public InsTagLibClientEntity findTagLibClientByName(String name, String tagParentId, String clientId) {
        Assert.hasLength(name, "标签名称不能为空");
        Assert.hasLength(tagParentId, "标签分类不能为空");
        return tagLibClientMapper.checkTagLibName(name, tagParentId, "");
    }

    @Override
    public List<InsTagLibClientEntity> findDownTagLibHierarchical(List<String> tagParentIds, List<String> tagStatusList, String tagName, List<String> ids, List<String> tagCodes,String tagType) {
        return tagLibClientMapper.findDownTagLibHierarchical(tagParentIds,tagStatusList,tagName, tagCodes,tagType);
    }

    @Override
    public List<InsTagLibClientEntity> findDownAllTagLibHierarchical(List<String> tagParentIds) {
        return tagLibClientMapper.findDownAllTagLibHierarchical(tagParentIds);
    }

    @Override
    public List<InsTagLibClientEntity> findUpTagLibHierarchical(List<String> tagParentIds, List<String> tagStatusList, String tagName, List<String> ids) {
        return List.of();
    }


    @Override
    public void deleteBatchTagLibClient(List<String> ids) {
        int del = tagLibClientMapper.deleteBatchIds(ids);
        if(del>0){
            log.info("批量删除标签成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.DELETE_BATCH_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public void batchMoveTagLibClient(List<String> ids, String tagParentId, Integer level) {
        final String userId = ServiceContextHolder.getUserId();
        UpdateWrapper<InsTagLibClientEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsTagLibClientEntity::getId, ids);
        updateWrapper.lambda().set(InsTagLibClientEntity::getTagParentId, tagParentId);
        updateWrapper.lambda().set(InsTagLibClientEntity::getLevel, level);
        updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateUser,userId);
        updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateTime, LocalDateTime.now());
        boolean update = this.update(updateWrapper);
        if (update) {
            log.info("批量移动标签成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.BATCH_MOVE_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public void batchUpdateStatusTagLibClient(List<String> ids, String tagStatus) {
        final String userId = ServiceContextHolder.getUserId();
        UpdateWrapper<InsTagLibClientEntity> queryWrapper = new UpdateWrapper<>();
        queryWrapper.lambda().in(InsTagLibClientEntity::getId, ids);
        queryWrapper.lambda().set(InsTagLibClientEntity::getTagStatus, tagStatus);
        queryWrapper.lambda().set(InsTagLibClientEntity::getUpdateUser,userId);
        queryWrapper.lambda().set(InsTagLibClientEntity::getUpdateTime, LocalDateTime.now());
        boolean update = this.update(queryWrapper);
        if (update) {
            log.info("批量更新标签状态成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.BATCH_UPDATE_STATUS_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public List<TagLibCategoryVo> findAllFinalTagLib(InsTagLibClientModel tagLibClientModel) {
        QueryWrapper<InsTagLibClientEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsTagLibClientEntity::getTagType, TagAttribute.FINAL_LABEL.getCode());
        List<InsTagLibClientEntity> insTagLibClientEntities = this.baseMapper.selectList(queryWrapper);
        List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(insTagLibClientEntities);
        return tagLibCategoryVos;
    }

    @Override
    public List<String> findTaglibCodeByName(List<String> codes,String tagAttribute) {
        return tagLibClientMapper.findTaglibCodeByName(codes,tagAttribute);
    }

    @Override
    public List<TagLibCategoryVo> findTagLib(List<Integer> level, List<String> tagType, String tagAttribute, List<String> tagStatusList) {
        QueryWrapper<InsTagLibClientEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(InsTagLibClientEntity::getLevel, level);
        if(ObjectUtils.isNotEmpty(tagStatusList)){
            queryWrapper.lambda().in(InsTagLibClientEntity::getTagStatus, tagStatusList);
        }else{
            queryWrapper.lambda().eq(InsTagLibClientEntity::getTagStatus, "1");
        }

        if(ObjectUtils.isNotEmpty(tagType)){
            queryWrapper.lambda().in(InsTagLibClientEntity::getTagType, tagType);
        }
        if(ObjectUtils.isNotEmpty(tagAttribute)){
            queryWrapper.lambda().eq(InsTagLibClientEntity::getTagAttribute, tagAttribute);
        }
        final List<InsTagLibClientEntity> insTagLibClientEntities = this.baseMapper.selectList(queryWrapper);
        if(ObjectUtils.isEmpty(insTagLibClientEntities)){
            return List.of();
        }
        final List<TagLibCategoryVo> tagLibCategoryVos = convertMapperService.tagLibClientEntityListConvertCategoryVoList(insTagLibClientEntities);
        return tagLibCategoryVos;
    }

    @Override
    public List<TagClientVo> findAllUpTagLibHierarchicalByTagId(InsTagLibClientModel tagLibClientModel) {
        return tagLibClientMapper.findAllUpTagLibHierarchicalByTagId(tagLibClientModel);
    }

    @Override
    public List<TagClientVo> findAllUpTagLibHierarchicalByTopicCode(Set<String> codes) {
        Assert.isTrue(ObjectUtils.isNotEmpty( codes), "标签编码不能为空");
        return tagLibClientMapper.findAllUpTagLibHierarchicalByTopicCode(codes);
    }

    @Override
    public IPage<InsTagLibClientEntity> findlAllTopic(IPage<InsTagLibClientEntity> page, InsTopicModel tagLibClientModel) {
        return tagLibClientMapper.findlAllTopic(page,tagLibClientModel);
    }

    @Override
    public List<TagLibTopicVo> findTopicList(InsTopicModel tagLibClientModel) {
        return tagLibClientMapper.findTopicList(tagLibClientModel);
    }

    @Override
    public void batchChangeTopicStatus(InsTopicModel tagLibClientModel) {
        final String username = ServiceContextHolder.getUser().getFirstname();
        UpdateWrapper<InsTagLibClientEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(InsTagLibClientEntity::getTagStatus, tagLibClientModel.getTagStatus());
        updateWrapper.lambda().in(InsTagLibClientEntity::getTagCode, tagLibClientModel.getTopicCodes());
        updateWrapper.lambda().eq(InsTagLibClientEntity::getTagAttribute, TagAttribute.FINAL_LABEL.getCode());
        updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateTime, LocalDateTime.now());
        updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateUser, username);
        boolean update = this.update(updateWrapper);
        if (update) {
            log.info("批量更新标签状态成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.BATCH_UPDATE_STATUS_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public void batchUpdateTopic(InsTopicModel insTopicModel, String scenarioAttr, List<String> attributeLabelIds) {
        final String username = ServiceContextHolder.getUser().getFirstname();
        UpdateWrapper<InsTagLibClientEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsTagLibClientEntity::getTagCode, insTopicModel.getTopicCodes());
        updateWrapper.lambda().eq(InsTagLibClientEntity::getTagAttribute, TagAttribute.FINAL_LABEL.getCode());
        updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateTime, LocalDateTime.now());
        updateWrapper.lambda().set(InsTagLibClientEntity::getUpdateUser, username);
        updateWrapper.lambda().set(InsTagLibClientEntity::getSynonyms, insTopicModel.getSynonyms());

        //更新智慧交互中心编码
        if (ObjectUtils.isNotEmpty(insTopicModel.getMappingCode())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getMappingCode, insTopicModel.getMappingCode());
        }
        //更新 情感
        if (ObjectUtils.isNotEmpty(insTopicModel.getEmotion())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getEmotion, insTopicModel.getEmotion());
        }
        //更新 意图
        if (ObjectUtils.isNotEmpty(insTopicModel.getIntention())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getIntention, insTopicModel.getIntention());
        }
        //更新 客户问题分级
        if (ObjectUtils.isNotEmpty(insTopicModel.getTagCustomerIssueClassification())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getTagCustomerIssueClassification, insTopicModel.getTagCustomerIssueClassification());
        }
        //更新 问题程度
        if (ObjectUtils.isNotEmpty(insTopicModel.getTagIssueSeverity())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getTagIssueSeverity, insTopicModel.getTagIssueSeverity());
        }
        //更新 事件清晰度
        if (ObjectUtils.isNotEmpty(insTopicModel.getEventClarity())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getEventClarity, insTopicModel.getEventClarity());
        }
        //更新 敏感类型
        if (ObjectUtils.isNotEmpty(insTopicModel.getSusceptiveType())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getSusceptiveType, insTopicModel.getSusceptiveType());
        }
        //更新 代码的精准性
        if (ObjectUtils.isNotEmpty(insTopicModel.getTagAccuracy())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getTagAccuracy, insTopicModel.getTagAccuracy());
        }
        //更新 业务领域
        if (ObjectUtils.isNotEmpty(insTopicModel.getTagBusinessDomain())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getTagBusinessDomain, insTopicModel.getTagBusinessDomain());
        }
        //更新 是否需回复
        if (ObjectUtils.isNotEmpty(insTopicModel.getTagComplaintFlagNeedingReply())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getTagComplaintFlagNeedingReply, insTopicModel.getTagComplaintFlagNeedingReply());
        }
        //更新 是否需闭环
        if (ObjectUtils.isNotEmpty(insTopicModel.getTagNeedForvclosedLoop())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getTagNeedForvclosedLoop, insTopicModel.getTagNeedForvclosedLoop());
        }
        //更新 主责部门
        if (ObjectUtils.isNotEmpty(insTopicModel.getD2cResponsibleDept())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getD2cResponsibleDept, insTopicModel.getD2cResponsibleDept());
        }
        //更新 状态
        if (ObjectUtils.isNotEmpty(insTopicModel.getTagStatus())) {
            updateWrapper.lambda().set(InsTagLibClientEntity::getTagStatus, insTopicModel.getTagStatus());
        }
        //更新 属性标签
        if(ObjectUtils.isNotEmpty(attributeLabelIds)){
            updateWrapper.lambda().set(InsTagLibClientEntity::getScenarioAttr, scenarioAttr);
            updateWrapper.lambda().set(InsTagLibClientEntity::getAttributeLabelIds, attributeLabelIds);
        }
        boolean update = this.update(updateWrapper);
        if (update) {
            log.info("批量更新标签成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.BATCH_UPDATE_TAGLIB_CLIENT_ERROR);
        }
    }

    @Override
    public List<InsTagLibClientEntity> findTopic(InsTopicModel tagLibClientModel) {
        QueryWrapper<InsTagLibClientEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsTagLibClientEntity::getTagAttribute, TagAttribute.FINAL_LABEL.getCode());
        if(ObjectUtils.isNotEmpty(tagLibClientModel.getTagName())){
            queryWrapper.lambda().eq(InsTagLibClientEntity::getTagName, tagLibClientModel.getTagName());
        }
        if(ObjectUtils.isNotEmpty(tagLibClientModel.getTopicCodes())){
            queryWrapper.lambda().in(InsTagLibClientEntity::getTagCode, tagLibClientModel.getTopicCodes());
        }
        if(ObjectUtils.isNotEmpty(tagLibClientModel.getTagCode())){
            queryWrapper.lambda().eq(InsTagLibClientEntity::getTagCode, tagLibClientModel.getTagCode());
        }
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public Boolean findTopicCount(String topicName) {
        Assert.hasLength(topicName, "观点名称不能为空");
        QueryWrapper<InsTagLibClientEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsTagLibClientEntity::getTagName, topicName);
        queryWrapper.lambda().eq(InsTagLibClientEntity::getTagAttribute, TagAttribute.FINAL_LABEL.getCode());
        return this.baseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public String findMaxCode() {
        return this.baseMapper.findMaxCode();
    }

    @Override
    @SwitchClientDS
    public void updateBatch(List<InsTagLibClientEntity> entities, String clientId) {
        boolean update = this.updateBatchById(entities);
        if (update) {
            log.info("更新成功");
        }else{
            throw new RuntimeException("更新失败");
        }
    }
}
