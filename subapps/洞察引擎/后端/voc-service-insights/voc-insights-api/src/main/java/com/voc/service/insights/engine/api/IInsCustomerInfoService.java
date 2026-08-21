package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.model.InsCustomerInfoModel;
import com.voc.service.insights.engine.vo.CustomerInfoVo;
import com.voc.service.insights.engine.vo.RoleAuthTree;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 10:18
 * @描述:
 **/
public interface IInsCustomerInfoService {
    void saveCustomerInfo(InsCustomerInfoModel customerInfoModel);

    void updateCustomerInfo(InsCustomerInfoModel customerInfoModel);

    void deleteCustomerInfo(InsCustomerInfoModel customerInfoModel);

    CustomerInfoVo findCustomerInfo(InsCustomerInfoModel customerInfoModel);

    PageInfo findCustomerList(InsCustomerInfoModel customerInfoModel);

    List<CustomerInfoVo> findAllCustomerInfo();

    Boolean checkCustomerCode(InsCustomerInfoModel customerInfoModel);


    String queryCodeById(String clientId);

    List<RoleAuthTree> queryCustomerPermissionList(InsCustomerInfoModel customerInfoModel);


    List<CustomerInfoVo> findCustomerListModel();
}
