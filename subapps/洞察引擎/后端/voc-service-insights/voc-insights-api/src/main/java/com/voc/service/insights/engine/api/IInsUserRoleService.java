package com.voc.service.insights.engine.api;


import com.voc.service.insights.engine.model.InsUserRoleModel;
import com.voc.service.insights.engine.vo.InsUserRoleVo;

import java.util.List;

public interface IInsUserRoleService {

    Boolean saveOrUpdate(InsUserRoleModel insUserRoleModel);
    Boolean deleteRole(InsUserRoleModel insUserRoleModel);

    List<InsUserRoleVo> getRoleInfo(InsUserRoleModel insUserRoleModel);

    String getRoleIdByUserId(String userId,String clientId);

    Integer getCountByRole(String roleId);

    List<String> getUserIdList(String roleId, String clientId);

    Boolean batchSaveOrUpdate(InsUserRoleModel models);

    Boolean deleteRoleByRoleId(InsUserRoleModel models);
}
