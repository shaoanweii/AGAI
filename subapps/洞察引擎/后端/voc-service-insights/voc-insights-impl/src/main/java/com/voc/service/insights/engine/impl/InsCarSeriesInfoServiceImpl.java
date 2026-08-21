package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.clients.IAnalysisDataServiceClient;
import com.voc.service.analysis.model.ModifyDataModel;
import com.voc.service.common.api.IUploadFileService;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.model.UserModel;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.YouBianCodeUtil;
import com.voc.service.insights.engine.api.IInsCarSeriesInfoService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.entity.InsCarSeriesInfoEntity;
import com.voc.service.insights.engine.entity.InsDictInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.listener.CarSeriesExcelListener;
import com.voc.service.insights.engine.mapper.AysLabelPostprocessDataMapper;
import com.voc.service.insights.engine.mapper.InsBrandInfoMapper;
import com.voc.service.insights.engine.mapper.InsCarSeriesInfoMapper;
import com.voc.service.insights.engine.mapper.InsDictMapper;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.model.InsCarSeriesInfoModel;
import com.voc.service.insights.engine.vo.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.voc.service.insights.engine.api.IConditionFilters.*;


/**
 * @author leiww
 * 车系管理
 */
@Service
public class InsCarSeriesInfoServiceImpl extends ServiceImpl<InsCarSeriesInfoMapper, InsCarSeriesInfoEntity> implements IInsCarSeriesInfoService {
    static final String CAR_PACKAGE_PATH = "static/车系/pc";

    static final String CAR_SERIES = "carSeries";

    static final String CAR_SERIES_ALL_LIST = "carSeriesAllList";

    static final String CAR_SERIES_BY_PARAM = "carSeriesByParam";

    private static final String CAR_SERIES_BY_PARAM_TRACKING_KEY = "carSeriesByParamKeys";

    private static final Logger log = LoggerFactory.getLogger(InsCarSeriesInfoServiceImpl.class);
    private static final String CAR_SERIES_KEY = "{}:carSeriesInfo:{}";
    @Autowired
    InsCarSeriesInfoMapper insCarSeriesInfoMapper;
    @Autowired
    InsBrandInfoServiceImpl insBrandInfoService;
    @Autowired
    InsConvertMapperService insConvertMapperService;
    @Autowired
    IUploadFileService uploadFileService;
    @Autowired
    InsDictMapper dictMapper;
    @Autowired
    AysLabelPostprocessDataMapper postprocessDataMapper;
    @Autowired
    private IAnalysisDataServiceClient dataServiceClient;
    @Autowired
    InsBrandInfoMapper brandInfoMapper;

    @Value("${project_id:4cb464bb8f604284dd83c92356fd62a4}")
    String projectId;

    String CAR_SERIES_PACKAGE_PATH = "/insights/car-series";

    @CreateCache(area = "VDP", name = ":", cacheType = CacheType.REMOTE)
    private Cache<String, List<InsCarSeriesInfoModel>> cache;

    @CreateCache(area = "VDP", name = ":", cacheType = CacheType.REMOTE)
    private Cache<String, Set<String>> trackingCache;

