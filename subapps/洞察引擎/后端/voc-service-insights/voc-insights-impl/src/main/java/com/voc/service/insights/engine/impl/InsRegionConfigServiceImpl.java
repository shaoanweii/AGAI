package com.voc.service.insights.engine.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsBasicInfoService;
import com.voc.service.insights.engine.api.IInsRegionConfigService;
import com.voc.service.insights.engine.dao.InsRegionConfigDao;
import com.voc.service.insights.engine.entity.InsProvinceAreaInfoEntity;
import com.voc.service.insights.engine.entity.InsRegionDetailEntity;
import com.voc.service.insights.engine.entity.InsRegionEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.model.AreaModel;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import com.voc.service.insights.engine.model.ProvinceModel;
import com.voc.service.insights.engine.vo.ProvinceAreaVo;
import com.voc.service.insights.engine.vo.RegionConfigVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:28
 * @描述:
 **/
@Service
public class InsRegionConfigServiceImpl implements IInsRegionConfigService {
    private static final Logger log = LoggerFactory.getLogger(InsRegionConfigServiceImpl.class);
    @Autowired
    InsConvertMapperService mapperService;
    @Autowired
    InsRegionConfigDao regionConfigDao;

    @Autowired
    IInsBasicInfoService basicInfoService;


    @Override
    public void saveRegionCategory(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getName(),"分类名称不允许为空");
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        //用户名
        final String username = ServiceContextHolder.getUsername();
        //客户id
        final String clientId = regionConfigModel.getClientId();
        if(ObjectUtils.isEmpty(regionConfigModel.getParentId())){
            regionConfigModel.setParentId("0");
        }
        List<InsRegionEntity> regionEntities = regionConfigDao.findRegionCategoryByName(clientId,regionConfigModel.getName(),regionConfigModel.getParentId());
        Assert.isTrue(ObjectUtils.isEmpty(regionEntities),"区域分类名称已存在");
        List<InsRegionDetailEntity> regionDetailEntities = regionConfigDao.findRegionByName(clientId,regionConfigModel.getName());
        Assert.isTrue(ObjectUtils.isEmpty(regionDetailEntities),"区域名称已存在");
        InsRegionEntity regionEntity = mapperService.regionModelConvertRegionEntity(regionConfigModel);
        regionEntity.setId(IdWorker.getId());
        regionEntity.setCreateTime(LocalDateTime.now());
        regionEntity.setCreateUser(username);
        regionConfigDao.saveRegionCategory(clientId,regionEntity);
    }

    @Override
    public void saveRegion(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getName(),"区域名称不允许为空");
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        Assert.hasLength(regionConfigModel.getParentId(),"分类id不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(regionConfigModel.getRegion()),"关联省市不允许为空");
        Assert.hasLength(regionConfigModel.getStatus(),"状态不允许为空");
        Assert.isTrue(regionConfigModel.getStatus().equalsIgnoreCase("1")
                ||regionConfigModel.getStatus().equalsIgnoreCase("0"),"状态码无效");
        //用户名
        final String username = ServiceContextHolder.getUsername();
        //客户id
        final String clientId = regionConfigModel.getClientId();
        List<InsRegionDetailEntity> regionDetailEntities = regionConfigDao.findRegionByName(clientId,regionConfigModel.getName());
        Assert.isTrue(ObjectUtils.isEmpty(regionDetailEntities),"区域名称已存在");
        InsRegionDetailEntity regionEntity = mapperService.regionModelConvertRegionDetailEntity(regionConfigModel);
        regionEntity.setId(IdWorker.getId());
        regionEntity.setCreateTime(LocalDateTime.now());
        regionEntity.setCreateUser(username);
        regionConfigDao.saveRegion(clientId,regionEntity);
    }

    @Override
    public void updateRegionCategory(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getName(),"分类名称不允许为空");
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        //用户名
        final String username = ServiceContextHolder.getUsername();
        //客户id
        final String clientId = regionConfigModel.getClientId();
        List<InsRegionEntity> regionEntities = regionConfigDao.findRegionCategoryByName(clientId,regionConfigModel.getName(),regionConfigModel.getParentId());
        List<InsRegionEntity> collect = regionEntities.stream().filter(e -> !e.getId().equalsIgnoreCase(regionConfigModel.getId())).collect(Collectors.toList());
        Assert.isTrue(ObjectUtils.isEmpty(collect),"区域分类名称已存在");
        List<InsRegionDetailEntity> regionDetailEntities = regionConfigDao.findRegionByName(clientId,regionConfigModel.getName());
        List<InsRegionDetailEntity> collect1 = regionDetailEntities.stream().filter(e -> !e.getId().equalsIgnoreCase(regionConfigModel.getId())).collect(Collectors.toList());
        Assert.isTrue(ObjectUtils.isEmpty(collect1),"区域名称已存在");
        InsRegionEntity regionEntity = mapperService.regionModelConvertRegionEntity(regionConfigModel);
        regionEntity.setUpdateTime(LocalDateTime.now());
        regionEntity.setUpdateUser(username);
        regionConfigDao.updateRegionCategory(clientId,regionEntity);
    }

    @Override
    public void updateRegion(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getName(),"区域名称不允许为空");
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        Assert.hasLength(regionConfigModel.getParentId(),"分类id不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(regionConfigModel.getRegion()),"关联省市不允许为空");
        Assert.hasLength(regionConfigModel.getStatus(),"状态不允许为空");
        Assert.isTrue(regionConfigModel.getStatus().equalsIgnoreCase("1")
                ||regionConfigModel.getStatus().equalsIgnoreCase("0"),"状态码无效");
        //用户名
        final String username = ServiceContextHolder.getUsername();
        //客户id
        final String clientId = regionConfigModel.getClientId();
        List<InsRegionDetailEntity> regionDetailEntities = regionConfigDao.findRegionByName(clientId,regionConfigModel.getName());
        List<InsRegionDetailEntity> collect = regionDetailEntities.stream().filter(e -> !e.getId().equalsIgnoreCase(regionConfigModel.getId())).collect(Collectors.toList());
        Assert.isTrue(ObjectUtils.isEmpty(collect),"区域名称已存在");
        InsRegionDetailEntity regionEntity = mapperService.regionModelConvertRegionDetailEntity(regionConfigModel);
        regionEntity.setUpdateTime(LocalDateTime.now());
        regionEntity.setUpdateUser(username);
        regionConfigDao.updateRegion(clientId,regionEntity);
    }

    @Override
    public void deleteRegionCategory(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getId(),"分类id不允许为空");
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        regionConfigModel.setParentId(regionConfigModel.getId());
//        List<InsRegionDetailEntity> regionDetailByRegionId = regionConfigDao.findRegionList(regionConfigModel);
//        if(ObjectUtils.isNotEmpty(regionDetailByRegionId)){
//            throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"当前区域分类下存在区域信息，不允许删除");
//        }else {
//            List<String> regionChildIdsByParentId = regionConfigDao.findRegionCategoryChildIdsByParentId(regionConfigModel.getClientId(),regionConfigModel.getId());
//            if(ObjectUtils.isEmpty(regionChildIdsByParentId)){
//                regionConfigDao.deleteRegionCategory(regionConfigModel.getClientId(),regionConfigModel.getId());
//            }else {
//                Integer count = regionConfigDao.findRegionChildCountByRegionIds(regionConfigModel.getClientId(),regionChildIdsByParentId);
//                if(count>0){
//                    throw new BussinessException(InsCommonErrorEnum.CONSTRAINT_VIOLATION_ERROR,"当前区域分类下存在区域信息，不允许删除");
//                }else {
//                    regionConfigDao.deleteRegionCategory(regionConfigModel.getClientId(),regionConfigModel.getId());
//                }
//            }
//        }
    }

    @Override
    public PageInfo findRegionCategoryList(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        //获取全部省市信息
        List<ProvinceAreaVo> allArea = basicInfoService.findAll();
        PageHelper.startPage(regionConfigModel.getPageNum(), regionConfigModel.getPageSize());
        if(ObjectUtils.isEmpty(allArea)){
            log.info("无区域分类信息");
            return new PageInfo();
        }
        Set<String> set = new HashSet<>();
        List<RegionConfigVo> collect = allArea.stream().map(e -> {
            if(set.add(e.getBigAreaSale())){
                return RegionConfigVo.builder().id(e.getBigAreaSale()).name(e.getBigAreaSale()).status("1").build();
            }else {
                return null;
            }
        }).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo(collect);
        return pageInfo;
    }

    @Override
    public PageInfo findRegionList(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        Assert.hasLength(regionConfigModel.getParentId(),"分类id不允许为空");
        List<ProvinceAreaVo> allArea = basicInfoService.findAllList();
//        PageHelper.startPage(regionConfigModel.getPageNum(), regionConfigModel.getPageSize());
        final String parentId = regionConfigModel.getParentId();
        log.info("parentId： {} allArea  {}， {}",parentId ,allArea.size(),  allArea.stream().map(ProvinceAreaVo::getBigAreaSale).collect(Collectors.toSet()));
       ;
        //获取全部省市信息
        List<ProvinceAreaVo> collect = allArea.stream().filter(e -> e.getBigAreaSale().equals(parentId)).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(collect)) {
            log.info("无区域信息");
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(collect);
        Map<String, List<ProvinceAreaVo>> collect2 = collect.stream().filter(k -> ObjectUtils.isNotEmpty(k.getSmallAreaSale())).collect(Collectors.groupingBy(k -> k.getSmallAreaSale()));
        List<RegionConfigVo> collect1 = collect2.entrySet().stream().map(k -> {
                final String smallAreaName = k.getKey();
                final List<ProvinceAreaVo> smallAreaNameList = k.getValue();
                Map<String, List<ProvinceAreaVo>> provinceMap = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.groupingBy(v -> v.getProvinceCode()));
                Map<String, ProvinceAreaVo> collect5 = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.toMap(v -> v.getProvinceCode(), v -> v, (k1, k2) -> k2));
                List<ProvinceModel> collect3 = provinceMap.entrySet().stream().map(v -> {
                    final String provinceCode = v.getKey();
                    List<ProvinceAreaVo> provinceList = v.getValue();
                    Map<String, ProvinceAreaVo> cityCodeMap = provinceList.stream().filter(l -> ObjectUtils.isNotEmpty(l.getAreaCode())).collect(Collectors.toMap(l -> l.getAreaCode(), l -> l, (k1, k2) -> k2));
                    List<AreaModel> collect4 = cityCodeMap.entrySet().stream().map(l -> {
                        return AreaModel.builder().areaCode(l.getValue().getAreaCode()).areaName(l.getValue().getAreaName()).build();
                    }).collect(Collectors.toList());
                    return ProvinceModel.builder().provinceName(collect5.get(provinceCode).getProvinceName()).provinceCode(provinceCode).areas(collect4).build();
                }).collect(Collectors.toList());
                return RegionConfigVo.builder().name(smallAreaName).code(smallAreaName).status("1").region(collect3).build();
            }).collect(Collectors.toList());
        pageInfo.setList(collect1);
        return pageInfo;
    }

    @Override
    public List<AreaModel> findRegionAllList(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        PageHelper.startPage(regionConfigModel.getPageNum(), regionConfigModel.getPageSize());
        //获取全部省市信息
        List<ProvinceAreaVo> allArea = basicInfoService.findAll();
        if (ObjectUtils.isEmpty(allArea)) {
            log.info("无区域信息");
            return List.of();
        }
        List<AreaModel> areaList = new ArrayList<>();
        Map<String, List<ProvinceAreaVo>> collect2 = allArea.stream().filter(k -> ObjectUtils.isNotEmpty(k.getSmallAreaSale())).collect(Collectors.groupingBy(k -> k.getSmallAreaSale()));
        collect2.entrySet().stream().forEach(k -> {
            final String smallAreaName = k.getKey();
            final List<ProvinceAreaVo> smallAreaNameList = k.getValue();
            Map<String, List<ProvinceAreaVo>> provinceMap = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.groupingBy(v -> v.getProvinceCode()));
            Map<String, ProvinceAreaVo> collect5 = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.toMap(v -> v.getProvinceCode(), v -> v, (k1, k2) -> k2));
             provinceMap.entrySet().stream().forEach(v -> {
                final String provinceCode = v.getKey();
                List<ProvinceAreaVo> provinceList = v.getValue();
                Map<String, ProvinceAreaVo> cityCodeMap = provinceList.stream().filter(l -> ObjectUtils.isNotEmpty(l.getAreaCode())).collect(Collectors.toMap(l -> l.getAreaCode(), l -> l, (k1, k2) -> k2));
                List<AreaModel> collect4 = cityCodeMap.entrySet().stream().map(l -> {
                    return AreaModel.builder().areaCode(l.getValue().getAreaCode()).areaName(l.getValue().getAreaName()).build();
                }).collect(Collectors.toList());
                 areaList.addAll(collect4);
            });
        });
        return areaList;
    }

    @Override
    public List<RegionConfigVo> findRegionTree(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        Assert.hasLength(regionConfigModel.getBrandName(),"品牌不允许为空");
        //获取全部省市信息
        List<RegionConfigVo> province;
        List<ProvinceAreaVo> allArea = basicInfoService.findAllByBrandName(regionConfigModel.getBrandName());
        if(ObjectUtils.isEmpty(allArea)){
            return null;
        }
        Map<String, List<ProvinceAreaVo>> collect = allArea.stream().collect(Collectors.groupingBy(ProvinceAreaVo::getBigAreaSaleCode));
        province = collect.entrySet().stream().map(e->{
            final String key = e.getKey();
            final List<ProvinceAreaVo> value = e.getValue();
            final ProvinceAreaVo provinceAreaVo = value.stream().findAny().get();
            List<RegionConfigVo> collect1 = value.stream().map(k -> {
                final String smallAreaSaleCode = k.getSmallAreaSaleCode();
                final String smallAreaSale = k.getSmallAreaSale();
                return RegionConfigVo.builder().id(smallAreaSale).name(smallAreaSale).code(smallAreaSaleCode).build();
            }).collect(Collectors.toList());
//            Map<String, List<ProvinceAreaVo>> collect2 = value.stream().filter(k -> ObjectUtils.isNotEmpty(k.getSmallAreaSale())).collect(Collectors.groupingBy(k -> k.getSmallAreaSaleCode()));
//            List<RegionConfigVo> collect1 = collect2.entrySet().stream().map(k -> {
//                final String smallAreaName = k.getKey();
//                final List<ProvinceAreaVo> smallAreaNameList = k.getValue();
//                Map<String, List<ProvinceAreaVo>> provinceMap = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.groupingBy(v -> v.getProvinceCode()));
//                Map<String, ProvinceAreaVo> collect5 = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.toMap(v -> v.getProvinceCode(), v -> v, (k1, k2) -> k2));
//                List<RegionConfigVo> collect3 = provinceMap.entrySet().stream().map(v -> {
//                    final String provinceCode = v.getKey();
//                    List<ProvinceAreaVo> provinceList = v.getValue();
//                    Map<String, ProvinceAreaVo> cityCodeMap = provinceList.stream().filter(l -> ObjectUtils.isNotEmpty(l.getAreaCode())).collect(Collectors.toMap(l -> l.getAreaCode(), l -> l, (k1, k2) -> k2));
//                    List<RegionConfigVo> collect4 = cityCodeMap.entrySet().stream().map(l -> {
//                        return RegionConfigVo.builder().code(l.getValue().getAreaCode()).name(l.getValue().getAreaName()).build();
//                    }).collect(Collectors.toList());
//                    return RegionConfigVo.builder().name(collect5.get(provinceCode).getProvinceName()).code(provinceCode).child(collect4).build();
//                }).collect(Collectors.toList());
//                return RegionConfigVo.builder().name(smallAreaName).code(smallAreaName).build();
//            }).collect(Collectors.toList());
            return RegionConfigVo.builder().id(provinceAreaVo.getBigAreaSale()).name(provinceAreaVo.getBigAreaSale()).code(key).child(collect1).build();
        }).collect(Collectors.toList());
        return province;
    }

    @Override
    public List<RegionConfigVo> findRegionTreeByIds(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        //获取区域列表
        List<InsProvinceAreaInfoEntity> regionList = regionConfigDao.findRegionList(regionConfigModel);
        if(ObjectUtils.isEmpty(regionList)){
            log.info("无区域信息");
            return List.of();
        }
        //大区的集合
        Map<String, List<InsProvinceAreaInfoEntity>> bigAreaMap = regionList.stream().collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getBigAreaSaleCode));

        List<RegionConfigVo> regionCategoryVo = bigAreaMap.entrySet().stream().map(e -> {
            //大区名称
            final String key = e.getKey();
            //大区集合
            final List<InsProvinceAreaInfoEntity> value = e.getValue();
            final InsProvinceAreaInfoEntity provinceAreaVos = value.stream().findAny().get();
            List<RegionConfigVo> collect1 = value.stream().map(k -> {
                final String smallAreaSaleCode = k.getSmallAreaSaleCode();
                final String smallAreaSale = k.getSmallAreaSale();
                return RegionConfigVo.builder().id(smallAreaSale).name(smallAreaSale).code(smallAreaSaleCode).build();
            }).collect(Collectors.toList());
            //小区的集合
//            Map<String, List<InsProvinceAreaInfoEntity>> collect2 = value.stream().filter(k -> ObjectUtils.isNotEmpty(k.getSmallAreaSale())).collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getSmallAreaSaleCode));
//            List<RegionConfigVo> collect1 = collect2.entrySet().stream().map(k -> {
//                //小区名称
//                final String smallAreaName = k.getKey();
//                //小区集合
//                final List<InsProvinceAreaInfoEntity> smallAreaNameList = k.getValue();
//                //省份集合
//                Map<String, List<InsProvinceAreaInfoEntity>> provinceMap = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getProvinceCode));
//                //省份code+名称
//                Map<String, InsProvinceAreaInfoEntity> collect5 = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.toMap(v -> v.getProvinceCode(), v -> v, (k1, k2) -> k2));
//                List<RegionConfigVo> collect3 = provinceMap.entrySet().stream().map(v -> {
//                    //省份code
//                    final String provinceCode = v.getKey();
//                    //省份集合
//                    List<InsProvinceAreaInfoEntity> provinceList = v.getValue();
//                    //城市集合
//                    Map<String, InsProvinceAreaInfoEntity> cityCodeMap = provinceList.stream().filter(l -> ObjectUtils.isNotEmpty(l.getAreaCode())).collect(Collectors.toMap(l -> l.getAreaCode(), l -> l, (k1, k2) -> k2));
//                    List<RegionConfigVo> collect4 = cityCodeMap.entrySet().stream().map(l -> {
//                        return RegionConfigVo.builder().id(l.getValue().getAreaName()).name(l.getValue().getAreaName()).code(l.getValue().getAreaCode()).build();
//                    }).collect(Collectors.toList());
//                    return RegionConfigVo.builder().id(collect5.get(provinceCode).getProvinceName()).code(provinceCode).name(collect5.get(provinceCode).getProvinceName()).child(collect4).build();
//                }).collect(Collectors.toList());
//                return RegionConfigVo.builder().id(smallAreaName).code(smallAreaName).name(smallAreaName).child(collect3).build();
//            }).collect(Collectors.toList());
            return RegionConfigVo.builder().id(provinceAreaVos.getBigAreaSale()).code(key).name(provinceAreaVos.getBigAreaSale()).child(collect1).build();
        }).collect(Collectors.toList());

