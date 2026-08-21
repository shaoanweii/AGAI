package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.data.entity.AysCqCaPostprocessDataEntity;
import com.voc.service.insights.engine.data.entity.AysPostprocessDataEntity;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.BaseCarSeriesDataVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @创建者: cuick
 * @创建时间: 2024/1/29 13:23
 * @描述:
 **/
@Mapper
public interface AysCqCaPostprocessDataMapper extends BaseMapper<AysPostprocessDataEntity> {

    @SwitchClientDS(datasource = "starrock_dndc")
    List<AysCqCaPostprocessDataEntity> pagePostprocessDataList(InsCqCaDataQueryModel insCqCaDataQueryModel);
    @SwitchClientDS(datasource = "starrock_dndc")
    Integer countPostprocessDataList(InsCqCaDataQueryModel insCqCaDataQueryModel);

    @SwitchClientDS(datasource = "starrock_dndc")
    List<AysCqCaPostprocessDataEntity> pagePostprocessIdList(InsCqCaDataQueryModel insCqCaDataQueryModel);
    @SwitchClientDS(datasource = "starrock_dndc")
    List<AysCqCaPostprocessDataEntity> getPostprocessDataByIds(InsCqCaDataQueryModel insCqCaDataQueryModel);


    @SwitchClientDS(datasource = "starrock_dndc")
    List<BaseCarSeriesDataVo> queryCarSeriesList(InsCqCaDataQueryModel insCqCaDataQueryModel);

    @SwitchClientDS(datasource = "starrock_dndc")
    List<BaseCarSeriesDataVo> findAllFinalTagLibClientVoList(InsCqCaDataQueryModel insCqCaDataQueryModel);

    @SwitchClientDS(datasource = "starrock_dndc")
    List<BaseCarSeriesDataVo> queryBrandList(InsCqCaDataQueryModel insCqCaDataQueryModel);
}