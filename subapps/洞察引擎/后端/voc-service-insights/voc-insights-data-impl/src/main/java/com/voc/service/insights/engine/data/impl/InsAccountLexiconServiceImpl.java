package com.voc.service.insights.engine.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsAccountLexiconService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.data.entity.InsAccountLexiconEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataConvertMapperService;
import com.voc.service.insights.engine.data.mapper.InsAccountLexiconMapper;
import com.voc.service.insights.engine.mapper.InsClosedRuleConditionMapper;
import com.voc.service.insights.engine.model.InsAccountLexiconModel;
import com.voc.service.insights.engine.vo.InsAccountLexiconVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/6 16:14
 * @描述:
 **/
@Service
public class InsAccountLexiconServiceImpl extends ServiceImpl<InsAccountLexiconMapper, InsAccountLexiconEntity> implements IInsAccountLexiconService {
    private static final Logger log = LoggerFactory.getLogger(InsAccountLexiconServiceImpl.class);
    @Autowired
    private InsDataConvertMapperService dataConvertMapperService;
    @Autowired
    InsClosedRuleConditionMapper closedRuleConditionMapper;

    @Override
    public void saveAccountLexiconDetails(InsAccountLexiconModel insAccountLexicon) {
//        Assert.hasLength(insAccountLexicon.getAccountName(), "账号名称不允许为空");
//        Assert.hasLength(insAccountLexicon.getAccountId(), "账号ID不允许为空");
        Assert.hasLength(insAccountLexicon.getChannel(), "渠道不允许为空");
        Assert.hasLength(insAccountLexicon.getStatus(), "状态不允许为空");
        Assert.hasLength(insAccountLexicon.getResourceId(), "资源组id不允许为空");
        final String userId = ServiceContextHolder.getUserId();
        QueryWrapper<InsAccountLexiconEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getAccountName, insAccountLexicon.getAccountName());
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getAccountId, insAccountLexicon.getAccountId());
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getChannel, insAccountLexicon.getChannel());
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getResourceId, insAccountLexicon.getResourceId());
        long count = this.count(queryWrapper);
        if(count>0){
            log.info("账号词库信息已存在，不做任何处理");
            return;
        }
        List<String> downHierarchical = this.baseMapper.findDownHierarchical(insAccountLexicon.getChannel());
        InsAccountLexiconEntity insAccountLexiconEntity = dataConvertMapperService.accountLexiconModelConvertEntity(insAccountLexicon);
        insAccountLexiconEntity.setId(IdWorker.getId());
        insAccountLexiconEntity.setCreateUser(userId);
        insAccountLexiconEntity.setCreateTime(LocalDateTime.now());
        insAccountLexiconEntity.setFinalChannel(downHierarchical);
        boolean save = this.save(insAccountLexiconEntity);
        if(save){
            log.info("账号词库信息保存成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.SAVE_ACCOUNT_LEXICON_ERROR);
        }
    }

    @Override
    public void updateAccountLexiconDetails(InsAccountLexiconModel insAccountLexicon) {
//        Assert.hasLength(insAccountLexicon.getAccountName(), "账号名称不允许为空");
//        Assert.hasLength(insAccountLexicon.getAccountId(), "账号ID不允许为空");
        Assert.hasLength(insAccountLexicon.getChannel(), "渠道不允许为空");
        Assert.hasLength(insAccountLexicon.getStatus(), "状态不允许为空");
        Assert.hasLength(insAccountLexicon.getResourceId(), "资源组id不允许为空");
        Assert.hasLength(insAccountLexicon.getId(), "id不允许为空");
        final String userId = ServiceContextHolder.getUserId();
        QueryWrapper<InsAccountLexiconEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getAccountName, insAccountLexicon.getAccountName());
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getAccountId, insAccountLexicon.getAccountId());
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getChannel, insAccountLexicon.getChannel());
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getResourceId, insAccountLexicon.getResourceId());
        queryWrapper.lambda().ne(InsAccountLexiconEntity::getId, insAccountLexicon.getId());
        long count = this.count(queryWrapper);
        if(count>0){
            log.info("账号词库信息已存在，不做任何处理");
            return;
        }

        if("0".equals(insAccountLexicon.getStatus())){
            Integer quoteCount = closedRuleConditionMapper.findQuoteCount(Arrays.asList(insAccountLexicon.getId()));
            if(quoteCount>0){
                throw new BussinessException(InsCommonErrorEnum.QUOTE_COUNT_NOT_ZERO);
            }
        }

        List<String> downHierarchical = this.baseMapper.findDownHierarchical(insAccountLexicon.getChannel());
        InsAccountLexiconEntity insAccountLexiconEntity = dataConvertMapperService.accountLexiconModelConvertEntity(insAccountLexicon);
        insAccountLexiconEntity.setUpdateUser(userId);
        insAccountLexiconEntity.setUpdateTime(LocalDateTime.now());
        insAccountLexiconEntity.setFinalChannel(downHierarchical);
        boolean update = this.updateById(insAccountLexiconEntity);
        if(update){
            log.info("账号词库信息更新成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.UPDATE_ACCOUNT_LEXICON_ERROR);
        }
    }

    @Override
    public InsAccountLexiconVo findAccountLexiconInfo(InsAccountLexiconModel insAccountLexicon) {
        Assert.hasLength(insAccountLexicon.getId(), "id不允许为空");
        QueryWrapper<InsAccountLexiconEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getId, insAccountLexicon.getId());
        final InsAccountLexiconEntity insAccountLexiconEntity = this.getOne(queryWrapper);
        if(ObjectUtils.isEmpty(insAccountLexiconEntity)){
            log.info("账号词库信息不存在");
            return null;
        }
        return dataConvertMapperService.accountLexiconEntityConvertVo(insAccountLexiconEntity);
    }

    @Override
    public IPage<InsAccountLexiconVo> findAccountLexiconList(InsAccountLexiconModel insAccountLexicon) {
        IPage<InsAccountLexiconEntity> page = new Page<>(insAccountLexicon.getPageNum(), insAccountLexicon.getPageSize());
        IPage<InsAccountLexiconEntity> accountLexiconList = this.baseMapper.findAccountLexiconList(page, insAccountLexicon);
        IPage<InsAccountLexiconVo> pages = new Page<>();
        pages.setCurrent(insAccountLexicon.getPageNum());
        pages.setSize(insAccountLexicon.getPageSize());
        pages.setTotal(accountLexiconList.getTotal());
        if(ObjectUtils.isEmpty(accountLexiconList.getRecords())){
            log.info("暂无账号词库信息");
            return pages;
        }
        final List<InsAccountLexiconEntity> records = accountLexiconList.getRecords();
        final List<InsAccountLexiconVo> accountLexiconVos = dataConvertMapperService.accountLexiconEntityListConvertVoList(records);
        pages.setRecords(accountLexiconVos);
        return pages;
    }

    @Override
    public void changeAccountLexiconStatus(InsAccountLexiconModel insAccountLexicon) {
        Assert.hasLength(insAccountLexicon.getResourceId(), "资源组id不允许为空");
        Assert.hasLength(insAccountLexicon.getStatus(), "状态不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(insAccountLexicon.getIds()), "id不允许为空");
        if("0".equals(insAccountLexicon.getStatus())){
            Integer quoteCount = closedRuleConditionMapper.findQuoteCount(insAccountLexicon.getIds());
            if(quoteCount>0){
                throw new BussinessException(InsCommonErrorEnum.QUOTE_COUNT_NOT_ZERO);
            }
        }

        UpdateWrapper<InsAccountLexiconEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(InsAccountLexiconEntity::getResourceId, insAccountLexicon.getResourceId());
        updateWrapper.lambda().in(InsAccountLexiconEntity::getId, insAccountLexicon.getIds());
        updateWrapper.lambda().set(InsAccountLexiconEntity::getStatus, insAccountLexicon.getStatus());
        boolean update = this.update(updateWrapper);
        if(update){
            log.info("账号词库信息更新成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.UPDATE_ACCOUNT_LEXICON_ERROR);
        }
    }

    @Override
    public Map<String, Integer> countByResourceIds(Set<String> ids) {
        if(ObjectUtils.isEmpty(ids)){
            return Map.of();
        }
        final List<InsAccountLexiconEntity> insAccountLexiconEntities = this.baseMapper.countByResourceIds(ids);
        return insAccountLexiconEntities.stream().collect(Collectors.toMap(InsAccountLexiconEntity::getResourceId, InsAccountLexiconEntity::getCnt));
    }

    @Override
    public List<InsAccountLexiconVo> findAllAccountLexiconList() {
        QueryWrapper<InsAccountLexiconEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsAccountLexiconEntity::getStatus, "Enabled");
        final List<InsAccountLexiconEntity> insAccountLexiconEntities = this.list(queryWrapper);
        if(ObjectUtils.isEmpty(insAccountLexiconEntities)){
            log.info("暂无账号词库信息");
            return List.of();
        }
        return dataConvertMapperService.accountLexiconEntityListConvertVoList(insAccountLexiconEntities);
    }
}
