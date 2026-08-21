package com.voc.service.security.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.AccountException;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.model.AccountModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.config.PBEStringEncryptor;
import com.voc.service.security.api.ICredentialsChangeRecordService;
import com.voc.service.security.api.ICredentialsService;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.entity.CredentialsEntity;
import com.voc.service.security.impl.mapper.CredentialsMapper;
import com.voc.service.security.model.CredentialsModel;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AuthorizationService
 * @Description ckcui
 * @createTime 2023年11月29日 12:36
 * @Copyright futong
 */

@Service
@CacheConfig(cacheNames = "accountsCache", keyGenerator = "keyGenerator")
public class CredentialsService extends ServiceImpl<CredentialsMapper, CredentialsEntity> implements ICredentialsService {
    private static final Logger logger = LoggerFactory.getLogger(CredentialsService.class);
    public static List<CredentialsModel> loginType = new ArrayList<>();
    @Autowired
    CredentialsMapper baseMapper;
    @Autowired
    SecurityConverMapperService securityConverMapperService;
    @Autowired
    ICredentialsChangeRecordService credentialsChangeRecordService;

    @Override
    public int add(CredentialsModel credentialsModel) {
        try {
            Assert.notNull(credentialsModel.getAppId(), "app_id cannot be empty");
            Assert.notNull(credentialsModel.getIdentityType(), "identity_type cannot be empty");
            Assert.notNull(credentialsModel.getIdentifier(), "identifier cannot be empty");
            logger.info("创建用户授权信息 {}", credentialsModel);

            QueryWrapper<CredentialsEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", credentialsModel.getAppId());
            queryWrapper.eq("identifier", credentialsModel.getIdentifier());
            queryWrapper.eq("identity_type", credentialsModel.getIdentityType());
            final Long count = baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new AccountException(CommonErrorEnum.ACCOUNT_EXISTS);
            }

            //处理密码
            if (StrUtil.isNotBlank(credentialsModel.getCredential())) {
                credentialsModel.setCredential(PBEStringEncryptor.getInstance().encrypt(credentialsModel.getCredential()));
//                credentialsModel.setCredential(MD5PasswordEncoder.getInstance().encode(credentialsModel.getCredential()));
            }

            CredentialsEntity entity = securityConverMapperService.converToCredential(credentialsModel);
            //操作人
            entity.setOperator(StrUtil.isNotBlank(credentialsModel.getOperator()) ? credentialsModel.getOperator() : ServiceContextHolder.getUserId());
            logger.trace("CredentialsEntity={}", entity);
            return baseMapper.insert(entity);
        } catch (AccountException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BussinessException(e);
        }
    }

    @Override
    public Optional<CredentialsModel> find(CredentialsModel param) {
        Assert.isTrue(StrUtil.isNotBlank(param.getIdentityType()), "identity_type cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getAppId()), "app_id cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getUserId()), "uesr_id cannot be empty");

        QueryWrapper<CredentialsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CredentialsEntity::getIdentityType, param.getIdentityType());
        queryWrapper.lambda().eq(CredentialsEntity::getAppId, param.getAppId());
        queryWrapper.lambda().eq(CredentialsEntity::getUserId, param.getUserId());

        CredentialsEntity entity = baseMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNull(entity)) {
            return null;
        }
        CredentialsModel credentialEntity = securityConverMapperService.converToCredential(entity);

        return Optional.of(credentialEntity);
    }

    @Override
    public boolean changePassword(final CredentialsModel param) {
        logger.trace("credentialsModel= {}", param);
        Assert.isTrue(StrUtil.isNotBlank(param.getId()), "id cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getCredential()), "new password cannot be empty");

        UpdateWrapper<CredentialsEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(CredentialsEntity::getId, param.getId());
        wrapper.set("credential", param.getCredential());
        wrapper.set("update_time", param.getUpdateTime());
        wrapper.set("operator", StrUtil.isNotBlank(param.getOperator()) ? param.getOperator() : ServiceContextHolder.getUserId());


        return this.update(wrapper);
    }


    @Override
    public int removeTestUsers(Set<String> ids) {
        return baseMapper.removeTestUsers(ids);
    }

    @Override
    public List<AccountModel> findByUserIds(Set<String> ids, String appId) {
        List<CredentialsEntity> entityList = baseMapper.selectByUserIds(ids, appId);

        return entityList.stream().map(entity ->
                securityConverMapperService.converToAccount(entity)
        ).collect(Collectors.toList());
    }

    @Override
   /* @CacheEvict(cacheNames = "accountsCache",
            key = "T(String).valueOf(#param.getAppId()).concat(':accounts:').concat( #param.getUserId()).concat('_').concat( #param.getIdentifier())"
            ,allEntries = true)*/
    public boolean lock(final CredentialsModel model) {
        Assert.isTrue(StrUtil.isNotBlank(model.getUserId()), "user_id cannot be empty");

        UpdateWrapper<CredentialsEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(CredentialsEntity::getUserId, model.getUserId());
        wrapper.lambda().eq(CredentialsEntity::isNonLocked, true);
        wrapper.set("non_locked", false);
        wrapper.set("update_time", model.getUpdateTime());
        wrapper.set("operator", StrUtil.isNotBlank(model.getOperator()) ? model.getOperator() : ServiceContextHolder.getUserId());

        return this.update(wrapper);
    }

    @Override
    /*@CacheEvict(cacheNames = "accountsCache",
            key = "T(String).valueOf(#param.getAppId()).concat(':accounts:').concat( #param.getUserId()).concat('_').concat( #param.getIdentifier())"
            ,allEntries = true)*/
    public boolean unlock(final CredentialsModel model) {
        Assert.isTrue(StrUtil.isNotBlank(model.getUserId()), "user_id cannot be empty");
        final String userId = ServiceContextHolder.getUserId();

        UpdateWrapper<CredentialsEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(CredentialsEntity::getUserId, model.getUserId());
        wrapper.lambda().eq(CredentialsEntity::isNonLocked, false);
        wrapper.set("non_locked", true);
        wrapper.set("update_time", model.getUpdateTime());
        wrapper.set("operator", StrUtil.isNotBlank(model.getOperator()) ? model.getOperator() : ServiceContextHolder.getUserId());

        return this.update(wrapper);
    }

    @Override
    /*@CacheEvict(cacheNames = "accountsCache",
            key = "T(String).valueOf(#param.getAppId()).concat(':accounts:').concat( #param.getUserId()).concat('_').concat( #param.getIdentifier())"
            ,allEntries = true)*/
    public boolean enable(final CredentialsModel model) {
        Assert.isTrue(StrUtil.isNotBlank(model.getUserId()), "user_id cannot be empty");
        logger.info("账号启用,入参:{}", JSONObject.toJSONString(model));
        UpdateWrapper<CredentialsEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(CredentialsEntity::getUserId, model.getUserId());
        wrapper.lambda().eq(CredentialsEntity::getAppId, model.getAppId());
//        wrapper.lambda().eq(CredentialsEntity::isEnabled, false);
        wrapper.set("enabled", true);
        wrapper.set("update_time", model.getUpdateTime());
        wrapper.set("operator", ObjectUtils.isNotEmpty(model.getUserId())?model.getUserId():"-1");
        wrapper.set("expire_date", model.getExpireDate());
        return this.update(wrapper);
    }

    @Override
    /*@CacheEvict(cacheNames = "accountsCache",
            key = "T(String).valueOf(#param.getAppId()).concat(':accounts:').concat( #param.getUserId()).concat('_').concat( #param.getIdentifier())"
            ,allEntries = true)*/
    public boolean disable(final CredentialsModel model) {
        Assert.isTrue(StrUtil.isNotBlank(model.getUserId()), "user_id cannot be empty");
        logger.info("账号停用，入参:{}", JSONObject.toJSONString(model));
        final LocalDateTime updateTime = LocalDateTime.now();
        UpdateWrapper<CredentialsEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(CredentialsEntity::getUserId, model.getUserId());
        wrapper.lambda().eq(CredentialsEntity::getAppId, model.getAppId());
//        wrapper.lambda().eq(CredentialsEntity::isEnabled, true);
        wrapper.set("enabled", false);
        wrapper.set("update_time", updateTime);
        wrapper.set("operator", ObjectUtils.isNotEmpty(model.getUserId())?model.getUserId():"-1");
        wrapper.set("expire_date", model.getExpireDate());
        return this.update(wrapper);
    }

    @Override
    public List<CredentialsModel> findByUserId(String userId, String appId) {
        Assert.isTrue(StrUtil.isNotBlank(userId), "uesr_id cannot be empty");

        QueryWrapper<CredentialsEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CredentialsEntity::getUserId, userId);
        queryWrapper.lambda().eq(CredentialsEntity::getAppId, appId);

        List<CredentialsEntity> entityList = baseMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(entityList)) {
            return null;
        }

        return entityList.stream().map(entity ->
                securityConverMapperService.converToCredential(entity)
        ).collect(Collectors.toList());
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/31 上午9:34
     * @描述   客户状态变更后，变更用户下所有账号
     * @param model
     * @return boolean
     **/
    @Override
    public boolean changeAllCredentials(CredentialsModel model) {
        Assert.isTrue(StrUtil.isNotBlank(model.getUserId()), "user_id cannot be empty");
        //启用
        if(model.isEnabled()){
            //获取用户最新变更记录
            List<CredentialsModel> lastChangeRecord = credentialsChangeRecordService.findLastChangeRecordByUserId(model.getUserId());
            if(ObjectUtils.isEmpty(lastChangeRecord)){
                logger.warn("当前用户{}无变更记录,变更所有账号状态",model.getUserId());
                final LocalDateTime updateTime = LocalDateTime.now();
                UpdateWrapper<CredentialsEntity> wrapper = new UpdateWrapper<>();
                wrapper.lambda().eq(CredentialsEntity::getUserId, model.getUserId());
                wrapper.set("enabled", false);
                wrapper.set("update_time", updateTime);
                return this.update(wrapper);
            }
            List<CredentialsEntity> credentialsEntities = securityConverMapperService.credentialsModelListConverToEntityList(lastChangeRecord);
            return this.updateBatchById(credentialsEntities);
        }else {
            //停用
            QueryWrapper<CredentialsEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(CredentialsEntity::getUserId, model.getUserId());
            List<CredentialsEntity> credentialsEntities = baseMapper.selectList(queryWrapper);
            if(ObjectUtils.isEmpty(credentialsEntities)){
                logger.warn("当前用户{}暂无任何账号,不做任何处理",model.getUserId());
                return true;
            }
            List<CredentialsModel> credentialsModels = securityConverMapperService.credentialsEntityListConverToModelList(credentialsEntities);
            //保存变更记录
            boolean saveChangeRecord = credentialsChangeRecordService.saveBatchChangeRecord(credentialsModels);
            if(saveChangeRecord){
                final LocalDateTime updateTime = LocalDateTime.now();
                UpdateWrapper<CredentialsEntity> wrapper = new UpdateWrapper<>();
                wrapper.lambda().eq(CredentialsEntity::getUserId, model.getUserId());
                wrapper.set("enabled", false);
                wrapper.set("update_time", updateTime);
                return this.update(wrapper);
            }
        }
        return false;
    }

    public static void main(String[] args) {
        String encrypt = PBEStringEncryptor.getInstance().decrypt("qOcJvl3AE2+9Rk31vFZgbQ==");
        System.out.println(encrypt);
    }
}
