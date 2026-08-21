package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsRoleRelationPermissionEntity;
import com.voc.service.insights.engine.model.InsRoleRelationPermissionModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsRoleRelationPermissionMapper extends BaseMapper<InsRoleRelationPermissionEntity> {


    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param model List<InsModelDescEntity> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("model") List<InsRoleRelationPermissionModel> model);
}
