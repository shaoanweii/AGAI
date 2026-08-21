package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.model.ProjectRawDataParamModel;
import com.voc.service.insights.engine.api.model.RawDataParamModel;
import com.voc.service.insights.engine.data.entity.AysMetaDataAnalysisEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.executor.result.DefaultMapResultHandler;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;
import java.util.Map;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysMetaDataAnalysisMapper extends BaseMapper<AysMetaDataAnalysisEntity> {

    @SwitchClientDS(datasource ="starrock_dndc")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(Map.class)
//    void pageMetaDataAnalysisList(RawDataParamModel paramModel, DefaultMapResultHandler handler);
    List<Map<String, Object>> pageMetaDataAnalysisList(RawDataParamModel paramModel );
    @SwitchClientDS(datasource ="starrock_dndc")
    long pageMetaDataAnalysisListCount(RawDataParamModel paramModel);



    @SwitchClientDS(datasource ="starrock_dndc")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(Map.class)
//    void pageMetaDataAnalysisList(RawDataParamModel paramModel, DefaultMapResultHandler handler);
    List<Map<String, Object>> projectMetaDataAnalysisList(ProjectRawDataParamModel paramModel);
    @SwitchClientDS(datasource ="starrock_dndc")
    long projectMetaDataAnalysisListCount(ProjectRawDataParamModel paramModel);
}
