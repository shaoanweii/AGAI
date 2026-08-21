package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsStaSysDepartEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface InsStaSysDepartMapper extends BaseMapper<InsStaSysDepartEntity> {

    List<InsStaSysDepartEntity> findSubDepartListByDeptId(@Param("deptId") String deptId);

    List<InsStaSysDepartEntity> findParentDepartListByDeptId(@Param("deptId") String deptId);
}
