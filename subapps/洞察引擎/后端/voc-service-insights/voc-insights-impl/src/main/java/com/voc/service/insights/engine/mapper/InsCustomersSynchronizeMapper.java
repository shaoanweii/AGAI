package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsUserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface InsCustomersSynchronizeMapper extends BaseMapper<InsUserRoleEntity> {


    int saveRole();

    int saveRoleRelationPermission(@Param("clientId") String clientId);

    int saveSysUser(@Param("clientId") String clientId);

    int saveSysCredentials(@Param("clientId") String clientId, @Param("pwd") String pwd);

    int saveUserRole(@Param("clientId") String clientId);

    int saveBatchButtonPermission(@Param("clientId") String clientId);

    int saveBatchMenuPermission(@Param("clientId") String clientId);

}
