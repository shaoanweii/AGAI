package com.voc.service.insights.engine.data.dao;

import com.voc.service.insights.engine.data.entity.InsRegulationDetailEntity;
import com.voc.service.insights.engine.model.InsRegulationInfoModel;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 13:46
 * @描述:
 **/
public interface InsRegulationDetailsDao {

    void saveRegulationDetails(List<InsRegulationDetailEntity> regulationDetails,String clientId);
    void updateRegulationDetails(List<InsRegulationDetailEntity> regulationDetails,String clientId);
    void deleteRegulationDetails(String regulationId,String userName);

    List<InsRegulationDetailEntity> findRegulationDetailsById(String regulationId,String clientId);

    List<InsRegulationDetailEntity> findAllRegulationDetails(InsRegulationInfoModel regulationInfoMode);
    List<InsRegulationDetailEntity> findAllStandardRegulationDetails();

    void removeRegulationDetails(String regulationId,String clientId);

}
