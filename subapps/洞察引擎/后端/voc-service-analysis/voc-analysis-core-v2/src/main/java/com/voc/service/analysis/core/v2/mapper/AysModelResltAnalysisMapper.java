package com.voc.service.analysis.core.v2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.analysis.core.v2.entity.AysModelResultDataAnalysisEntity;
import com.voc.service.analysis.model.AysValidDataModel;
import com.voc.service.analysis.model.ResultConditionsParamModel;
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
public interface AysModelResltAnalysisMapper extends BaseMapper<AysModelResultDataAnalysisEntity> {

    AysModelResultDataAnalysisEntity validDataCondition();

    Long moveBatch(AysValidDataModel validResltDataParam);

    List<AysModelResultDataAnalysisEntity> find(AysValidDataModel validResltDataParam);

    List<AysModelResultDataAnalysisEntity> findByWorkId(@Param("conditionsParamModel") ResultConditionsParamModel conditionsParamModel);

    long dataCount(AysValidDataModel param);

    long removeHistoryData(@Param("days") int days);

    Set<String> findIincompleteData();
}
