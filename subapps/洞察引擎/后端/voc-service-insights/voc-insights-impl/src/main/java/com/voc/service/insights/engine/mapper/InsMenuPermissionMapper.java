package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsMenuPermissionEntity;
import com.voc.service.insights.engine.vo.InsRolePermissionVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsMenuPermissionMapper extends BaseMapper<InsMenuPermissionEntity> {


    List<InsRolePermissionVo> getMenuPermission();


    List<InsRolePermissionVo> getUserMenuPermission(List<String> permissionIdList);

    int updateUserMenuPermission(String clientId);

    int deleteMenuPermission(String clientId);

    int updateUserButtonPermission(String clientId);

    int deleteButtonPermission(String clientId);

}
