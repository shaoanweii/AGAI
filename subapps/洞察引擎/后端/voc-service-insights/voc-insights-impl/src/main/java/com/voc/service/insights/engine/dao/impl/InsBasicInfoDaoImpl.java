package com.voc.service.insights.engine.dao.impl;

import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.dao.InsBasicInfoDao;
import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.entity.InsDictInfoEntity;
import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import com.voc.service.insights.engine.entity.InsVehicleInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsBasicInfoMapper;
import com.voc.service.insights.engine.mapper.InsDictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:52
 * @描述:
 **/
@Repository
public class InsBasicInfoDaoImpl implements InsBasicInfoDao {
    @Autowired
    InsBasicInfoMapper basicInfoMapper;
    @Autowired
    InsDictMapper dictMapper;
    @Autowired
    InsConvertMapperService convertMapperService;

    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsEnergyInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 16:01
     * @描述 获取能源信息
     **/
    @Override
    public List<InsDictInfoEntity> findEnergyInfo(String energyType) {
        return dictMapper.findDictInfo(energyType);
    }

    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 16:02
     * @描述 获取省市信息
     **/
    @Override
    public List<InsProvinceAreaInfoEntity> findProvinceAreaInfo() {
        return basicInfoMapper.findProvinceAreaInfo();
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public List<InsProvinceAreaInfoEntity> findAllProvinceAreaInfo(String brandName) {
        return basicInfoMapper.findAllProvinceAreaInfo(brandName);
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public List<InsProvinceAreaInfoEntity> findAllProvinceAreaList() {
        return basicInfoMapper.findAllProvinceAreaList();
    }

    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsVehicleInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/23 14:11
     * @描述 获取车辆信息
     **/
    @Override
    public List<InsVehicleInfoEntity> findVehicleInfo() {
        return dictMapper.findVehicleInfo();
    }

    @Override
    public List<InsDictInfoEntity> findCarType(String carType) {
        return dictMapper.findDictInfo(carType);
    }

    @Override
    public List<InsBrandInfoEntity> findAllBrandInfo() {
        return basicInfoMapper.findAllBrandInfo();
    }
}
