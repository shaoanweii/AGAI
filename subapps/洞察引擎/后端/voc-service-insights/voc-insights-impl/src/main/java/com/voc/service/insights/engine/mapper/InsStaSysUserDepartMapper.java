package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.StaSysUserDepartEntity;
import com.voc.service.insights.engine.model.InsStaSysUserDepartModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface InsStaSysUserDepartMapper extends BaseMapper<StaSysUserDepartEntity> {

    List<StaSysUserDepartEntity> findStaSysUserDepartList(@Param("sysUserDepartModel") InsStaSysUserDepartModel sysUserDepartModel);

    List<String> findDownAllHierarchical(@Param("depId")String depId);

    List<StaSysUserDepartEntity> findDownAllHierarchicalList();

}
