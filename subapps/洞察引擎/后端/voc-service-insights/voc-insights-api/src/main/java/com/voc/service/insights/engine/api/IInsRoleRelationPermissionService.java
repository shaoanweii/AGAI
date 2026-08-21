package com.voc.service.insights.engine.api;

import com.voc.service.insights.engine.model.InsRoleRelationPermissionModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IInsRoleRelationPermissionService {

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param model List<InsModelDescEntity> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("model") List<InsRoleRelationPermissionModel> model);
    int delete(String roleId);
    List<InsRoleRelationPermissionModel> queryList(String roleId);

}