//        //区域列表对象转换
//        List<RegionConfigVo> regionConfigVos1 = regionList.stream().map(e -> {
//            RegionConfigVo regionConfigVo = mapperService.regionEntityConvertVo(e);
//            return regionConfigVo;
//        }).collect(Collectors.toList());
//        //区域父级id
//        final Set<String> regionParentIds = regionList.stream().map(e -> e.getParentId()).collect(Collectors.toSet());
//        regionConfigModel.setParentIds(regionParentIds);
//        List<InsRegionEntity> regionCategoryListHierarchical = regionConfigDao.findRegionCategoryListHierarchical(regionConfigModel);
//        //顶级区域
//        final List<InsRegionEntity> topRegion = regionCategoryListHierarchical.stream().filter(e -> e.getParentId().equalsIgnoreCase("0")).collect(Collectors.toList());
//        //顶级区域分类对象转换
//        List<RegionConfigVo> regionCategoryVo = mapperService.regionCategoryEntityListConvertVoList(topRegion);
//        //下级区域
//        final List<InsRegionEntity> regionEntities = regionCategoryListHierarchical.stream().filter(e -> !e.getParentId().equalsIgnoreCase("0")).collect(Collectors.toList());
//        //下级区域分类对象转换
//        List<RegionConfigVo> regionConfigVos = mapperService.regionCategoryEntityListConvertVoList(regionEntities);
//        regionConfigVos.addAll(regionConfigVos1);
//        //将区域列表根据父级id进行分组
//        Map<String, List<RegionConfigVo>> regionMap = regionConfigVos.stream().collect(Collectors.groupingBy(RegionConfigVo::getParentId));
//
//        this.regionTree(regionCategoryVo, regionMap);

        return regionCategoryVo;
    }

    @Override
    public List<RegionConfigVo> findRegionTreeByProvinceIds(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        //获取区域列表
        List<InsProvinceAreaInfoEntity> regionList = regionConfigDao.findRegionListByProvinceCode(regionConfigModel);
        if(ObjectUtils.isEmpty(regionList)){
            log.info("无区域信息");
            return List.of();
        }
        //大区的集合
        Map<String, List<InsProvinceAreaInfoEntity>> bigAreaMap = regionList.stream().collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getBigAreaSale));

        List<RegionConfigVo> regionCategoryVo = bigAreaMap.entrySet().stream().map(e -> {
            //大区名称
            final String key = e.getKey();
            //大区集合
            final List<InsProvinceAreaInfoEntity> value = e.getValue();
            final InsProvinceAreaInfoEntity provinceAreaVos = value.stream().findAny().get();
            List<RegionConfigVo> collect1 = value.stream().map(k -> {
                final String smallAreaSaleCode = k.getSmallAreaSaleCode();
                final String smallAreaSale = k.getSmallAreaSale();
                return RegionConfigVo.builder().id(smallAreaSaleCode).name(smallAreaSale).code(smallAreaSaleCode).build();
            }).collect(Collectors.toList());
            //小区的集合
//            Map<String, List<InsProvinceAreaInfoEntity>> collect2 = value.stream().filter(k -> ObjectUtils.isNotEmpty(k.getSmallAreaSale())).collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getSmallAreaSaleCode));
//            List<RegionConfigVo> collect1 = collect2.entrySet().stream().map(k -> {
//                //小区名称
//                final String smallAreaName = k.getKey();
//                //小区集合
//                final List<InsProvinceAreaInfoEntity> smallAreaNameList = k.getValue();
//                //省份集合
//                Map<String, List<InsProvinceAreaInfoEntity>> provinceMap = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getProvinceCode));
//                //省份code+名称
//                Map<String, InsProvinceAreaInfoEntity> collect5 = smallAreaNameList.stream().filter(v -> ObjectUtils.isNotEmpty(v.getProvinceCode())).collect(Collectors.toMap(v -> v.getProvinceCode(), v -> v, (k1, k2) -> k2));
//                List<RegionConfigVo> collect3 = provinceMap.entrySet().stream().map(v -> {
//                    //省份code
//                    final String provinceCode = v.getKey();
//                    //省份集合
//                    List<InsProvinceAreaInfoEntity> provinceList = v.getValue();
//                    //城市集合
//                    Map<String, InsProvinceAreaInfoEntity> cityCodeMap = provinceList.stream().filter(l -> ObjectUtils.isNotEmpty(l.getAreaCode())).collect(Collectors.toMap(l -> l.getAreaCode(), l -> l, (k1, k2) -> k2));
//                    List<RegionConfigVo> collect4 = cityCodeMap.entrySet().stream().map(l -> {
//                        return RegionConfigVo.builder().id(l.getValue().getAreaName()).name(l.getValue().getAreaName()).code(l.getValue().getAreaCode()).build();
//                    }).collect(Collectors.toList());
//                    return RegionConfigVo.builder().id(collect5.get(provinceCode).getProvinceName()).code(provinceCode).name(collect5.get(provinceCode).getProvinceName()).child(collect4).build();
//                }).collect(Collectors.toList());
//                return RegionConfigVo.builder().id(smallAreaName).code(smallAreaName).name(smallAreaName).child(collect3).build();
//            }).collect(Collectors.toList());
            return RegionConfigVo.builder().id(provinceAreaVos.getBigAreaSaleCode()).code(key).name(provinceAreaVos.getBigAreaSale()).child(collect1).build();
        }).collect(Collectors.toList());
        return regionCategoryVo;
    }

    @Override
    public List<RegionConfigVo> findRegionTreeByProvinceIds1(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        //获取区域列表
        List<InsProvinceAreaInfoEntity> regionList = regionConfigDao.findRegionListByProvinceCode1(regionConfigModel);
        if(ObjectUtils.isEmpty(regionList)){
            log.info("无区域信息");
            return List.of();
        }
        //大区的集合
        Map<String, Set<InsProvinceAreaInfoEntity>> bigAreaMap = regionList.stream().collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getBigAreaSale,Collectors.toSet()));

        List<RegionConfigVo> regionCategoryVo = bigAreaMap.entrySet().stream().map(e -> {
            //大区名称
            final String key = e.getKey();
            //大区集合
            final Set<InsProvinceAreaInfoEntity> value = e.getValue();
            final InsProvinceAreaInfoEntity provinceAreaVos = value.stream().findAny().get();
            //小区集合
            Map<String, Set<InsProvinceAreaInfoEntity>> collect2 = value.stream().filter(k -> ObjectUtils.isNotEmpty(k.getSmallAreaSale())).collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getSmallAreaSaleCode,Collectors.toSet()));
            List<RegionConfigVo> collect = collect2.entrySet().stream().map(k -> {
                //小区名称
                final String smallAreaName = k.getKey();
                //小区集合
                final Set<InsProvinceAreaInfoEntity> smallAreaNameList = k.getValue();
                InsProvinceAreaInfoEntity insProvinceAreaInfoEntity = smallAreaNameList.stream().findAny().get();
                //专营店集合
                Map<String, Set<InsProvinceAreaInfoEntity>> collect1 = smallAreaNameList.stream().collect(Collectors.groupingBy(InsProvinceAreaInfoEntity::getDealershipName, Collectors.toSet()));
                List<RegionConfigVo> collect3 = new ArrayList();
                collect1.entrySet().stream().forEach(v->{
                    Set<InsProvinceAreaInfoEntity> value1 = v.getValue();
                    List<RegionConfigVo> collect4 = value1.stream().map(l -> RegionConfigVo.builder().id(l.getDealershipName()).name(l.getDealershipName()).code(l.getDealershipName()).build()).collect(Collectors.toList());
                    collect3.addAll(collect4);
                });
                return RegionConfigVo.builder().id(insProvinceAreaInfoEntity.getSmallAreaSaleCode()).name(insProvinceAreaInfoEntity.getSmallAreaSale()).code(insProvinceAreaInfoEntity.getSmallAreaSaleCode()).child(collect3).build();
            }).collect(Collectors.toList());
            return RegionConfigVo.builder().id(provinceAreaVos.getBigAreaSaleCode()).code(key).name(provinceAreaVos.getBigAreaSale()).child(collect).build();
        }).collect(Collectors.toList());
        return regionCategoryVo;
    }

    @Override
    public List<RegionConfigVo> findAllRegionDetail(InsRegionConfigModel regionConfigModel) {
        Assert.hasLength(regionConfigModel.getClientId(),"客户id不允许为空");
        //获取区域列表
        List<InsProvinceAreaInfoEntity> regionList = regionConfigDao.findRegionList(regionConfigModel);
        //区域列表对象转换
//        List<RegionConfigVo> regionConfigVos1 = regionList.stream().map(e -> {
//            RegionConfigVo regionConfigVo = mapperService.regionEntityConvertVo(e);
//            List<ProvinceEntity> region = e.getRegion();
//            String jsonString = JSONArray.toJSONString(region);
//            List<ProvinceModel> list = JSONUtil.toList(jsonString, ProvinceModel.class);
//            regionConfigVo.setRegion(list);
//            return regionConfigVo;
//        }).collect(Collectors.toList());
        return null;
    }


    void regionTree(List<RegionConfigVo> topRegion, Map<String, List<RegionConfigVo>> regionCategoryMap) {
        if (ObjectUtils.isEmpty(topRegion)) {
            return;
        }
        for (RegionConfigVo regionConfigVo : topRegion) {
            List<RegionConfigVo> regionConfigVos = regionCategoryMap.get(regionConfigVo.getId());
            this.regionTree(regionConfigVos, regionCategoryMap);
            regionConfigVo.setChild(regionConfigVos);
        }
    }
}
