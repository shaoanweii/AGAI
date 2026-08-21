package com.voc.service.security.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.security.api.IUserChangeRecordService;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.entity.UserChangeRecordEntity;
import com.voc.service.security.impl.mapper.UserChangeRecordMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/31 上午11:29
 * @描述:
 **/
@Service
public class UserChangeRecordService extends ServiceImpl<UserChangeRecordMapper, UserChangeRecordEntity> implements IUserChangeRecordService {
    @Autowired
    SecurityConverMapperService securityConverMapperService;

    @Override
    public Boolean saveBatchChangeRecord(List<UserModel> userModels) {
        Assert.isTrue(ObjectUtils.isNotEmpty(userModels), "userModels cannot be empty ");
        final LocalDateTime updateTime = LocalDateTime.now();
        //操作人
        final String userId = ServiceContextHolder.getUserId();

        List<UserChangeRecordEntity> changeRecordEntities = userModels.stream().map(e -> {
            UserChangeRecordEntity userEntity = securityConverMapperService.userModelConverToChangeRecordEntity(e);
            userEntity.setChangeTime(updateTime);
            return userEntity;
        }).collect(Collectors.toList());

        return this.saveOrUpdateBatch(changeRecordEntities);
    }

    @Override
    public List<UserModel> findLastUserChangeRecordByClientId(String clientId) {
        Assert.hasLength(clientId, "clientId cannot be empty ");
        QueryWrapper<UserChangeRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserChangeRecordEntity::getClientId, clientId);
        List<UserChangeRecordEntity> userChangeRecordEntities = this.baseMapper.selectList(queryWrapper);
        List<UserModel> userModels = userChangeRecordEntities.stream().map(e -> {
            return securityConverMapperService.userChangeRecordEntityConvertToModel(e);
        }).collect(Collectors.toList());
        return userModels;
    }
}
