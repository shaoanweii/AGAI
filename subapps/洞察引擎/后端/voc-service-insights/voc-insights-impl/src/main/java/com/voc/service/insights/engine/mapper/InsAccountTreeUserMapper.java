package com.voc.service.insights.engine.mapper;

import com.voc.service.common.model.UserModel;
import com.voc.service.insights.engine.vo.InsDepartAccountRelationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Mapper
@Repository
public interface InsAccountTreeUserMapper {

    List<InsDepartAccountRelationVo> findDepartAccountRelationList(@Param("userModel") UserModel userModel,
                                                                  @Param("deptCodes") Collection<String> deptCodes);
}
