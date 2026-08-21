package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsCustomerInfoEntity;
import com.voc.service.insights.engine.model.InsCustomerInfoModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 10:21
 * @描述:
 **/
public interface InsCustomerInfoDao {
    void saveCustomerInfo(InsCustomerInfoEntity customerInfo);

    void updateCustomerInfo(InsCustomerInfoEntity customerInfo);

    void deleteCustomerInfo(String customerId);

    InsCustomerInfoEntity findCustomerInfo(String customerId);

    List<InsCustomerInfoEntity> findAllCustomerList();

    List<InsCustomerInfoEntity> findCustomerListByCondition(InsCustomerInfoModel customerInfoModel);

    Boolean checkCustomerCode(InsCustomerInfoModel customerInfoModel);

    Integer getAccountNumber(String appId, String clientId);
}
