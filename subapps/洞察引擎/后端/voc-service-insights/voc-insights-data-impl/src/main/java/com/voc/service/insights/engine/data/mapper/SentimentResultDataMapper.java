package com.voc.service.insights.engine.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.data.entity.SentimentResultDataEntity;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @创建者: CLine
 * @创建时间: 2026/1/27
 * @描述: 情感分析结果数据Mapper
 **/
@Mapper
public interface SentimentResultDataMapper extends BaseMapper<SentimentResultDataEntity> {

    @SwitchClientDS(datasource = "starrock_dndc")
    List<SentimentResultDataEntity> pageSentimentResultDataList(InsCqCaDataQueryModel insCqCaDataQueryModel);
}
