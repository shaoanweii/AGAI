package com.voc.service.security.api;

import com.voc.service.common.model.UserModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/31 上午11:28
 * @描述:
 **/
public interface IUserChangeRecordService {

    Boolean saveBatchChangeRecord(List<UserModel> userModels);

    List<UserModel> findLastUserChangeRecordByClientId(String clientId);
}
