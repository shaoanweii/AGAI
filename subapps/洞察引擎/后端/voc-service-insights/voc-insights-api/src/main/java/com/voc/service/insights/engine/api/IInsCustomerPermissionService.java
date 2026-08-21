package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.vo.RoleAuthTree;

import java.util.List;

public interface IInsCustomerPermissionService {

    Boolean saveOrUpdate(String clientId, List<String> permissionIdList);

    List<RoleAuthTree> queryCustomerPermissionList(String clientId);
}