    private QueryWrapper<InsCarSeriesInfoEntity> createQueryWrapper(InsCarSeriesInfoModel model) {
        QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<InsCarSeriesInfoEntity> lambdaQueryWrapper = queryWrapper.lambda();
        lambdaQueryWrapper.eq(InsCarSeriesInfoEntity::getStatus, "1");
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInsCarSeriesInfo(InsCarSeriesInfoModel insCarSeriesInfoModel) {
        this.checkParameter(insCarSeriesInfoModel);
        final String id = IdWorker.getId();
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        InsCarSeriesInfoEntity entity = insConvertMapperService.converTo(insCarSeriesInfoModel);
        entity.setId(id);
        entity.setOperator(userName);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateUser(userName);
        entity.setCode(this.codeGenerationRules(insCarSeriesInfoModel.getBrandCode()));
        boolean save = this.save(entity);
        if (save) {
            log.info("车系信息保存成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.SAVE_CAR_SERIES_ERROR);
        }
        if (ObjectUtils.isNotEmpty(insCarSeriesInfoModel.getCompetitiveProduct())) {
            List<CarInfoVo> competitiveProduct = insCarSeriesInfoModel.getCompetitiveProduct();
            Set<String> collect = competitiveProduct.stream().map(CarInfoVo::getId).collect(Collectors.toSet());
            QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(InsCarSeriesInfoEntity::getId, collect);
            List<InsCarSeriesInfoEntity> insCarSeriesInfoEntities = this.list(queryWrapper);
            if (ObjectUtils.isNotEmpty(insCarSeriesInfoEntities)) {
                List<InsCarSeriesInfoEntity> collect1 = insCarSeriesInfoEntities.stream().map(e -> {
                    CarInfoVo build = CarInfoVo.builder()
                            .id(entity.getId())
                            .code(entity.getCode())
                            .name(entity.getName())
                            .build();
                    List<CarInfoVo> competitiveCar = e.getCompetitiveProduct();
                    if (ObjectUtils.isNotEmpty(competitiveCar)) {
                        competitiveCar.add(build);
                    } else {
                        competitiveCar = new ArrayList<>();
                        competitiveCar.add(build);
                    }
                    e.setCompetitiveProduct(competitiveCar);
                    return e;
                }).toList();
                boolean b = this.updateBatchById(collect1);
                if (b) {
                    log.info("更新竞品车系成功");
                } else {
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                }
            } else {
                throw new BussinessException(InsCommonErrorEnum.COMPETITIVE_CAR_SERIES_NOT_EXIST);
            }
        } else {
            log.info("未选择竞品车系");
        }
        cache.remove(this.getCarSeriesKey(CAR_SERIES));
        cache.remove(this.getCarSeriesKey(CAR_SERIES_ALL_LIST));
        this.removeCarSeriesByParamCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInsCarSeriesInfo(InsCarSeriesInfoModel insCarSeriesInfoModel) {
        //单独校验
        Assert.hasLength(insCarSeriesInfoModel.getId(), "ID不能为空");
        this.checkParameter(insCarSeriesInfoModel);
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        InsCarSeriesInfoEntity entity = insConvertMapperService.converTo(insCarSeriesInfoModel);
        entity.setUpdateUser(userName);
        entity.setUpdateTime(LocalDateTime.now());
        if (ObjectUtils.isNotEmpty(insCarSeriesInfoModel.getImg()) && insCarSeriesInfoModel.getImg().startsWith("/")) {
            entity.setImg(null);
        }
        //获取根据id获车系
        QueryWrapper<InsCarSeriesInfoEntity> queryOne = new QueryWrapper<>();
        queryOne.lambda().eq(InsCarSeriesInfoEntity::getId, insCarSeriesInfoModel.getId());
        final InsCarSeriesInfoEntity one = this.getOne(queryOne);

        boolean update = this.updateById(entity);
        if (update) {
            log.info("更新车系信息成功");
        } else {
            throw new BussinessException(InsCommonErrorEnum.UPDATE_CAR_SERIES_ERROR);
        }
        //获取本竞品字段已包含本车系的车系
        QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(InsCarSeriesInfoEntity::getCompetitiveProduct, insCarSeriesInfoModel.getId());
        List<InsCarSeriesInfoEntity> competitiveProduct = this.list(queryWrapper);
        if (ObjectUtils.isNotEmpty(insCarSeriesInfoModel.getCompetitiveProduct())) {
            //本次代更新本竞品信息
            List<CarInfoVo> competitiveProduct1 = insCarSeriesInfoModel.getCompetitiveProduct();
            Set<String> collect = competitiveProduct1.stream().map(CarInfoVo::getId).collect(Collectors.toSet());
            QueryWrapper<InsCarSeriesInfoEntity> wrapper = new QueryWrapper<>();
            wrapper.lambda().in(InsCarSeriesInfoEntity::getId, collect);
            List<InsCarSeriesInfoEntity> insCarSeriesInfoEntities = this.list(wrapper);
            if (ObjectUtils.isEmpty(insCarSeriesInfoEntities)) {
                throw new BussinessException(InsCommonErrorEnum.COMPETITIVE_CAR_SERIES_NOT_EXIST);
            }

            if (ObjectUtils.isNotEmpty(competitiveProduct)) {
                //过滤出不在本次代更新的本竞品车系，删除本竞品字段中本车系的信息
                Set<String> collect1 = insCarSeriesInfoEntities.stream().map(InsCarSeriesInfoEntity::getId).collect(Collectors.toSet());
                List<InsCarSeriesInfoEntity> list = competitiveProduct.stream().filter(e -> !collect1.contains(e.getId())).map(e -> {
                    List<CarInfoVo> competitiveProduct2 = e.getCompetitiveProduct();
                    List<CarInfoVo> list1 = competitiveProduct2.stream().filter(k -> !k.getId().equals(entity.getId())).toList();
                    e.setCompetitiveProduct(list1);
                    return e;
                }).toList();
                if (ObjectUtils.isNotEmpty(list)) {
                    boolean b = this.updateBatchById(list);
                    if (b) {
                        log.info("更新竞品车系成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                    }
                } else {
                    log.info("无新增的本竞品车系");
                }


                //过滤出新的竞品车系，将本车系信息加入到本竞品字段中
                Set<String> collect2 = competitiveProduct.stream().map(e -> e.getId()).collect(Collectors.toSet());
                List<InsCarSeriesInfoEntity> list1 = insCarSeriesInfoEntities.stream().filter(e -> !collect2.contains(e.getId())).map(e -> {
                    List<CarInfoVo> competitiveProduct2 = e.getCompetitiveProduct();
                    CarInfoVo build = CarInfoVo.builder()
                            .id(entity.getId())
                            .code(entity.getCode())
                            .name(entity.getName())
                            .build();
                    if (ObjectUtils.isNotEmpty(competitiveProduct2)) {
                        competitiveProduct2.add(build);
                    } else {
                        competitiveProduct2 = new ArrayList<>();
                        competitiveProduct2.add(build);
                    }
                    e.setCompetitiveProduct(competitiveProduct2);
                    return e;
                }).toList();
                if (ObjectUtils.isNotEmpty(list1)) {
                    boolean b = this.updateBatchById(list1);
                    if (b) {
                        log.info("更新竞品车系成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                    }
                } else {
                    log.info("无新增的本竞品车系");
                }

            } else {
                List<InsCarSeriesInfoEntity> collect1 = insCarSeriesInfoEntities.stream().map(e -> {
                    CarInfoVo build = CarInfoVo.builder()
                            .id(entity.getId())
                            .code(entity.getCode())
                            .name(entity.getName())
                            .build();
                    List<CarInfoVo> competitiveCar = e.getCompetitiveProduct();
                    if (ObjectUtils.isNotEmpty(competitiveCar)) {
                        competitiveCar.add(build);
                    } else {
                        competitiveCar = new ArrayList<>();
                        competitiveCar.add(build);
                    }
                    e.setCompetitiveProduct(competitiveCar);
                    return e;
                }).toList();
                boolean b = this.updateBatchById(collect1);
                if (b) {
                    log.info("更新竞品车系成功");
                } else {
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                }
            }
        } else {
            //删除所有本竞品字段中关联本车系的信息
            if (ObjectUtils.isNotEmpty(competitiveProduct)) {
                List<InsCarSeriesInfoEntity> list = competitiveProduct.stream().map(e -> {
                    if (ObjectUtils.isNotEmpty(e.getCompetitiveProduct())) {
//                        e.setCompetitiveProduct(null);
                        List<CarInfoVo> list1 = e.getCompetitiveProduct().stream().filter(k -> !k.getId().equals(entity.getId())).toList();
                        e.setCompetitiveProduct(list1);
                    }
                    return e;
                }).toList();
                boolean b = this.updateBatchById(list);
                if (b) {
                    log.info("更新竞品车系成功");
                } else {
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                }
            } else {
                log.info("暂无本竞品车系，不做任何处理");
            }
        }
        cache.remove(this.getCarSeriesKey(CAR_SERIES));
        cache.remove(this.getCarSeriesKey(CAR_SERIES_ALL_LIST));
        removeCarSeriesByParamCache();

        //更新结果数据 当是否核心、本竞品类型、品牌、状态任意字段与原值不符时，才会进行数据更新
        if (one.getIsCore().intValue() != insCarSeriesInfoModel.getIsCore().intValue()
                || one.getCompetitiveType().intValue() != insCarSeriesInfoModel.getCompetitiveType().intValue()
                || !one.getBrandCode().equals(insCarSeriesInfoModel.getBrandCode())
                || !one.getStatus().equals(insCarSeriesInfoModel.getStatus())
                || !one.getName().equals(insCarSeriesInfoModel.getName())
        ) {
//            InsCqCaDataQueryModel build = InsCqCaDataQueryModel.builder().carSeries(Collections.singletonList(one.getCode())).build();
//            final List<String> dataIds = postprocessDataMapper.findResultDataIdsByBrandCode(build);
//            if(ObjectUtils.isEmpty(dataIds)){
//                log.warn("未查询到相关车系的结果数据，不作任何处理");
//            }else{
//
//            }
            List<ModifyDataModel.ModifyAttrs> attrsList = new ArrayList<>();
            List<ModifyDataModel.FilterEntity> filters = new ArrayList<>();
            filters.add(ModifyDataModel.FilterEntity.builder().field("car_series_code").value(one.getCode()).build());
            if (one.getIsCore().intValue() != insCarSeriesInfoModel.getIsCore().intValue()) {
                attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("is_core").value(String.valueOf(insCarSeriesInfoModel.getIsCore())).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("is_core").value(String.valueOf(one.getIsCore())).build());
            }
            if (one.getCompetitiveType().intValue() != insCarSeriesInfoModel.getCompetitiveType().intValue()) {
                attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("competitive_type").value(String.valueOf(insCarSeriesInfoModel.getCompetitiveType())).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("competitive_type").value(String.valueOf(one.getCompetitiveType())).build());
            }
            if (!one.getBrandCode().equals(insCarSeriesInfoModel.getBrandCode())) {
                attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("brand_code").value(insCarSeriesInfoModel.getBrandCode()).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("brand_code").value(one.getBrandCode()).build());
                QueryWrapper<InsBrandInfoEntity> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.lambda().eq(InsBrandInfoEntity::getCode, insCarSeriesInfoModel.getBrandCode());
                InsBrandInfoEntity brandInfoEntity = brandInfoMapper.selectOne(queryWrapper1);
                attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("brand_name").value(brandInfoEntity.getName()).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("brand_name").value(one.getBrandName()).build());

            }
            if (!one.getStatus().equals(insCarSeriesInfoModel.getStatus())) {
                attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("abandon").value("1".equals(insCarSeriesInfoModel.getStatus()) ? "0" : "1").build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("abandon").value(one.getStatus()).build());
            }
            if (!one.getName().equals(insCarSeriesInfoModel.getName())) {
                attrsList.add(ModifyDataModel.ModifyAttrs.builder().field("car_series_name").value(insCarSeriesInfoModel.getName()).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("car_series_name").value(one.getName()).build());
            }
            ServiceContextHolder.getExecutor().execute(() -> {
                try {
                    this.modifyResultData(filters, attrsList);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            log.info("未修改关键字段，不调用接口更新结果表数据");
        }

    }

    @Override
    public void delInsCarSeriesInfo(InsCarSeriesInfoModel model) {
        Assert.notNull(model.getId(), "id不能为空");
        insCarSeriesInfoMapper.delInsCarSeriesInfoEntity(model.getId());
        cache.remove(this.getCarSeriesKey(CAR_SERIES));
        cache.remove(this.getCarSeriesKey(CAR_SERIES_ALL_LIST));
        removeCarSeriesByParamCache();
    }

    @Override
    public List<InsCarSeriesInfoModel> queryByParam(InsCarSeriesInfoModel insCarSeriesInfoModel) {
        String key = this.getCarSeriesKey(CAR_SERIES_BY_PARAM, SecureUtil.md5(JSONObject.toJSONString(insCarSeriesInfoModel)));
        List<InsCarSeriesInfoModel> carSeriesList = cache.get(key);
        if (ObjectUtils.isNotEmpty(carSeriesList)) {
            return carSeriesList;
        }
        List<InsCarSeriesInfoEntity> list = insCarSeriesInfoMapper.selectMultiInsCarSeriesInfoEntity(insCarSeriesInfoModel);
        List<InsCarSeriesInfoModel> modelList = list.stream().map(e -> insConvertMapperService.converTo(e)).collect(Collectors.toList());
        cache.put(key, modelList);
        String trackingKey = this.getCarSeriesKey(CAR_SERIES_BY_PARAM_TRACKING_KEY);
        Set<String> trackedKeys = trackingCache.get(trackingKey);
        if (trackedKeys == null) {
            trackedKeys = new HashSet<>();
        }
        trackedKeys.add(key);
        trackingCache.put(trackingKey, trackedKeys);
        return modelList;
    }


    @Override
    public Result<?> queryBySelect(InsCarSeriesInfoModel model) {
        IPage<InsCarSeriesInfoEntity> page = new Page<>(model.getPageNum(), model.getPageSize());
        IPage<InsCarSeriesInfoEntity> insCarSeriesInfoList = this.baseMapper.findInsCarSeriesInfoList(page, model);
        IPage<InsCarSeriesVo> pages = new Page<>();
        if (ObjectUtils.isEmpty(insCarSeriesInfoList.getRecords())) {
            log.info("暂无车系数据");
        } else {
            pages.setSize(insCarSeriesInfoList.getSize());
            pages.setCurrent(insCarSeriesInfoList.getCurrent());
            pages.setTotal(insCarSeriesInfoList.getTotal());
            List<InsCarSeriesInfoEntity> records = insCarSeriesInfoList.getRecords();
            final List<InsDictInfoEntity> dictInfoList = dictMapper.findDictInfoList();
            Map<String, List<InsDictInfoEntity>> collect = dictInfoList.stream().collect(Collectors.groupingBy(InsDictInfoEntity::getTypeCode));
            List<InsCarSeriesVo> carSeriesVos = insConvertMapperService.carSeriesEntityConvertToCarSeriesVoList(records);
            carSeriesVos.stream().forEach(e -> {
                if (collect.containsKey(IS_CORE)) {
                    List<InsDictInfoEntity> insDictInfoEntities = collect.get(IS_CORE);
                    InsDictInfoEntity insDictInfoEntity1 = insDictInfoEntities.stream().filter(insDictInfoEntity -> insDictInfoEntity.getClassifyCode().equals(String.valueOf(e.getIsCore()))).findAny().orElse(null);
                    if (ObjectUtils.isNotEmpty(insDictInfoEntity1)) {
                        e.setIsCoreName(insDictInfoEntity1.getClassifyName());
                    }
                }
                if (collect.containsKey(COMPETITIVE_TYPE)) {
                    List<InsDictInfoEntity> insDictInfoEntities = collect.get(COMPETITIVE_TYPE);
                    InsDictInfoEntity insDictInfoEntity1 = insDictInfoEntities.stream().filter(insDictInfoEntity -> insDictInfoEntity.getClassifyCode().equals(String.valueOf(e.getCompetitiveType()))).findAny().orElse(null);
                    if (ObjectUtils.isNotEmpty(insDictInfoEntity1)) {
                        e.setCompetitiveTypeName(insDictInfoEntity1.getClassifyName());
                    }
                }
                if (collect.containsKey(STATUS)) {
                    List<InsDictInfoEntity> insDictInfoEntities = collect.get(STATUS);
                    InsDictInfoEntity insDictInfoEntity1 = insDictInfoEntities.stream().filter(insDictInfoEntity -> insDictInfoEntity.getClassifyCode().equals(e.getStatus())).findAny().orElse(null);
                    if (ObjectUtils.isNotEmpty(insDictInfoEntity1)) {
                        e.setStatusName(insDictInfoEntity1.getClassifyName());
                    }
                }
                if (collect.containsKey(IS_NEW_CAR)) {
                    List<InsDictInfoEntity> insDictInfoEntities = collect.get(IS_NEW_CAR);
                    InsDictInfoEntity insDictInfoEntity1 = insDictInfoEntities.stream().filter(insDictInfoEntity -> insDictInfoEntity.getClassifyCode().equals(String.valueOf(e.getIsNewCar()))).findAny().orElse(null);
                    if (ObjectUtils.isNotEmpty(insDictInfoEntity1)) {
                        e.setIsNewCarName(insDictInfoEntity1.getClassifyName());
                    }
                }
            });


            pages.setRecords(carSeriesVos);
        }
        return Result.OK(pages);
    }

    @Override
    public InsCarSeriesVo findCarSeriesInfo(InsCarSeriesInfoModel model) {
        Assert.hasLength(model.getId(), "id不能为空");
        QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsCarSeriesInfoEntity::getId, model.getId());
        InsCarSeriesInfoEntity insCarSeriesInfoEntity = this.baseMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(insCarSeriesInfoEntity)) {
            throw new BussinessException(InsCommonErrorEnum.CAR_SERIES_NOT_EXIST);
        }
        InsCarSeriesVo insCarSeriesVo = insConvertMapperService.CarSeriesEntityConvertToVo(insCarSeriesInfoEntity);
        if (ObjectUtils.isNotEmpty(insCarSeriesVo.getImg())) {
            final String url = uploadFileService.getObjectUrl(CAR_PACKAGE_PATH.concat("/").concat(insCarSeriesVo.getImg()));
            String imgUrl = "/files".concat(url.substring(url.indexOf("resource") - 1));
            insCarSeriesVo.setImg(imgUrl);
        }

        return insCarSeriesVo;
    }

    @Override
    public void batchChangeStatus(InsCarSeriesInfoModel model) {
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIds()), "id不能为空");
        Assert.hasLength(model.getStatus(), "状态不能为空");
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        UpdateWrapper<InsCarSeriesInfoEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsCarSeriesInfoEntity::getId, model.getIds());
        updateWrapper.lambda().set(InsCarSeriesInfoEntity::getStatus, model.getStatus());
        updateWrapper.lambda().set(InsCarSeriesInfoEntity::getUpdateUser, userName);
        updateWrapper.lambda().set(InsCarSeriesInfoEntity::getUpdateTime, LocalDateTime.now());
        boolean update = this.update(updateWrapper);
        if (update) {
            log.info("批量更新车系状态成功");
            cache.remove(this.getCarSeriesKey(CAR_SERIES));
            cache.remove(this.getCarSeriesKey(CAR_SERIES_ALL_LIST));
            removeCarSeriesByParamCache();
        } else {
            throw new BussinessException(InsCommonErrorEnum.BATCH_CHANGE_STATUS_ERROR);
        }

        //更新结果数据
        QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(InsCarSeriesInfoEntity::getId, model.getIds());
        queryWrapper.lambda().select(InsCarSeriesInfoEntity::getCode);
        final List<InsCarSeriesInfoEntity> list = this.list(queryWrapper);
        final List<ModifyDataModel.FilterEntity> filters = list.stream()
                .map(InsCarSeriesInfoEntity::getCode)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .map(e -> ModifyDataModel.FilterEntity.builder().field("car_series_code").value(e).build())
                .collect(Collectors.toList());
        if (ObjectUtils.isEmpty(filters)) {
            log.warn("未查询到相关车系编码，不作结果数据推送");
            return;
        }
//        InsCqCaDataQueryModel build = InsCqCaDataQueryModel.builder().carSeries(collect).build();
//        final List<String> dataIds = postprocessDataMapper.findResultDataIdsByBrandCode(build);
//        if(ObjectUtils.isEmpty(dataIds)){
//            log.warn("未查询到相关品牌的结果数据，不作任何处理");
//        }else{
//
//        }
        ModifyDataModel.ModifyAttrs arrts = ModifyDataModel.ModifyAttrs.builder().field("abandon").value("1".equals(model.getStatus()) ? "0" : "1").build();
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.modifyResultData(filters, Collections.singletonList(arrts));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }


