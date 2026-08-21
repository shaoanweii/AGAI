package com.voc.service.insights.engine.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsMenuPermsInfoDao;
import com.voc.service.insights.engine.entity.InsMenuPermsInfoEntity;
import com.voc.service.insights.engine.mapper.InsMenuPermsInfoMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 17:32
 * @描述:
 **/
@Repository
public class InsMenuPermsInfoDaoImpl extends ServiceImpl<InsMenuPermsInfoMapper, InsMenuPermsInfoEntity> implements InsMenuPermsInfoDao {
    private static final Logger log = LoggerFactory.getLogger(InsMenuPermsInfoDaoImpl.class);
    @Autowired
    InsMenuPermsInfoMapper menuPermsInfoMapper;

    @Override
    public void saveBatchMenuPermsInfo(List<InsMenuPermsInfoEntity> menuPermsInfoEntities) {
        boolean saveBatch = false;
        try {
            saveBatch = this.saveBatch(menuPermsInfoEntities);
        } catch (Exception e) {
            log.error("批量保存菜单权限异常:{}", e.getMessage());
        }

        if (saveBatch) {
            log.info("批量保存菜单权限成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_MENU_PERMS_ERROR);
        }
    }

    @Override
    public List<InsMenuPermsInfoEntity> findMenuPermsInfoByUserId(String userId) {
        try {
            final List<InsMenuPermsInfoEntity> menuPermsInfoByUserId = menuPermsInfoMapper.findMenuPermsInfoByUserId(userId);
            if (ObjectUtils.isEmpty(menuPermsInfoByUserId)) {
                log.debug("当前用户:{} 暂无菜单权限");
            }
            return menuPermsInfoByUserId;
        } catch (Exception e) {
            log.error("获取菜单权限异常", e);
            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR, "获取菜单权限异常:" + e.getMessage());
        }
    }

    @Override
    public void updateBatchMenuPermsInfo(List<InsMenuPermsInfoEntity> menuPermsInfoEntities) {
        boolean saveBatch = false;
        try {
            saveBatch = this.saveOrUpdateBatch(menuPermsInfoEntities);
        } catch (Exception e) {
            log.error("批量更新菜单权限异常:{}", e.getMessage());
        }

        if (saveBatch) {
            log.info("批量更新菜单权限成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_MENU_PERMS_ERROR);
        }
    }

    @Override
    public void deleteMenuPerms(String userId) {
        try {
            menuPermsInfoMapper.deleteMenuPerms(userId);
        } catch (Exception e) {
            throw new BussinessException(InsCommonErrorEnum.REMOVE_MENU_PERMS_ERROR);
        }

    }
}
