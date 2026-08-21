package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.data.entity.AysCqCaMetaDataAnalysisEntity;
import com.voc.service.insights.engine.data.entity.AysMetaDataAnalysisEntity;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysCqCaMetaDataAnalysisMapper extends BaseMapper<AysMetaDataAnalysisEntity> {


    @SwitchClientDS(datasource ="starrock_dndc")
    List<AysCqCaMetaDataAnalysisEntity> pageMetaDataAnalysisList(InsCqCaDataQueryModel insCqCaDataQueryModel);
    @SwitchClientDS(datasource ="starrock_dndc")
    Integer countMetaDataAnalysisList(InsCqCaDataQueryModel insCqCaDataQueryModel);

    @SwitchClientDS(datasource ="starrock_dndc")
    List<AysCqCaMetaDataAnalysisEntity> pageMetaDataIdList(InsCqCaDataQueryModel insCqCaDataQueryModel);
    @SwitchClientDS(datasource ="starrock_dndc")
    List<AysCqCaMetaDataAnalysisEntity> getMetaDataByIds(InsCqCaDataQueryModel insCqCaDataQueryModel);
    @SwitchClientDS(datasource ="starrock_dndc")
    List<AysCqCaMetaDataAnalysisEntity> findMateDataAnalysisList(@Param("model") InsCqCaDataQueryModel insCqCaDataQueryModel);
    @SwitchClientDS(datasource ="starrock_dndc")
    Integer countMateDataAnalysisDetailList(@Param("model") InsCqCaDataQueryModel insCqCaDataQueryModel);
}
