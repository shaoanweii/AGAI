package com.voc.service.insights.engine.impl;

import com.alibaba.fastjson.JSONArray;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.insights.engine.api.IInsBasicInfoService;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.dao.InsBasicInfoDao;
import com.voc.service.insights.engine.entity.InsDictInfoEntity;
import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import com.voc.service.insights.engine.entity.InsVehicleInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.vo.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 10:19
 * @描述:
 **/
@Service
public class InsBasicInfoServiceImpl implements IInsBasicInfoService {

    private static final Logger log = LoggerFactory.getLogger(InsBasicInfoServiceImpl.class);
    @Autowired
    InsConvertMapperService basicInfoConvertMapper;
    @Autowired
    InsBasicInfoDao basicInfoDao;
    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 此处分页无效
     *
     * @param pageNumber
     * @param pageSize
     * @return
     */
    @Override
    @Cached(area="VDP" ,name = ":dict", key = "':C{appId}:energy_type:'+ #pageNumber + '_' + #pageSize", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public PageInfo findEnergyInfo(Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<EnergyInfoVo> energyInfoVos = this.findAllEnergyInfo();
        PageInfo pageInfo = new PageInfo(energyInfoVos);
        return pageInfo;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:province_area:' + #pageNumber + '_' + #pageSize", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public PageInfo findProvinceAreaInfoVo(Integer pageNumber, Integer pageSize) {
        List<InsProvinceAreaInfoEntity> provinceAreaInfo = basicInfoDao.findProvinceAreaInfo();
        if (ObjectUtils.isEmpty(provinceAreaInfo)) {
            log.info("无省市信息");
            return new PageInfo();
        }

        PageInfo pageInfos = new PageInfo(provinceAreaInfo);
        if(log.isDebugEnabled()) {
            log.debug("转换前 provinceAreaInfo:{}", JSONArray.toJSONString(provinceAreaInfo));
        }
        List<ProvinceAreaVo> list = basicInfoConvertMapper.provinceAreaEntityListConvertVoList(provinceAreaInfo);
        if(log.isDebugEnabled()) {
            log.debug("转换后 provinceAreaInfoVos:{}", JSONArray.toJSONString(list));
        }
        pageInfos.setList(list);
        return pageInfos;
    }

    @Override
    @Cached(area="VDP" ,name = ":data_local:", key = "':C{appId}:province_area:map:all'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public Map<String, List<ProvinceAreaVo>> findAllProvinceAreaInfo() {
        List<ProvinceAreaVo> provinceAreaInfoVos = this.findAll();
        if(log.isDebugEnabled()) {
            log.debug("转换后 provinceAreaInfoVos:{}", JSONArray.toJSONString(provinceAreaInfoVos));
        }
        //按省份编码进行分组
        if (ObjectUtils.isEmpty(provinceAreaInfoVos)) {
            log.info("暂无省市信息");
            return Collections.EMPTY_MAP;
        }
        Map<String, List<ProvinceAreaVo>> provinceAreaVos = provinceAreaInfoVos.stream().sorted(Comparator.comparing(ProvinceAreaVo::getProvinceCode,Comparator.nullsLast(Comparator.naturalOrder()))).collect(Collectors.groupingBy(ProvinceAreaVo::getProvinceCode,LinkedHashMap::new, Collectors.toList()));
        return provinceAreaVos;
    }

    @Override
   // @Cached(area="VDP" ,name = ":data_local:", key = "'province_area:list:all'", expire = 60 * 60, cacheType = CacheType.LOCAL )
    public List<ProvinceAreaVo> findAll() {
        log.trace("读取数据库");
        final List<InsProvinceAreaInfoEntity> provinceAreaInfo = basicInfoDao.findAllProvinceAreaInfo(null);
        if (ObjectUtils.isEmpty(provinceAreaInfo)) {
            log.info("暂无省市信息");
            return Collections.EMPTY_LIST;
        }
        final List<ProvinceAreaVo> list = basicInfoConvertMapper.provinceAreaEntityListConvertVoList(provinceAreaInfo);
        return list;
    }

    @Override
    public List<ProvinceAreaVo> findAllList() {
        log.trace("读取数据库");
        final List<InsProvinceAreaInfoEntity> provinceAreaInfo = basicInfoDao.findAllProvinceAreaList();
        if (ObjectUtils.isEmpty(provinceAreaInfo)) {
            log.info("暂无省市信息");
            return Collections.EMPTY_LIST;
        }
        final List<ProvinceAreaVo> list = basicInfoConvertMapper.provinceAreaEntityListConvertVoList(provinceAreaInfo);
        return list;
    }

    @Override
    public List<ProvinceAreaVo> findAllByBrandName(String brandName) {
        log.trace("读取数据库");
        final List<InsProvinceAreaInfoEntity> provinceAreaInfo = basicInfoDao.findAllProvinceAreaInfo(brandName);
        if (ObjectUtils.isEmpty(provinceAreaInfo)) {
            log.info("暂无省市信息");
            return Collections.EMPTY_LIST;
        }
        final List<ProvinceAreaVo> list = basicInfoConvertMapper.provinceAreaEntityListConvertVoList(provinceAreaInfo);
        return list;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:energy_type:all'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<EnergyInfoVo> findAllEnergyInfo() {
        log.trace("读取数据库");
        final List<InsDictInfoEntity> energyInfo = basicInfoDao.findEnergyInfo(InsightsConstants.ENERGY_TYPE);
        if (ObjectUtils.isEmpty(energyInfo)) {
            log.info("无能源信息");
            return Collections.EMPTY_LIST;
        }
        if(log.isDebugEnabled()) {
            log.debug("转换前 insEnergyInfoEntities:{}", JSONArray.toJSONString(energyInfo));
        }
        final List<EnergyInfoVo> list = basicInfoConvertMapper.dictEntityListConvertEnergyEntityList(energyInfo);
        if(log.isDebugEnabled()) {
            log.debug("转换后 energyInfoVos:{}", JSONArray.toJSONString(list));
        }
        return list;
    }

    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:vehicle_stage:' +#pageNumber + '_' + #pageSize", expire = 60 * 60, cacheType = CacheType.REMOTE )
    @Override
    public PageInfo findVehicleInfo(Integer pageNumber, Integer pageSize) {
        List<InsVehicleInfoEntity> vehicleInfo = basicInfoDao.findVehicleInfo();
        if (ObjectUtils.isEmpty(vehicleInfo)) {
            log.info("无车辆信息");
            return new PageInfo();
        }
        PageHelper.startPage(pageNumber, pageSize);
        LinkedHashMap<String, List<InsVehicleInfoEntity>> collect = vehicleInfo.stream().collect(Collectors.groupingBy(InsVehicleInfoEntity::getCarType, LinkedHashMap::new, Collectors.toList()));
        List<VehicleInfoVo> list = collect.entrySet().stream().map(e -> {
            VehicleInfoVo vehicleInfoVo = new VehicleInfoVo();
            final List<InsVehicleInfoEntity> value = e.getValue();
            InsVehicleInfoEntity insVehicleInfoEntity = value.stream().findFirst().get();
            String collect1 = value.stream().map(k -> k.getCarLevel()).collect(Collectors.joining("、"));
            vehicleInfoVo.setCarType(insVehicleInfoEntity.getCarType());
            vehicleInfoVo.setCarLevel(collect1);
            return vehicleInfoVo;
        }).collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo(list);
        return pageInfo;
    }


    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:car_type:all'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<CarTypeVo> findCarType() {
        log.trace("读取数据库");
        final List<InsDictInfoEntity> carType = basicInfoDao.findCarType(InsightsConstants.CAR_TYPE);
        if (ObjectUtils.isEmpty(carType)) {
            log.info("无车辆类型");
            return Collections.EMPTY_LIST;
        }
        final List<CarTypeVo> list = basicInfoConvertMapper.dictEntityListConvertCarVoList(carType);
        if(log.isDebugEnabled()) {
            log.debug("转换后 energyInfoVos:{}", JSONArray.toJSONString(list));
        }
        return list;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:label_type:all'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<LabelTypeInfoVo> findLabelTypeInfo() {
        log.trace("读取数据库");
        final List<InsDictInfoEntity> energyInfo = basicInfoDao.findEnergyInfo(InsightsConstants.LABEL_TYPE);
        if (ObjectUtils.isEmpty(energyInfo)) {
            log.info("无标签类型信息");
            return Collections.EMPTY_LIST;
        }
        Map<String, List<InsDictInfoEntity>> collect = energyInfo.stream().collect(Collectors.groupingBy(InsDictInfoEntity::getTypeCode, LinkedHashMap::new, Collectors.toList()));
        List<LabelTypeInfoVo> list = collect.entrySet().stream().map(e -> {
            LabelTypeInfoVo dictInfoEntity = new LabelTypeInfoVo();
            final List<InsDictInfoEntity> value = e.getValue();
            final List<InsDictInfoEntity> classify = value.stream().filter(k -> ObjectUtils.isNotEmpty(k.getClassifyName())).collect(Collectors.toList());
            final List<InsDictInfoEntity> processingModel = value.stream().filter(k -> ObjectUtils.isNotEmpty(k.getProcessingModel())).collect(Collectors.toList());
            InsDictInfoEntity insDictInfoEntity = classify.stream().findFirst().get();
            String collect1 = classify.stream().map(k -> k.getClassifyName()).collect(Collectors.joining("、"));
            if(ObjectUtils.isNotEmpty(processingModel)){
                String collect2 = processingModel.stream().map(k -> k.getProcessingModel()).collect(Collectors.joining("、"));
                dictInfoEntity.setProcessingModel(collect2);
            }
            dictInfoEntity.setTypeName(insDictInfoEntity.getTypeName());
            dictInfoEntity.setClassifyName(collect1);
            return dictInfoEntity;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:label_type:'+ #pageNumber + '_' + #pageSize", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public PageInfo findLabelTypeInfo(Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<LabelTypeInfoVo> labelTypeInfo = this.findLabelTypeInfo();
        PageInfo pageInfo = new PageInfo(labelTypeInfo);
        return pageInfo;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:seriousness_info:all'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<LabelTypeInfoVo> findSeriousnessInfo() {
        log.trace("读取数据库");
        final List<InsDictInfoEntity> energyInfo = basicInfoDao.findEnergyInfo(InsightsConstants.SERIOUSNESS);
        if (ObjectUtils.isEmpty(energyInfo)) {
            log.info("无严重性信息");
            return Collections.EMPTY_LIST;
        }
        List<LabelTypeInfoVo> collect = energyInfo.stream().map(e -> {
            LabelTypeInfoVo dictInfoEntity = new LabelTypeInfoVo();
            dictInfoEntity.setTypeName(e.getTypeName());
            return dictInfoEntity;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:seriousness_info:'+ #pageNumber + '_' + #pageSize", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public PageInfo findSeriousnessInfo(Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<LabelTypeInfoVo> labelTypeInfo = this.findSeriousnessInfo();
        PageInfo pageInfo = new PageInfo(labelTypeInfo);
        return pageInfo;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:user_journey:all'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<LabelTypeInfoVo> findUserJourneyInfo() {
        log.trace("读取数据库");
        final List<InsDictInfoEntity> energyInfo = basicInfoDao.findEnergyInfo(InsightsConstants.USER_JOURNEY);
        if (ObjectUtils.isEmpty(energyInfo)) {
            log.info("无用户旅程信息");
            return Collections.EMPTY_LIST;
        }
        List<LabelTypeInfoVo> collect = energyInfo.stream().map(e -> {
            LabelTypeInfoVo dictInfoEntity = new LabelTypeInfoVo();
            dictInfoEntity.setTypeName(e.getTypeName());
            return dictInfoEntity;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:user_journey:'+ #pageNumber + '_' + #pageSize", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public PageInfo findUserJourneyInfo(Integer pageNumber, Integer pageSize) {
        PageHelper.startPage(pageNumber, pageSize);
        List<LabelTypeInfoVo> labelTypeInfo = this.findUserJourneyInfo();
        PageInfo pageInfo = new PageInfo(labelTypeInfo);
        return pageInfo;
    }

    @Override
    @Cached(area="VDP" ,name = ":data:", key = "':C{appId}:labelAndModel:all'", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<LabelAndModelVo> findLabelAndModel() {
        log.trace("读取数据库");
        final List<InsDictInfoEntity> energyInfo = basicInfoDao.findEnergyInfo(InsightsConstants.LABEL_TYPE);
        if (ObjectUtils.isEmpty(energyInfo)) {
            log.info("无标签类型信息");
            return Collections.EMPTY_LIST;
        }
        List<InsDictInfoEntity> collect = energyInfo.stream().filter(e -> ObjectUtils.isNotEmpty(e.getProcessingModel())).collect(Collectors.toList());
        List<LabelAndModelVo> labelAndModelVos = basicInfoConvertMapper.labelAndModelConvert(collect);
        return labelAndModelVos;
    }


}
