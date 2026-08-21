package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.entity.InsEnergyInfoEntity;
import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 10:25
 * @描述:
 **/
@Mapper
@Repository
public interface InsBasicInfoMapper extends BaseMapper<InsEnergyInfoEntity> {


    /**
     * @return java.util.List<com.voc.service.insights.setting.entity.InsProvinceAreaInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 10:55
     * @描述 查询省市信息
     **/
    List<InsProvinceAreaInfoEntity> findProvinceAreaInfo();

    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/21 10:53
     * @描述 查询全部省市信息
     **/
    List<InsProvinceAreaInfoEntity> findAllProvinceAreaInfo(@Param("brandName") String brandName);

    List<InsBrandInfoEntity> findAllBrandInfo();

    List<InsProvinceAreaInfoEntity> findAllProvinceAreaList();
}
