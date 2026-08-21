package com.voc.service.insights.engine.data.dao;

import com.voc.service.insights.engine.data.entity.InsSIDataSourceEntity;
import com.voc.service.insights.engine.model.InsDataSourceRequestModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.AttrMappingVo;
import com.voc.service.insights.engine.vo.InsDataSourceOriginDataVo;
import com.voc.service.insights.engine.vo.InsDataSourceResultVo;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/28 下午4:08
 * @描述:
 **/
public interface InsSIDataSourceDao {

    InsDataSourceResultVo getDataSourceResult(InsDataSourceRequestModel dataSourceRequestModel);

    void saveOrUpdateDataSource(InsDataSourceResultVo dataSourceResultVo);

    void saveOrUpdateBatchDataSource(String clientId,List<InsDataSourceResultVo> dataSourceResultVoList);

    List<InsSIDataSourceEntity> findSIDataSourceList(InsDataSourceModel insDataSourceModel);

    void updateSIDataSource(InsDataSourceModel insDataSourceModel);

    List<InsDataSourceOriginDataVo> findVerificationResultByCondition(InsDataSourceRequestModel insDataSourceModel);

    List<AttrMappingVo> findAllAttrMapping();
}
