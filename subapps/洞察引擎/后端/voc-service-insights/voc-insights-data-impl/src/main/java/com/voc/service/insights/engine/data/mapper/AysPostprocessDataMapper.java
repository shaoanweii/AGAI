package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.model.ProjectResultDataParamModel;
import com.voc.service.insights.engine.api.model.RawDataParamModel;
import com.voc.service.insights.engine.api.model.ResultDataParamModel;
import com.voc.service.insights.engine.data.entity.AysPostprocessDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.mapping.ResultSetType;

import java.util.List;
import java.util.Map;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysPostprocessDataMapper extends BaseMapper<AysPostprocessDataEntity> {

    @SwitchClientDS(datasource ="starrock_dndc")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(Map.class)
    List<Map<String, Object>> pagePostprocessDataList(ResultDataParamModel paramModel);

    @SwitchClientDS(datasource ="starrock_dndc")
    long pagePostprocessDataListCount(ResultDataParamModel paramModel);


    @SwitchClientDS(datasource ="starrock_dndc")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(Map.class)
    List<Map<String, Object>> pageProjectPostDataList(ProjectResultDataParamModel paramModel);

    @SwitchClientDS(datasource ="starrock_dndc")
    long pageProjectPostDataCount(ProjectResultDataParamModel paramModel);

    @SwitchClientDS(datasource ="starrock_dndc")
    String getDateTime(String dateTime);


}