package com.voc.service.insights.engine.api;


import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.RoleAuthTree;
import com.voc.service.insights.engine.vo.RoleAuthVo;
import com.voc.service.insights.engine.vo.UserRoleInfoVo;

import java.util.List;

public interface IInsRoleService {


    PageInfo queryRoleList(InsRoleQueryModel model);

    List<RoleAuthTree> queryMenuPermissionList(InsRoleQueryModel model);

    Result<?> saveOrUpdateRole(RoleAuthModel model);

    Result<?> queryRoleInfo(RoleInfoQueryModel model);

    Result<?> queryRoleALlList(InsRoleQueryModel model);

    PageInfo getUserRoleList(InsRoleQueryModel model);


    UserRoleInfoVo queryUserPermission(UserRoleQueryModel model);

    Result<?> deleteRole(RoleInfoQueryModel model);

    RoleAuthVo getRoleInfo(RoleInfoQueryModel model);
}