    @Override
    public List<InsCarSeriesInfoModel> findAll() {
        String key = this.getCarSeriesKey(CAR_SERIES_ALL_LIST);
        List<InsCarSeriesInfoModel> cached = cache.get(key);
        if (ObjectUtils.isNotEmpty(cached)) {
            log.info("使用缓存数据，数据大小：{}", cached.size());
            return cached;
        }
        log.info("缓存未命中，查询实时数据");
        final List<InsCarSeriesInfoEntity> entityList = list(createQueryWrapper(new InsCarSeriesInfoModel()));
        if (ObjectUtils.isEmpty(entityList)) {
            log.warn("暂无车系数据");
            return null;
        }
        final List<InsCarSeriesInfoModel> list_ = entityList.stream()
                .map(e -> insConvertMapperService.converTo(e))
                .collect(Collectors.toList());
        log.info("数据大小：{}", list_.size());
        cache.put(key, list_);
        return list_;
    }

    @Override
    public List<BrandInfoVo> findBrandCarsTree() {
        List<InsBrandInfoModel> brandInfo = insBrandInfoService.findAll();
        if (ObjectUtils.isEmpty(brandInfo)) {
            log.warn("暂无品牌数据");
            return List.of();
        }
        List<BrandInfoVo> brandInfoVos = brandInfo.stream().map(e -> {
            return BrandInfoVo.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .code(e.getCode())
                    .build();
        }).toList();
        List<InsCarSeriesInfoModel> carsInfo = this.findAll();
        if (ObjectUtils.isEmpty(carsInfo)) {
            log.warn("暂无车系数据");
            return brandInfoVos;
        }

        Map<String, List<InsCarSeriesInfoModel>> collect = carsInfo.stream().collect(Collectors.groupingBy(InsCarSeriesInfoModel::getBrandId));
        brandInfoVos.stream().forEach(e -> {
            final String code = e.getId();
            if (collect.containsKey(code)) {
                List<CarInfoVo> carInfoVos = insConvertMapperService.carConvertToVo(collect.get(code));
                e.setCars(carInfoVos);
            }
        });
        return brandInfoVos;
    }

