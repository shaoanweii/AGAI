package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysModelResltDataAnalysisValidEntity;
import com.voc.service.analysis.model.AysValidDataModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
@Repository
public interface AysModelResltAnalysisValidMapper extends BaseMapper<AysModelResltDataAnalysisValidEntity> {

    AysModelResltDataAnalysisValidEntity validDataCondition();


    List<AysModelResltDataAnalysisValidEntity> find(AysValidDataModel validResltDataParam);

    Long selectCount_(@Param("param") AysValidDataModel param);

    long removeHistoryData(@Param("days") int days);

    Set<String> findIincompleteData();
}
