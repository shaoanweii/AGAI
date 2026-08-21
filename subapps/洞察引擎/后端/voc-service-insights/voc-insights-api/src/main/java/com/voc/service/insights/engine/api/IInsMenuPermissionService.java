package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.model.InsMenuPermissionsModel;
import com.voc.service.insights.engine.vo.InsRolePermissionVo;

import java.util.List;

public interface IInsMenuPermissionService {

    List<InsRolePermissionVo> getMenuPermission();

    List<InsRolePermissionVo> getUserMenuPermission(List<String> permissionIdList);

    Boolean updateUserMenuPermission(InsMenuPermissionsModel model);

}