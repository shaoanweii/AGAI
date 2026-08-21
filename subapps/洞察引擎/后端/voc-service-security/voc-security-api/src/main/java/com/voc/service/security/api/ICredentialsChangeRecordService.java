package com.voc.service.security.api;

import com.voc.service.security.model.CredentialsModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/31 上午10:19
 * @描述:
 **/
public interface ICredentialsChangeRecordService {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/31 上午10:23
     * @描述   批量新增账号变更记录
     * @param credentialsModels
     * @return boolean
     **/
    boolean saveBatchChangeRecord(List<CredentialsModel> credentialsModels);
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/31 上午10:40
     * @描述   根据用户id查询账号最新的变更记录
     * @param userId
     * @param userId
     * @return java.util.List<com.voc.service.security.model.CredentialsModel>
     **/
    List<CredentialsModel> findLastChangeRecordByUserId(String userId);
}
