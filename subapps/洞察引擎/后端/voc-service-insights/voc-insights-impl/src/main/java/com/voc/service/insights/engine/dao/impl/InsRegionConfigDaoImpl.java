package com.voc.service.insights.engine.dao.impl;

import com.voc.service.common.exception.BussinessException;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.dao.InsRegionConfigDao;
import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import com.voc.service.insights.engine.entity.InsRegionDetailEntity;
import com.voc.service.insights.engine.entity.InsRegionEntity;
import com.voc.service.insights.engine.mapper.InsRegionDetailMapper;
import com.voc.service.insights.engine.mapper.InsRegionMapper;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:40
 * @描述:
 **/
@Repository
public class InsRegionConfigDaoImpl implements InsRegionConfigDao {
    private static final Logger log = LoggerFactory.getLogger(InsRegionConfigDaoImpl.class);
    @Autowired
    InsRegionMapper regionMapper;
    @Autowired
    InsRegionDetailMapper regionDetailMapper;

    @Override
    @SwitchClientDS
    public void saveRegionCategory(String clientId,InsRegionEntity regionEntity) {
        int insert = regionMapper.insert(regionEntity);
        if(insert>0){
            log.info("保存区域分类成功");
        }else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_REGION_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void saveRegion(String clientId,InsRegionDetailEntity regionDetailEntity) {
        int insert = regionDetailMapper.insert(regionDetailEntity);
        if(insert>0){
            log.info("保存区域成功");
        }else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_REGION_DETAIL_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void updateRegionCategory(String clientId, InsRegionEntity regionDetailEntity) {
        try {
            regionMapper.updateRegionCategory(regionDetailEntity);
            log.info("更新区域分类成功");
        }catch (Exception e){
            throw new BussinessException(InsCommonErrorEnum.UPDATE_REGION_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void updateRegion(String clientId,InsRegionDetailEntity regionDetailEntity) {
        try {
            regionDetailMapper.updateById(regionDetailEntity);
            log.info("更新区域成功");
        }catch (Exception e){
            log.error("{}",e);
            throw new BussinessException(InsCommonErrorEnum.UPDATE_REGION_DETAIL_ERROR);
        }
    }

    @Override
    @SwitchClientDS
    public void deleteRegionCategory(String clientId, String regionCategoryId) {
        try {
            regionMapper.deleteRegionCategory(regionCategoryId);
            log.info("删除区域分类成功");
        }catch (Exception e){
            throw new BussinessException(InsCommonErrorEnum.DELETE_REGION_ERROR);
        }
    }

    @Override
    @SwitchClientDS(objectAttribute = "regionConfigModel.clientId")
    public List<InsRegionEntity> findRegionCategoryList(InsRegionConfigModel regionConfigModel) {
        return regionMapper.findRegionCategoryList(regionConfigModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "regionConfigModel.clientId")
    public List<InsRegionEntity> findRegionCategoryListHierarchical(InsRegionConfigModel regionConfigModel) {
        return regionMapper.findRegionCategoryListHierarchical(regionConfigModel);
    }

    @Override
    @SwitchClientDS(objectAttribute = "regionConfigModel.clientId")
    public List<InsRegionEntity> findAllRegionCategoryList(InsRegionConfigModel regionConfigModel) {
        return regionMapper.findAllRegionCategoryList(regionConfigModel);
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
//    @SwitchClientDS(objectAttribute = "regionConfigModel.clientId")
    public List<InsProvinceAreaInfoEntity> findRegionList(InsRegionConfigModel regionConfigModel) {
        return regionDetailMapper.findRegionList(regionConfigModel);
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public List<InsProvinceAreaInfoEntity> findRegionListByProvinceCode(InsRegionConfigModel regionConfigModel) {
        return regionDetailMapper.findRegionListByProvinceCode(regionConfigModel);
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public List<InsProvinceAreaInfoEntity> findRegionListByProvinceCode1(InsRegionConfigModel regionConfigModel) {
        return regionDetailMapper.findRegionListByProvinceCode1(regionConfigModel);
    }

    @Override
    @SwitchClientDS
    public List<String> findRegionCategoryChildIdsByParentId(String clientId,String regionId) {
        return regionMapper.findRegionChildIdsByParentId(regionId);
    }

    @Override
    @SwitchClientDS
    public Integer findRegionChildCountByRegionIds(String clientId, List<String> regionIds) {
        return regionDetailMapper.findRegionChildCountByRegionIds(regionIds);
    }

    @Override
    @SwitchClientDS
    public List<InsRegionEntity> findRegionCategoryByName(String clientId, String name, String parentId) {
        return regionMapper.findRegionCategoryByName(name,parentId);
    }

    @Override
    @SwitchClientDS
    public List<InsRegionDetailEntity> findRegionByName(String clientId, String name) {
        return regionDetailMapper.findRegionByName(name);
    }
}
