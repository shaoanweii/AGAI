package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsMenuPermsInfoEntity;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 17:32
 * @描述:
 **/
public interface InsMenuPermsInfoDao {
    /**
     * @param menuPermsInfoEntities
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/4 17:35
     * @描述 批量保存菜单权限
     **/
    void saveBatchMenuPermsInfo(List<InsMenuPermsInfoEntity> menuPermsInfoEntities);

    /**
     * @param userId
     * @return java.util.List<com.voc.service.insights.engine.entity.InsMenuPermsInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 09:27
     * @描述 根据用户id获取菜单权限
     **/
    List<InsMenuPermsInfoEntity> findMenuPermsInfoByUserId(String userId);

    /**
     * @param menuPermsInfoEntities
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 10:16
     * @描述 批量更新菜单权限
     **/
    void updateBatchMenuPermsInfo(List<InsMenuPermsInfoEntity> menuPermsInfoEntities);

    /**
     * @param userId
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/3/5 17:08
     * @描述 根据用户id删除菜单权限
     **/
    void deleteMenuPerms(String userId);
}
