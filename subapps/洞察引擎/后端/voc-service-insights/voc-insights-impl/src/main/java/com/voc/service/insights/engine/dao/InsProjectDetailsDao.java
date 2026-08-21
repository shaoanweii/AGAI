package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsProjectDetailsEntity;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/25 下午2:29
 * @描述:
 **/
public interface InsProjectDetailsDao {
    /**
     * @param clientId
     * @param insProjectDetailsEntity
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/25 下午3:08
     * @描述 批量保存项目详情数据
     **/
    void saveBatchProjectDetails(String clientId, List<InsProjectDetailsEntity> insProjectDetailsEntity);
    /**
     * @param clientId
     * @param insProjectDetailsEntity
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/25 下午3:15
     * @描述 批量更新项目详情数据
     **/
    void updateBatchProjectDetails(String clientId, List<InsProjectDetailsEntity> insProjectDetailsEntity);

    /**
     * @param clientId
     * @param projectId
     * @return com.voc.service.insights.engine.entity.InsProjectDetailsEntity
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/25 下午3:25
     * @描述 根据项目id查询项目详情数据
     **/
    List<InsProjectDetailsEntity> findProjectInfo(String clientId, String projectId);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/10/28 上午9:32
     * @描述  删除项目详情
     * @param clientId
     * @param projectId
     * @return void
     **/
    void deleteProjectInfo(String clientId, String projectId);
}
