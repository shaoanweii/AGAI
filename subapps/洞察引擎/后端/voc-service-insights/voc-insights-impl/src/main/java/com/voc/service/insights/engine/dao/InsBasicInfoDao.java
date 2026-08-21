package com.voc.service.insights.engine.dao;

import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.entity.InsDictInfoEntity;
import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import com.voc.service.insights.engine.entity.InsVehicleInfoEntity;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:51
 * @描述:
 **/
public interface InsBasicInfoDao {

    List<InsDictInfoEntity> findEnergyInfo(String energyType);

    List<InsProvinceAreaInfoEntity> findProvinceAreaInfo();

    List<InsProvinceAreaInfoEntity> findAllProvinceAreaInfo(String brandName);
    List<InsProvinceAreaInfoEntity> findAllProvinceAreaList();

    List<InsBrandInfoEntity> findAllBrandInfo();

    List<InsVehicleInfoEntity> findVehicleInfo();

    List<InsDictInfoEntity> findCarType(String carType);
}