    @Override
    public List<BrandInfoVo> findSelfBrandCarsTree() {
        List<InsBrandInfoModel> brandInfo = insBrandInfoService.findSelfBrand();
        if (ObjectUtils.isEmpty(brandInfo)) {
            log.warn("暂无品牌数据");
            return List.of();
        }
        List<BrandInfoVo> brandInfoVos = brandInfo.stream().map(e -> {
            return BrandInfoVo.builder()
                    .id(e.getId())
                    .name(e.getName())
                    .code(e.getCode())
                    .build();
        }).toList();
        List<InsCarSeriesInfoModel> carsInfo = this.findAll();
        if (ObjectUtils.isEmpty(carsInfo)) {
            log.warn("暂无车系数据");
            return brandInfoVos;
        }

        Map<String, List<InsCarSeriesInfoModel>> collect = carsInfo.stream().collect(Collectors.groupingBy(InsCarSeriesInfoModel::getBrandId));
        brandInfoVos.stream().forEach(e -> {
            final String code = e.getId();
            if (collect.containsKey(code)) {
                List<CarInfoVo> carInfoVos = insConvertMapperService.carConvertToVo(collect.get(code));
                e.setCars(carInfoVos);
            }
        });
        return brandInfoVos;
    }

    @Override
    public List<CarInfoVo> findCarSeriesByIds(InsCarSeriesInfoModel model) {
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getCodes()), "code编码集不能为空");
        List<InsCarSeriesInfoModel> carsInfo = this.findAll();
        if (ObjectUtils.isEmpty(carsInfo)) {
            log.warn("暂无车系数据");
            return List.of();
        }
        List<InsCarSeriesInfoModel> collect = carsInfo.stream().filter(e -> model.getCodes().contains(e.getCode())).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(collect)) {
            log.warn("暂未匹配到车系数据");
            return List.of();
        }
        List<CarInfoVo> carInfoVos = insConvertMapperService.carConvertToVo(collect);
        return carInfoVos;
    }

    @Override
    public List<CarSeriesTreeVo> findBrandCarSeriesByCarName(List<String> carName) {
        Assert.isTrue(ObjectUtils.isNotEmpty(carName), "车系名称不允许为空");
        QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(InsCarSeriesInfoEntity::getName, carName);
        List<InsCarSeriesInfoEntity> entityList = this.list(queryWrapper);
        if (ObjectUtils.isNotEmpty(entityList)) {
            Map<String, List<InsCarSeriesInfoEntity>> collect = entityList.stream().collect(Collectors.groupingBy(InsCarSeriesInfoEntity::getBrandId));
            List<CarSeriesTreeVo> collect2 = collect.entrySet().stream().map(e -> {
                final String key = e.getKey(); // 品牌id
                InsBrandInfoModel insBrandInfoModel = insBrandInfoService.queryByCode(key);
                if (ObjectUtils.isNotEmpty(insBrandInfoModel)) {
                    final List<InsCarSeriesInfoEntity> value = e.getValue();
                    List<CarSeriesTreeVo> collect1 = value.stream().map(k -> {
                        return CarSeriesTreeVo.builder().name(k.getName()).code(k.getCode()).value(k.getName()).build();
                    }).collect(Collectors.toList());
                    return CarSeriesTreeVo.builder()
                            .name(insBrandInfoModel.getName())
                            .code(insBrandInfoModel.getCode())
                            .value(insBrandInfoModel.getName())
                            .child(collect1)
                            .build();
                }
                return null;
            }).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            return collect2;
        }
        return List.of();
    }

    @Override
    public void uploadExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), CarSeriesTemplateVo.class, new CarSeriesExcelListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void analyzeExcelData(List<CarSeriesTemplateVo> list) {
        list.stream().forEach(e -> {
//            final String brandName = e.getBrandName();
//            BrandInfoVo brandByBrandName = insBrandInfoService.findBrandByBrandName(brandName, true);
            //更新品牌本竞品
//            if(ObjectUtils.isNotEmpty(brandByBrandName)){
//                final String competitive = e.getCompetitive();
//                BrandInfoVo brandByBrandName1 = insBrandInfoService.findBrandByBrandName(competitive,false );
//
//                List<BrandInfoVo> competitiveProduct = brandByBrandName.getCompetitiveProduct();
//                if(ObjectUtils.isNotEmpty(competitiveProduct)){
//                    competitiveProduct.add(brandByBrandName1);
//                }else{
//                    competitiveProduct = new ArrayList<>();
//                    competitiveProduct.add(brandByBrandName1);
//                }
//                InsBrandInfoEntity brandEntity = new InsBrandInfoEntity();
//                brandEntity.setId(brandByBrandName.getId());
//                brandEntity.setIsCore(1);
//                brandEntity.setCompetitiveProduct(competitiveProduct);
//                insBrandInfoService.getBaseMapper().updateById(brandEntity);
//            }

            //更新车系本竞品
//            QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
//            queryWrapper.lambda().eq(InsCarSeriesInfoEntity::getName, brandName);
//            final List<InsCarSeriesInfoEntity> insCarSeriesInfoModel = this.list(queryWrapper);
//            if(ObjectUtils.isNotEmpty(insCarSeriesInfoModel)){
//                insCarSeriesInfoModel.stream().forEach(car->{
//                    final String competitive = e.getCompetitive();
//                    QueryWrapper<InsCarSeriesInfoEntity> wrapper = new QueryWrapper<>();
//                    wrapper.lambda().eq(InsCarSeriesInfoEntity::getName, competitive);
//                    final List<InsCarSeriesInfoEntity> insCarSeriesInfoModel1 = this.list(wrapper);
//                    if(ObjectUtils.isNotEmpty(insCarSeriesInfoModel1)){
//                        insCarSeriesInfoModel1.stream().forEach(k->{
//                            CarInfoVo build = CarInfoVo.builder().id(k.getId())
//                                    .code(k.getCode())
//                                    .name(k.getName())
//                                    .build();
//                            List<CarInfoVo> competitiveProduct = car.getCompetitiveProduct();
//                            if(ObjectUtils.isNotEmpty(competitiveProduct)){
//                                competitiveProduct.add( build);
//                            }else{
//                                competitiveProduct = new ArrayList<>();
//                                competitiveProduct.add( build);
//                            }
//                            car.setCompetitiveProduct(competitiveProduct);
//                            this.updateById(car);
//                        });
//                    }
//                });
//
//
//            }

            //新增或更新品牌信息
//            String brandId = null;
//            String brandCode = null;
//            if(ObjectUtils.isEmpty(brandByBrandName)){
//                brandId = IdWorker.getId();
//                brandCode = insBrandInfoService.codeGenerationRules();
//                InsBrandInfoEntity insBrandInfoModel = new InsBrandInfoEntity();
//                insBrandInfoModel.setId(brandId);
//                insBrandInfoModel.setCode(brandCode);
//                insBrandInfoModel.setDelFlag(false);
//                insBrandInfoModel.setCreateTime(LocalDateTime.now());
//                insBrandInfoModel.setUpdateTime(LocalDateTime.now());
//                insBrandInfoModel.setName(brandName);
////                insBrandInfoModel.setCompetitiveType(e.getCompetitiveType());
////                insBrandInfoModel.setImg(ObjectUtils.isNotEmpty(e.getBrandImage())?e.getBrandImage():null);
////                if(ObjectUtils.isNotEmpty(e.getBrandAlias())){
////                    String brandAlias = e.getBrandAlias();
////                    String replace = brandAlias.replace('|', ',');
////                    insBrandInfoModel.setAlias(replace);
////                }
////                insBrandInfoModel.setAutomark(ObjectUtils.isNotEmpty(e.getFactory())?e.getFactory():null);
//                insBrandInfoService.save(insBrandInfoModel);
//            }else{
//                brandId = brandByBrandName.getId();
//                brandCode = brandByBrandName.getCode();
//            }

            //新增或更新车系信息
//            String carSeries = e.getCarSeries();
//            QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
//            queryWrapper.lambda().eq(InsCarSeriesInfoEntity::getName, carSeries);
////            queryWrapper.lambda().eq(InsCarSeriesInfoEntity::getBrandId, brandId);
//            final InsCarSeriesInfoEntity insCarSeriesInfoModel = this.getOne(queryWrapper);
//            if(ObjectUtils.isEmpty(insCarSeriesInfoModel)){
//                InsCarSeriesInfoEntity carSeriesInfoEntity = new InsCarSeriesInfoEntity();
//                carSeriesInfoEntity.setBrandId(brandId);
//                carSeriesInfoEntity.setName(carSeries);
////                carSeriesInfoEntity.setCompetitiveType(e.getCompetitiveType());
//                carSeriesInfoEntity.setBrandCode(brandCode);
//                carSeriesInfoEntity.setCode(this.codeGenerationRules(brandCode));
//                carSeriesInfoEntity.setDelFlag(false);
//                carSeriesInfoEntity.setCreateTime(LocalDateTime.now());
//                carSeriesInfoEntity.setUpdateTime(LocalDateTime.now());
//                String s = "";
//                if(ObjectUtils.isNotEmpty(e.getAlias1())){
//                    s+=e.getAlias1()+"|";
//                }
////                if(ObjectUtils.isNotEmpty(e.getAlias2())){
////                    s+=e.getAlias2()+"|";
////                }
////                if(ObjectUtils.isNotEmpty(e.getAlias3())){
////                    s+=e.getAlias3()+"|";
////                }
//                if(s.endsWith("|")){
//                    s = s.substring(0, s.length()-1);
//                }
//                if(StrUtil.isNotBlank(s)){
//                    if(s.contains(",")){
//                        s = s.replace(",", "|");
//                    }
//                    String[] split = s.split("\\|");
//                    Set<String> set = new HashSet<>();
//                    for (int i = 0; i < split.length; i++) {
//                        set.add(split[i]);
//                    }
//                    String join = String.join(",", set);
//                    carSeriesInfoEntity.setAlias(join);
//                }
////                carSeriesInfoEntity.setCarLevel1(ObjectUtils.isNotEmpty(e.getCarType())?e.getCarType():null);
////                carSeriesInfoEntity.setCarLevel2(ObjectUtils.isNotEmpty(e.getCarTypeLevel())?e.getCarTypeLevel():null);
////                carSeriesInfoEntity.setEnergyType1(ObjectUtils.isNotEmpty(e.getEnergyType1())?e.getEnergyType1():null);
////                carSeriesInfoEntity.setFactory(ObjectUtils.isNotEmpty(e.getFactory())?e.getFactory():null);
////                carSeriesInfoEntity.setCountry(ObjectUtils.isNotEmpty(e.getCountry())?e.getCountry():null);
//                this.save(carSeriesInfoEntity);
//            }else{
//                log.info("车系已存在,更新车系");
//                insCarSeriesInfoModel.setBrandId(brandId);
//                insCarSeriesInfoModel.setBrandCode(brandCode);
//                if(ObjectUtils.isNotEmpty(e.getAlias1())){
//                    insCarSeriesInfoModel.setAlias(e.getAlias1());
//                }
//                if(ObjectUtils.isNotEmpty(e.getCarSeriesExclusionWords())){
//                    insCarSeriesInfoModel.setExclusionWords(e.getCarSeriesExclusionWords());
//                }
//                insCarSeriesInfoModel.setUpdateTime(LocalDateTime.now());
//                this.updateById(insCarSeriesInfoModel);
//            }
            UpdateWrapper<InsCarSeriesInfoEntity> queryWrapper = new UpdateWrapper<>();
            queryWrapper.lambda().eq(InsCarSeriesInfoEntity::getCode, e.getCode());
            queryWrapper.lambda().set(InsCarSeriesInfoEntity::getFactory, e.getFactory());
            queryWrapper.lambda().set(InsCarSeriesInfoEntity::getCarLevel1, e.getCarLevel1());
            boolean update = this.update(queryWrapper);
            if (update) {
                log.info("更新车系成功");
            } else {
                log.info("更新车系失败");
                throw new RuntimeException("更新车系失败");
            }
        });
    }

    @Override
    public NewCarSeriesConditionVo getNewCarSeriesCondition() {
        log.info("开始查询新车上市车系筛选条件");

        // 1. 查询所有符合条件的品牌（自有+竞品）
        List<InsBrandInfoEntity> allBrandList = new LambdaQueryChainWrapper<>(insBrandInfoService.getBaseMapper())
                .eq(InsBrandInfoEntity::getStatus, "1")
                .and(wrapper ->
                        wrapper.eq(InsBrandInfoEntity::getCompetitiveType, 1)
                                .or()
                                .eq(InsBrandInfoEntity::getCompetitiveType, 2)
                )
                .orderByAsc(InsBrandInfoEntity::getOrderBy)
                .list();

        if (CollUtil.isEmpty(allBrandList)) {
            log.warn("暂无品牌数据");
            return NewCarSeriesConditionVo.builder().build();
        }

        // 分离自有品牌和竞品品牌
        List<InsBrandInfoEntity> selfBrandList = allBrandList.stream()
                .filter(e -> e.getCompetitiveType() == 1)
                .toList();

        // 2. 查询所有符合条件的车系
        List<InsCarSeriesInfoEntity> allSeriesList = new LambdaQueryChainWrapper<>(baseMapper)
                .eq(InsCarSeriesInfoEntity::getDelFlag, 0)
                .eq(InsCarSeriesInfoEntity::getIsNewCar, 1)
                .and(wrapper ->
                        wrapper.eq(InsCarSeriesInfoEntity::getCompetitiveType, 1)
                                .or()
                                .eq(InsCarSeriesInfoEntity::getCompetitiveType, 2)
                )
                .orderByAsc(InsCarSeriesInfoEntity::getOrderBy)
                .list();

        if (CollUtil.isNotEmpty(allSeriesList)) {
            Map<String, InsCarSeriesInfoEntity> coreSeriesMap = allSeriesList.stream()
                    .filter(e -> Objects.equals(e.getIsCore(), 1))
                    .collect(Collectors.toMap(
                            InsCarSeriesInfoEntity::getCode,
                            e -> e,
                            (o1, o2) -> o1
                    ));
            List<String> seriesCode = new ArrayList<>(coreSeriesMap.keySet());

            for (InsCarSeriesInfoEntity carSeries : allSeriesList) {
                List<CarInfoVo> competitiveProduct = carSeries.getCompetitiveProduct();
                if (CollUtil.isNotEmpty(competitiveProduct)) {
                    List<CarInfoVo> filteredCompetitiveProduct = competitiveProduct.stream()
                            .filter(car -> seriesCode.contains(car.getCode()))
                            .toList();
                    if (CollUtil.isNotEmpty(filteredCompetitiveProduct)) {
                        for (CarInfoVo carInfoVo : filteredCompetitiveProduct) {
                            InsCarSeriesInfoEntity entity = coreSeriesMap.get(carInfoVo.getCode());
                            if (entity != null) {
                                carInfoVo.setPreheatStartTime(entity.getPreheatStartTime());
                                carInfoVo.setPreheatEndTime(entity.getPreheatEndTime());
                                carInfoVo.setLaunchStartTime(entity.getLaunchStartTime());
                                carInfoVo.setLaunchEndTime(entity.getLaunchEndTime());
                                carInfoVo.setStableStartTime(entity.getStableStartTime());
                                carInfoVo.setStableEndTime(entity.getStableEndTime());
                            }
                        }
                    }
                    carSeries.setCompetitiveProduct(filteredCompetitiveProduct);
                }
            }
        }

        List<InsCarSeriesInfoEntity> selfSeriesList = CollUtil.isEmpty(allSeriesList) ? List.of() :
                allSeriesList.stream()
                        .filter(e -> e.getCompetitiveType() == 1)
                        .toList();
        final String defaultSeriesId = selfBrandList.stream()
                .map(InsBrandInfoEntity::getCode)
                .flatMap(code -> selfSeriesList.stream()
                        .filter(e -> StrUtil.equals(e.getBrandCode(), code))
                        .filter(e -> e.getUpdateTime() != null)
                        .sorted(Comparator.comparing(InsCarSeriesInfoEntity::getUpdateTime).reversed())
                        .map(InsCarSeriesInfoEntity::getId)
                        .findFirst()
                        .stream())
                .findFirst()
                .orElse(null);

        // 3. 按品牌ID分组
        Map<String, List<CarInfoVo>> carSeriesMap = CollUtil.isEmpty(allSeriesList) ? Map.of() :
                allSeriesList.stream()
                        .map(e -> insConvertMapperService.converTo(e))
                        .collect(Collectors.groupingBy(InsCarSeriesInfoModel::getBrandId))
                        .entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> insConvertMapperService.carConvertToVo(e.getValue())
                        ));

        // 4. 构建对比车系（自有+竞品）
        List<BrandInfoVo> compareCarSeries = buildBrandInfoVoList(allBrandList, carSeriesMap);

        // 5. 构建新品车系（仅自有）
        Map<String, List<CarInfoVo>> selfCarSeriesMap = CollUtil.isEmpty(selfSeriesList) ? Map.of() :
                selfSeriesList.stream()
                        .map(e -> insConvertMapperService.converTo(e))
                        .collect(Collectors.groupingBy(InsCarSeriesInfoModel::getBrandId))
                        .entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> insConvertMapperService.carConvertToVo(e.getValue()).stream()
                                        .peek(car -> car.setIsDefault(StrUtil.equals(car.getId(), defaultSeriesId)))
                                        .toList()
                        ));
        List<BrandInfoVo> newCarSeries = buildBrandInfoVoList(selfBrandList, selfCarSeriesMap);

        log.info("新车上市车系筛选条件查询完成，新品车系数量：{}，对比车系数量：{}",
                newCarSeries.size(), compareCarSeries.size());

        return NewCarSeriesConditionVo.builder()
                .newCarSeries(newCarSeries)
                .compareCarSeries(compareCarSeries)
                .build();
    }

    /**
     * 构建品牌信息列表（只包含有车系的品牌）
     *
     * @param brandList 品牌列表
     * @param carSeriesMap 车系Map（key: 品牌ID, value: 车系列表）
     * @return 品牌信息列表
     */
    private List<BrandInfoVo> buildBrandInfoVoList(List<InsBrandInfoEntity> brandList,
                                                   Map<String, List<CarInfoVo>> carSeriesMap) {
        return brandList.stream()
                .filter(e -> carSeriesMap.containsKey(e.getId()))
                .map(e -> {
                    BrandInfoVo vo = BrandInfoVo.builder()
                            .id(e.getId())
                            .name(e.getName())
                            .code(e.getCode())
                            .build();
                    vo.setCars(carSeriesMap.get(e.getId()));
                    return vo;
                })
                .toList();
    }

    protected void checkParameter(InsCarSeriesInfoModel model) {
        Assert.hasLength(model.getName(), "名称不能为空");
        Assert.isTrue(!model.getName().isEmpty() && model.getName().length() <= 20, "车系名称不能超过20个字符");
        Assert.hasLength(model.getBrandId(), "归属品牌不能为空");
        Assert.isTrue(!StrUtil.isNotBlank(model.getNameEn()) || model.getNameEn().length() <= 20, "车系英文名称不能超过20个字符");
        Assert.hasLength(model.getImg(), "图片不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIsCore()), "是否核心不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIsNewCar()), "是否新车不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getCompetitiveType()), "本竞品类型不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getStatus()), "状态不允许为空");
        QueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsCarSeriesInfoEntity::getName, model.getName());
        if (ObjectUtils.isNotEmpty(model.getId())) {
            queryWrapper.lambda().notIn(InsCarSeriesInfoEntity::getId, model.getId());
        }
        InsCarSeriesInfoEntity one = this.getOne(queryWrapper);
        Assert.isTrue(ObjectUtils.isEmpty(one), "车系名称不允许重复");
    }

    @Override
    public String codeGenerationRules(String brandId) {
        //找同类 确定上一个最大的code值
        LambdaQueryWrapper<InsCarSeriesInfoEntity> query = new LambdaQueryWrapper<InsCarSeriesInfoEntity>()
                .eq(InsCarSeriesInfoEntity::getBrandCode, brandId)
                .isNotNull(InsCarSeriesInfoEntity::getCode)
//                .eq(InsCarSeriesInfoEntity::getDelFlag, false)
                .orderByDesc(InsCarSeriesInfoEntity::getCode);
        List<InsCarSeriesInfoEntity> list = this.baseMapper.selectList(query);
        return YouBianCodeUtil.getSubYouBianCode(brandId,
                CollUtil.isNotEmpty(list) && StrUtil.isNotBlank(list.get(0).getCode()) ? list.get(0).getCode() : null,
                this::existsCarSeriesCode);
    }

    private boolean existsCarSeriesCode(String code) {
        return this.count(new LambdaQueryWrapper<InsCarSeriesInfoEntity>()
                .eq(InsCarSeriesInfoEntity::getCode, code)) > 0;
    }

    private String getCarSeriesKey(String... params) {
        String base = StrUtil.format(CAR_SERIES_KEY, ServiceContextHolder.getSystemId(), params[0]);
        if (params.length > 1) {
            StringBuilder sb = new StringBuilder(base);
            for (int i = 1; i < params.length; i++) {
                sb.append(":").append(params[i]);
            }
            return sb.toString();
        }
        return base;
    }

    private void removeCarSeriesByParamCache() {
        String trackingKey = this.getCarSeriesKey(CAR_SERIES_BY_PARAM_TRACKING_KEY);
        Set<String> keys = trackingCache.get(trackingKey);
        if (keys != null && !keys.isEmpty()) {
            keys.forEach(k -> cache.remove(k));
            trackingCache.remove(trackingKey);
            log.info("按前缀删除carSeriesByParam缓存, 删除{}个key", keys.size());
        }
    }

    private String getFileName(String name) {
        return CAR_SERIES_PACKAGE_PATH.concat("/").concat(name);
    }

    private boolean startsWithHttpOrHttps(String url) {
        Pattern pattern = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.find(); // find 从开头匹配，因为 ^ 已经限定开头
    }


    private void modifyResultData(List<ModifyDataModel.FilterEntity> filters, List<ModifyDataModel.ModifyAttrs> resultDataModel) {
        final String id = IdWorker.getId();
        ModifyDataModel build = ModifyDataModel.builder().requestId(id).type(2).attrs(resultDataModel).filters(filters).build();
        log.info("开始推送,请求id:{},入参:{}", id, build);
        Result<?> result = dataServiceClient.modifyResultdata(build);
        if (ObjectUtils.isEmpty(result) || !"200".equals(result.getCode()) || ObjectUtils.isEmpty(result.getResult())) {
            log.error("调用数据清洗服务更新[id:{}]车系相关数据接口异常", id);
        } else {
            log.info("调用数据清洗服务更新车系相关数据成功");
        }
    }
}
