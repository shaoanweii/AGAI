package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsRoleEntity;
import com.voc.service.insights.engine.model.InsRoleQueryModel;
import com.voc.service.insights.engine.vo.RoleListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsRoleMapper extends BaseMapper<InsRoleEntity> {

    List<RoleListVo> queryRoleList(@Param("model") InsRoleQueryModel model);

    List<RoleListVo> allQueryRoleList(InsRoleQueryModel model);

}
