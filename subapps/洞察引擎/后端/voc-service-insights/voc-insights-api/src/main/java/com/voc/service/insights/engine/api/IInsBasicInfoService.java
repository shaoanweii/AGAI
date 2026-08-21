package com.voc.service.insights.engine.api;

import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.vo.*;

import java.util.List;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 10:13
 * @描述:
 **/
public interface IInsBasicInfoService {
    /**
     * @return java.util.List<com.voc.service.insights.engine.vo.EnergyInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 10:18
     * @描述 查询能源信息
     **/
    PageInfo findEnergyInfo(Integer pageNumber, Integer pageSize);
    /**
     * @return java.util.List<com.voc.service.insights.engine.vo.ProvinceAreaInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/21 11:03
     * @描述 查询省市信息(以分组)
     **/
    PageInfo findProvinceAreaInfoVo(Integer pageNumber, Integer pageSize);
    /**
     * @return java.util.List<com.voc.service.insights.engine.vo.ProvinceAreaInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/21 11:03
     * @描述 查询全部省市信息
     **/
    Map<String,List<ProvinceAreaVo>> findAllProvinceAreaInfo();

    List<ProvinceAreaVo> findAll();
    List<ProvinceAreaVo> findAllList();
    List<ProvinceAreaVo> findAllByBrandName(String brandName);

    /**
     * @return java.util.List<com.voc.service.insights.engine.vo.VehicleInfoVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/23 14:14
     * @描述 查询车辆信息
     **/
    PageInfo findVehicleInfo(Integer pageNumber, Integer pageSize);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/26 14:30
     * @描述 查询全部能源信息
     * @return java.util.List<com.voc.service.insights.engine.vo.EnergyInfoVo>
     **/
    List<EnergyInfoVo> findAllEnergyInfo();

    List<CarTypeVo> findCarType();

    List<LabelTypeInfoVo> findLabelTypeInfo();

    PageInfo findLabelTypeInfo(Integer pageNumber, Integer pageSize);

    List<LabelTypeInfoVo> findSeriousnessInfo();

    PageInfo findSeriousnessInfo(Integer pageNumber, Integer pageSize);

    List<LabelTypeInfoVo> findUserJourneyInfo();

    PageInfo findUserJourneyInfo(Integer pageNumber, Integer pageSize);

    List<LabelAndModelVo> findLabelAndModel();




}
