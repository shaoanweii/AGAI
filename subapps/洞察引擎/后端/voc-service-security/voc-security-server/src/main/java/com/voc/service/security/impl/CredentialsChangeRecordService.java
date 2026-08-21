package com.voc.service.security.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.security.api.ICredentialsChangeRecordService;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.entity.CredentialsChangeRecordEntity;
import com.voc.service.security.impl.mapper.CredentialsChangeRecordMapper;
import com.voc.service.security.model.CredentialsModel;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/31 上午10:23
 * @描述:
 **/
@Service
public class CredentialsChangeRecordService extends ServiceImpl<CredentialsChangeRecordMapper, CredentialsChangeRecordEntity> implements ICredentialsChangeRecordService {
    @Autowired
    SecurityConverMapperService securityConverMapperService;

    @Override
    public boolean saveBatchChangeRecord(List<CredentialsModel> credentialsModels) {
        Assert.isTrue(ObjectUtils.isNotEmpty(credentialsModels), "credentialsModels cannot be empty ");
        List<CredentialsChangeRecordEntity> credentialsChangeRecordEntities = securityConverMapperService.credentialsModelListConverToChangeRecordList(credentialsModels);
        final LocalDateTime now = LocalDateTime.now();
        credentialsChangeRecordEntities.stream().forEach(e->e.setChangeTime(now));
        return this.saveOrUpdateBatch(credentialsChangeRecordEntities);
    }

    @Override
    public List<CredentialsModel> findLastChangeRecordByUserId(String userId) {
        Assert.hasLength(userId, "userId cannot be empty ");
        QueryWrapper<CredentialsChangeRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CredentialsChangeRecordEntity::getUserId, userId);
        List<CredentialsChangeRecordEntity> credentialsChangeRecordEntities = this.baseMapper.selectList(queryWrapper);
        List<CredentialsModel> credentialsModels = securityConverMapperService.changeRecordListConverToModelList(credentialsChangeRecordEntities);
        return credentialsModels;
    }
}
