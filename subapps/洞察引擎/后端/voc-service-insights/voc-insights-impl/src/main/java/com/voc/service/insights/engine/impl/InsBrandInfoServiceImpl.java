package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.voc.service.components.minio.config.MinioConfig;
import com.voc.service.insights.engine.api.IInsBrandInfoService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.common.util.QueryUtil;
import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.entity.InsCarSeriesInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.AysLabelPostprocessDataMapper;
import com.voc.service.insights.engine.mapper.InsBrandInfoMapper;
import com.voc.service.insights.engine.mapper.InsCarSeriesInfoMapper;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.BrandInfoVo;
import com.voc.service.insights.engine.vo.InsALlBrandAndCarSeriesVo;
import com.voc.service.insights.engine.vo.InsBrandInfoVo;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InsBrandInfoServiceImpl extends ServiceImpl<InsBrandInfoMapper, InsBrandInfoEntity> implements IInsBrandInfoService {
    private static final Logger log = LoggerFactory.getLogger(InsBrandInfoServiceImpl.class);
    @Autowired
    InsBrandInfoMapper insBrandInfoMapper;
    @Autowired
    InsConvertMapperService insConvertMapperService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    InsCarSeriesInfoMapper carSeriesInfoMapper;
    @Autowired
    IUploadFileService uploadFileService;
    @Autowired
    MinioConfig minioConfig;
    @Autowired
    AysLabelPostprocessDataMapper postprocessDataMapper;
    @Autowired
    private IAnalysisDataServiceClient dataServiceClient;

    static final String BRAND_PACKAGE_PATH = "static/品牌";

    private static final String BRAND_INFO_KEY = "{}:brandInfo:{}";
    private static final String SELF_BRAND_CODES_KEY = "{}:selfBrandCodes";
    /** selfBrandCodes 缓存过期时间（秒） */
    private static final long SELF_BRAND_CODES_TTL = 60 * 60 * 24L;


    @CreateCache(area = "VDP", name = ":",  cacheType = CacheType.REMOTE)
    private Cache<String, List<InsBrandInfoModel>> cache;

    @CreateCache(area = "VDP", name = ":",  cacheType = CacheType.REMOTE)
    private Cache<String, List<InsALlBrandAndCarSeriesVo>> cacheAllCarSeries;

    private String getBrandsKey(String... params){
        return StrUtil.format(BRAND_INFO_KEY, ServiceContextHolder.getSystemId(),params);
    }

    private QueryWrapper<InsBrandInfoEntity> createQueryWrapper(InsBrandInfoModel model) {
        InsBrandInfoEntity entity = insConvertMapperService.converTo(model);
        QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<InsBrandInfoEntity> lambdaQueryWrapper = queryWrapper.lambda();
        lambdaQueryWrapper.eq(InsBrandInfoEntity::getDelFlag, false);
        if (CollUtil.isNotEmpty(model.getBrandFilters())) {
            lambdaQueryWrapper.in(InsBrandInfoEntity::getCode, model.getBrandFilters());
        }
        if (StrUtil.isNotBlank(model.getNotIdFilter())) {
            lambdaQueryWrapper.ne(InsBrandInfoEntity::getId, model.getNotIdFilter());
        }
//        model.orderBy(queryWrapper);
//        queryWrapper.lambda().orderByAsc(InsBrandInfoEntity::getCode);
        queryWrapper.orderByAsc("case when name = '非指代品牌' then 0 else 1 end")
                .orderByAsc("competitive_type")
                .orderByDesc("is_core")
                .orderByAsc("code");
//        queryWrapper.lambda().orderByAsc(InsBrandInfoEntity::getCompetitiveType).orderByDesc(InsBrandInfoEntity::getIsCore).orderByAsc(InsBrandInfoEntity::getCode);
        return queryWrapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addInsBrandInfo(InsBrandInfoModel insBrandInfoModel) {
        this.checkParameter(insBrandInfoModel);
        final String id = IdWorker.getId();
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        InsBrandInfoEntity brandInfoEntity = insConvertMapperService.brandModelToInsBrandInfoEntity(insBrandInfoModel);
        brandInfoEntity.setId(id);
        brandInfoEntity.setCreateTime(LocalDateTime.now());
        brandInfoEntity.setCode(this.codeGenerationRules());
        brandInfoEntity.setDelFlag(false);
        brandInfoEntity.setOperator(userName);
        brandInfoEntity.setUpdateUser(userName);
        boolean save = this.save(brandInfoEntity);
        if (save) {
            log.info("新增品牌成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.SAVE_BRAND_ERROR);
        }

        if(ObjectUtils.isNotEmpty(insBrandInfoModel.getCompetitiveProduct())){
            List<BrandInfoVo> competitiveProduct = insBrandInfoModel.getCompetitiveProduct();
            Set<String> collect = competitiveProduct.stream().map(BrandInfoVo::getId).collect(Collectors.toSet());
            QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(InsBrandInfoEntity::getId,collect);
            List<InsBrandInfoEntity> insBrandInfoEntities = this.list(queryWrapper);
            if(ObjectUtils.isNotEmpty(insBrandInfoEntities)){
                List<InsBrandInfoEntity> collect1 = insBrandInfoEntities.stream().map(e -> {
                    BrandInfoVo build = BrandInfoVo.builder()
                            .id(brandInfoEntity.getId())
                            .code(brandInfoEntity.getCode())
                            .name(brandInfoEntity.getName())
                            .build();
                    List<BrandInfoVo> competitiveCar = e.getCompetitiveProduct();
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
                if(b){
                    log.info("更新竞品品牌成功");
                }else{
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_BRAND_ERROR);
                }
            }else{
                throw new BussinessException(InsCommonErrorEnum.COMPETITIVE_BRAND_NOT_EXIST);
            }
        }else{
            log.info("未选择竞品品牌");
        }
        cache.remove(this.getBrandsKey("brand"));
        cache.remove(this.getBrandsKey("brandAllList"));
        if (Integer.valueOf(1).equals(insBrandInfoModel.getCompetitiveType())) {
            redisTemplate.delete(StrUtil.format(SELF_BRAND_CODES_KEY, ServiceContextHolder.getSystemId()));
            log.debug("清除 selfBrandCodes 缓存 (add)");
        }
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void updateInsBrandInfo(InsBrandInfoModel insBrandInfoModel) {
        //独立校验
        Assert.hasLength(insBrandInfoModel.getId(), "id不能为空");
        this.checkParameter(insBrandInfoModel);
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        InsBrandInfoEntity brandInfoEntity = insConvertMapperService.brandModelToInsBrandInfoEntity(insBrandInfoModel);
        brandInfoEntity.setUpdateTime(LocalDateTime.now());
        brandInfoEntity.setDelFlag(false);
        brandInfoEntity.setUpdateUser(userName);
        if(ObjectUtils.isNotEmpty(brandInfoEntity.getImg())&&insBrandInfoModel.getImg().startsWith("/")){
            brandInfoEntity.setImg(null);
        }
        //获取根据id获取品牌
        QueryWrapper<InsBrandInfoEntity> queryOne = new QueryWrapper<>();
        queryOne.lambda().eq(InsBrandInfoEntity::getId, insBrandInfoModel.getId());
        final InsBrandInfoEntity one = this.getOne(queryOne);
        //更新品牌
        boolean save = this.updateById(brandInfoEntity);
        if (save) {
            log.info("更新品牌成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.UPDATE_BRAND_ERROR);
        }

        //获取本竞品字段已包含本车系的车系
        QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(InsBrandInfoEntity::getCompetitiveProduct, insBrandInfoModel.getId());
        List<InsBrandInfoEntity> competitiveProduct = this.list(queryWrapper);
        if(ObjectUtils.isNotEmpty(insBrandInfoModel.getCompetitiveProduct())){
            //本次代更新本竞品信息
            List<BrandInfoVo> competitiveProduct1 = insBrandInfoModel.getCompetitiveProduct();
            Set<String> collect = competitiveProduct1.stream().map(BrandInfoVo::getId).collect(Collectors.toSet());
            QueryWrapper<InsBrandInfoEntity> wrappers = new QueryWrapper<>();
            wrappers.lambda().in(InsBrandInfoEntity::getId, collect);
            List<InsBrandInfoEntity> insBrandInfoEntities = this.list(wrappers);
            if(ObjectUtils.isEmpty(insBrandInfoEntities)){
                throw new BussinessException(InsCommonErrorEnum.COMPETITIVE_BRAND_NOT_EXIST);
            }

            if(ObjectUtils.isNotEmpty(competitiveProduct)){
                //过滤出不在本次代更新的本竞品车系，删除本竞品字段中本车系的信息
                Set<String> collect1 = insBrandInfoEntities.stream().map(InsBrandInfoEntity::getId).collect(Collectors.toSet());
                List<InsBrandInfoEntity> list = competitiveProduct.stream().filter(e -> !collect1.contains(e.getId())).map(e -> {
                    List<BrandInfoVo> competitiveProduct2 = e.getCompetitiveProduct();
                    List<BrandInfoVo> list1 = competitiveProduct2.stream().filter(k -> !k.getId().equals(brandInfoEntity.getId())).toList();
                    e.setCompetitiveProduct(list1);
                    return e;
                }).toList();
                if(ObjectUtils.isNotEmpty( list)){
                    boolean b = this.updateBatchById(list);
                    if (b) {
                        log.info("更新竞品品牌成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_BRAND_ERROR);
                    }
                }else{
                    log.info("无新增的本竞品品牌");
                }


                //过滤出新的竞品车系，将本车系信息加入到本竞品字段中
                Set<String> collect2 = competitiveProduct.stream().map(e -> e.getId()).collect(Collectors.toSet());
                List<InsBrandInfoEntity> list1 = insBrandInfoEntities.stream().filter(e -> !collect2.contains(e.getId())).map(e -> {
                    List<BrandInfoVo> competitiveProduct2 = e.getCompetitiveProduct();
                    BrandInfoVo build = BrandInfoVo.builder()
                            .id(brandInfoEntity.getId())
                            .code(brandInfoEntity.getCode())
                            .name(brandInfoEntity.getName())
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
                if(ObjectUtils.isNotEmpty( list1)){
                    boolean b = this.updateBatchById(list1);
                    if (b) {
                        log.info("更新竞品品牌成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                    }
                }else{
                    log.info("无新增的本竞品品牌");
                }

            }else {
                List<InsBrandInfoEntity> collect1 = insBrandInfoEntities.stream().map(e -> {
                    BrandInfoVo build = BrandInfoVo.builder()
                            .id(brandInfoEntity.getId())
                            .code(brandInfoEntity.getCode())
                            .name(brandInfoEntity.getName())
                            .build();
                    List<BrandInfoVo> competitiveCar = e.getCompetitiveProduct();
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
                    log.info("更新竞品品牌成功");
                } else {
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                }
            }
        }else{
            //删除所有本竞品字段中关联本车系的信息
            if(ObjectUtils.isNotEmpty(competitiveProduct)){
                List<InsBrandInfoEntity> list = competitiveProduct.stream().map(e -> {
                    if(ObjectUtils.isNotEmpty(e.getCompetitiveProduct())){
//                        e.setCompetitiveProduct(null);
                        List<BrandInfoVo> list1 = e.getCompetitiveProduct().stream().filter(k -> !k.getId().equals(brandInfoEntity.getId())).toList();
                        e.setCompetitiveProduct(list1);
                    }
                    return e;
                }).toList();
                boolean b = this.updateBatchById(list);
                if (b) {
                    log.info("更新竞品品牌成功");
                } else {
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_CAR_SERIES_ERROR);
                }
            }else{
                log.info("暂无本竞品品牌，不做任何处理");
            }
        }


        cache.remove(this.getBrandsKey("brand"));
        cache.remove(this.getBrandsKey("brandAllList"));
        if (Integer.valueOf(1).equals(insBrandInfoModel.getCompetitiveType())) {
            redisTemplate.delete(StrUtil.format(SELF_BRAND_CODES_KEY, ServiceContextHolder.getSystemId()));
            log.debug("清除 selfBrandCodes 缓存 (update)");
        }

        //更新结果数据 当是否核心、本竞品类型、车企、状态任意字段与原值不符时，才会进行数据更新
        if( one.getIsCore().intValue() !=insBrandInfoModel.getIsCore().intValue()
        || one.getCompetitiveType().intValue()!=insBrandInfoModel.getCompetitiveType().intValue()
        ||!one.getAutomarkId().equals(insBrandInfoModel.getAutomarkId())
        ||!one.getStatus().equals(insBrandInfoModel.getStatus())
        ||!one.getName().equals(insBrandInfoModel.getName())
        ){
//            InsCqCaDataQueryModel build = InsCqCaDataQueryModel.builder().brandCode(Collections.singletonList(one.getCode())).build();
//            final List<String> dataIds = postprocessDataMapper.findResultDataIdsByBrandCode(build);
//            if(ObjectUtils.isEmpty(dataIds)){
//                log.warn("未查询到相关品牌的结果数据，不作任何处理");
//            }else{
//
//            }
            List<ModifyDataModel.ModifyAttrs> arrtsList = new ArrayList<>();
            List<ModifyDataModel.FilterEntity> filters = new ArrayList<>();
            filters.add(ModifyDataModel.FilterEntity.builder().field("brand_code").value(one.getCode()).build());
            if(one.getIsCore().intValue() !=insBrandInfoModel.getIsCore().intValue()){
                arrtsList.add(ModifyDataModel.ModifyAttrs.builder().field("is_core").value(String.valueOf(insBrandInfoModel.getIsCore())).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("is_core").value(String.valueOf(one.getIsCore())).build());
            }
            if(one.getCompetitiveType().intValue()!=insBrandInfoModel.getCompetitiveType().intValue()){
                arrtsList.add(ModifyDataModel.ModifyAttrs.builder().field("competitive_type").value(String.valueOf(insBrandInfoModel.getCompetitiveType())).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("competitive_type").value(String.valueOf(one.getCompetitiveType())).build());
            }
            if(!one.getAutomarkId().equals(insBrandInfoModel.getAutomarkId())){
                arrtsList.add(ModifyDataModel.ModifyAttrs.builder().field("automark").value(insBrandInfoModel.getAutomark()).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("automark").value(String.valueOf(one.getAutomark())).build());
            }
            if(!one.getStatus().equals(insBrandInfoModel.getStatus())){
                arrtsList.add(ModifyDataModel.ModifyAttrs.builder().field("abandon").value("1".equals(insBrandInfoModel.getStatus())?"0":"1").build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("abandon").value(one.getStatus()).build());
            }
            if(!one.getName().equals(insBrandInfoModel.getName())){
                arrtsList.add(ModifyDataModel.ModifyAttrs.builder().field("brand_name").value(insBrandInfoModel.getName()).build());
//                filters.add(ModifyDataModel.FilterEntity.builder().field("brand_name").value(one.getName()).build());
            }
            ServiceContextHolder.getExecutor().execute(() -> {
                try {
                    this.modifyResultData(filters, arrtsList);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        }else{
            log.info("未修改关键字段，不调用接口更新结果表数据");
        }

    }


    @Override
    public void delInsBrandInfo(InsBrandInfoModel model) {
        Assert.notNull(model.getId(), "id不能为空");
        InsBrandInfoEntity entity = insBrandInfoMapper.selectById(model.getId());
        Assert.notNull(entity, "不存在品牌，不能删除");
        LambdaQueryWrapper<InsCarSeriesInfoEntity> queryWrapper = new LambdaQueryWrapper<InsCarSeriesInfoEntity>()
                .eq(InsCarSeriesInfoEntity::getBrandId, entity.getCode())
                .eq(InsCarSeriesInfoEntity::getDelFlag, false);
        List<InsCarSeriesInfoEntity> list = carSeriesInfoMapper.selectList(queryWrapper);
        Assert.isTrue(list.isEmpty(), "品牌下存在车系，不能删除");
        insBrandInfoMapper.delInsBrandInfoEntity(model.getId());
        cache.remove("brand");
        cache.remove(this.getBrandsKey("brandAllList"));
        if (Integer.valueOf(1).equals(entity.getCompetitiveType())) {
            redisTemplate.delete(StrUtil.format(SELF_BRAND_CODES_KEY, ServiceContextHolder.getSystemId()));
            log.debug("清除 selfBrandCodes 缓存 (delete)");
        }
    }

    @Override
    public List<InsBrandInfoVo> queryByParam(InsBrandInfoModel insBrandInfoModel) {
//        List<InsBrandInfoEntity> list = insBrandInfoMapper.selectMultiInsBrandInfoEntity(insBrandInfoModel);
//        List<InsBrandInfoModel> modelList = list.stream().map(e -> insConvertMapperService.converTo(e)).collect(Collectors.toList());
//        return modelList;

        QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
        if(ObjectUtils.isNotEmpty(insBrandInfoModel.getStatus())){
            queryWrapper.lambda().eq(InsBrandInfoEntity::getStatus,insBrandInfoModel.getStatus());
        }
        if(ObjectUtils.isNotEmpty(insBrandInfoModel.getCompetitiveType())){
            queryWrapper.lambda().eq(InsBrandInfoEntity::getCompetitiveType,insBrandInfoModel.getCompetitiveType());
        }
        queryWrapper.lambda().orderByAsc(InsBrandInfoEntity::getCompetitiveType).orderByDesc(InsBrandInfoEntity::getIsCore).orderByAsc(InsBrandInfoEntity::getCode);

        List<InsBrandInfoEntity> list = this.list(queryWrapper);
        if(ObjectUtils.isEmpty( list)){
            log.info("暂无数据");
            return List.of();
        }

        return insConvertMapperService.brandEntityConvertToBrandVoList( list);
    }

    @Override
    public List<InsBrandInfoModel> findAll() {
        log.trace("读取数据库");
        final List<InsBrandInfoEntity> entityList = list(createQueryWrapper(new InsBrandInfoModel()));
        if (ObjectUtils.isEmpty(entityList)) {
            return Collections.EMPTY_LIST;
        }
        log.trace("读取数据库：{}",entityList.size());
        final List<InsBrandInfoModel> list = entityList.stream().map(e -> insConvertMapperService.converTo(e)).collect(Collectors.toList());
        log.trace("读取数据库-结束");
//        cache.put(this.getBrandsKey("brand"), list);
        return list;
    }

    @Override
    public InsBrandInfoVo findInsBrandInfo(InsBrandInfoModel insBrandInfoModel) {
        Assert.hasLength(insBrandInfoModel.getId(), "id不能为空");
        QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsBrandInfoEntity::getId, insBrandInfoModel.getId());
        final InsBrandInfoEntity entity = getOne(queryWrapper);
        if (ObjectUtils.isEmpty(entity)) {
           throw new BussinessException(InsCommonErrorEnum.BRAND_NOT_EXIST);
        }
        InsBrandInfoVo insBrandInfoVo = insConvertMapperService.brandEntityConvertToBrandVo(entity);
        if(ObjectUtils.isNotEmpty(insBrandInfoVo.getImg())){
            String url = uploadFileService.getObjectUrl(BRAND_PACKAGE_PATH.concat("/").concat(insBrandInfoVo.getImg()));
            String imgUrl = "/files".concat(url.substring(url.indexOf("resource") - 1));
            insBrandInfoVo.setImg(imgUrl);
        }
        return insBrandInfoVo;
    }

    @Override
    public List<InsBrandInfoModel> findSelfBrand() {
        log.trace("读取数据库");
        final List<InsBrandInfoEntity> entityList = list(Wrappers.lambdaQuery(InsBrandInfoEntity.class)
                .eq(InsBrandInfoEntity::getCompetitiveType, 1)
                .eq(InsBrandInfoEntity::getStatus, "1")
                .orderByAsc(InsBrandInfoEntity::getCreateTime));
        if (ObjectUtils.isEmpty(entityList)) {
            return Collections.EMPTY_LIST;
        }
        log.trace("读取数据库：{}", entityList.size());
        final List<InsBrandInfoModel> list = entityList.stream().map(e -> insConvertMapperService.converTo(e)).collect(Collectors.toList());
        log.trace("读取数据库-结束");
//        cache.put(this.getBrandsKey("brand"), list);
        return list;
    }

    @Override
    public Result<?> queryBySelect(InsBrandInfoModel model) {
        IPage<InsBrandInfoEntity> page = new Page<>(model.getPageNum(), model.getPageSize());
        IPage<InsBrandInfoEntity> brandInfoList = this.baseMapper.findBrandInfoList(page,model);
        IPage<InsBrandInfoVo> pages = new Page<>();
        if(ObjectUtils.isEmpty(brandInfoList.getRecords())){
            log.info("暂无品牌数据");
        }else{
            pages.setSize(brandInfoList.getSize());
            pages.setCurrent(brandInfoList.getCurrent());
            pages.setTotal(brandInfoList.getTotal());
            page.setSize(brandInfoList.getSize());
            List<InsBrandInfoEntity> records = brandInfoList.getRecords();
            List<InsBrandInfoVo> insBrandInfoVos =insConvertMapperService.brandEntityConvertToBrandVoList(records);
            pages.setRecords(insBrandInfoVos);
        }
        return Result.OK(pages);
    }

    @Override
    public void batchChangeStatus(InsBrandInfoModel model) {
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIds()),"ids不允许为空");
        Assert.hasLength(model.getStatus(), "状态不允许为空");
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        UpdateWrapper<InsBrandInfoEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(InsBrandInfoEntity::getId,model.getIds());
        updateWrapper.lambda().set(InsBrandInfoEntity::getStatus,model.getStatus());
        updateWrapper.lambda().set(InsBrandInfoEntity::getUpdateUser,userName);
        updateWrapper.lambda().set(InsBrandInfoEntity::getUpdateTime,LocalDateTime.now());
        boolean update = this.update(updateWrapper);
        if (update) {
            log.info("批量修改成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.BATCH_CHANGE_STATUS_ERROR);
        }

        //更新结果数据
        QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(InsBrandInfoEntity::getId, model.getIds());
        queryWrapper.lambda().select(InsBrandInfoEntity::getCode);
        final List<InsBrandInfoEntity> list = this.list(queryWrapper);
        final List<ModifyDataModel.FilterEntity> filters = list.stream()
                .map(InsBrandInfoEntity::getCode)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .map(e->ModifyDataModel.FilterEntity.builder().field("brand_code").value(e).build())
                .collect(Collectors.toList());
        if(ObjectUtils.isEmpty(filters)){
            log.warn("未查询到相关品牌编码，不作结果数据推送");
            return;
        }
//        InsCqCaDataQueryModel build = InsCqCaDataQueryModel.builder().brandCode(collect).build();
//        final List<String> dataIds = postprocessDataMapper.findResultDataIdsByBrandCode(build);
//        if(ObjectUtils.isEmpty(dataIds)){
//            log.warn("未查询到相关品牌的结果数据，不作任何处理");
//        }else{
//
//        }
        ModifyDataModel.ModifyAttrs arrts = ModifyDataModel.ModifyAttrs.builder().field("abandon").value("1".equals(model.getStatus())?"0":"1").build();
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.modifyResultData(filters, Collections.singletonList(arrts));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public InsBrandInfoModel queryByCode(Serializable id) {
        InsBrandInfoEntity one = this.getOne(this.createQueryWrapper(
                InsBrandInfoModel.builder()
                        .id(id.toString())
                        .build()));
        return this.insConvertMapperService.converTo(one);
    }

    protected void checkParameter(InsBrandInfoModel model) {
        Assert.hasLength(model.getName(), "品牌名称不允许为空");
        Assert.isTrue(!model.getName().isEmpty() && model.getName().length() <= 20, "品牌名称不允许超过20个字符");
        Assert.isTrue(StrUtil.isNotBlank(model.getNameEn()) ? model.getNameEn().length() <= 20 : true, "品牌英文名称不允许超过20个字符");
        Assert.hasLength(model.getAutomark(),"车企归属不允许为空");
        Assert.hasLength(model.getImg(),"品牌图片不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIsCore()),"是否核心不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getCompetitiveType()),"本竞品类型不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getStatus()),"状态不允许为空");
        if("2".equals(model.getCompetitiveType())){
            Assert.isTrue(ObjectUtils.isNotEmpty(model.getCompetitiveProduct()),"本竞品关系不允许为空");
        }
        QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsBrandInfoEntity::getName, model.getName());
        if(ObjectUtils.isNotEmpty(model.getId())){
            queryWrapper.lambda().notIn(InsBrandInfoEntity::getId, model.getId());
        }
        InsBrandInfoEntity one = this.getOne(queryWrapper);
        Assert.isTrue(ObjectUtils.isEmpty(one), "品牌名称不允许重复");
    }

    @Override
    public String codeGenerationRules() {
        //找同类 确定上一个最大的code值
        LambdaQueryWrapper<InsBrandInfoEntity> query = new LambdaQueryWrapper<InsBrandInfoEntity>()
                .isNotNull(InsBrandInfoEntity::getCode)
//                .eq(InsBrandInfoEntity::getDelFlag, false)
                .orderByDesc(InsBrandInfoEntity::getCode);
        List<InsBrandInfoEntity> list = baseMapper.selectList(query);
        return YouBianCodeUtil.getNextYouBianCode(CollUtil.isNotEmpty(list) && StrUtil.isNotBlank(list.get(0).getCode())
                ? list.get(0).getCode() : null);
    }

    @Override
    public List<InsALlBrandAndCarSeriesVo> findAllBrandAndCarSeries() {
        log.trace("读取数据库");
        String key = this.getBrandsKey("brandAllList");
        if(ObjectUtils.isNotEmpty(cacheAllCarSeries.get(key))){
            List<InsALlBrandAndCarSeriesVo> insALlBrandAndCarSeriesVos = cacheAllCarSeries.get(key);
            return insALlBrandAndCarSeriesVos;
        }
        List<InsALlBrandAndCarSeriesVo> list = insBrandInfoMapper.findAllBrandAndCarSeries();
        cacheAllCarSeries.put(key, list);
//        List<InsALlBrandAndCarSeriesVo> list = cacheAllCarSeries.computeIfAbsent(key, (k) -> {
//            log.trace("读取数据库");
//            return insBrandInfoMapper.findAllBrandAndCarSeries();
//        });
//
        log.trace("读取数据库-结束");
        return list;
    }

    @Override
    public BrandInfoVo findBrandByBrandName(String brand, Boolean isCompetitiveProduct) {
        Assert.isTrue(ObjectUtils.isNotEmpty(brand),"品牌名称不允许为空");
        QueryWrapper<InsBrandInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsBrandInfoEntity::getName, brand);
        InsBrandInfoEntity entity = this.getOne(queryWrapper);
        if(ObjectUtils.isNotEmpty(entity)){
            BrandInfoVo brandInfoVo = insConvertMapperService.brandEntityConvertToVo(entity);
            if(!isCompetitiveProduct){
                brandInfoVo.setCompetitiveProduct(null);
            }
            return brandInfoVo;
        }
        return null;
    }

    @Override
    public String getSelfBrandCodes() {
        String key = StrUtil.format(SELF_BRAND_CODES_KEY, ServiceContextHolder.getSystemId());
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.debug("getSelfBrandCodes hit cache, key={}", key);
            return cached.toString();
        }
        String result = insBrandInfoMapper.getSelfBrandCodes();
        if (result != null) {
            redisTemplate.opsForValue().set(key, result, SELF_BRAND_CODES_TTL, TimeUnit.SECONDS);
        }
        return result;
    }


    private String getFileName(String name) {
        return BRAND_PACKAGE_PATH.concat("/").concat(name);
    }

    private boolean startsWithHttpOrHttps(String url) {
        Pattern pattern = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.find(); // find 从开头匹配，因为 ^ 已经限定开头
    }


    private void modifyResultData(List<ModifyDataModel.FilterEntity> filters, List<ModifyDataModel.ModifyAttrs> resultDataModel){
        final String id = IdWorker.getId();
        ModifyDataModel build = ModifyDataModel.builder().requestId(id).type(2).attrs(resultDataModel).filters(filters).build();
        log.info("开始推送,请求id:{},入参:{}",id,build);
        Result<?> result = dataServiceClient.modifyResultdata(build);
        if(ObjectUtils.isEmpty(result)||!"200".equals(result.getCode())||ObjectUtils.isEmpty(result.getResult())){
            log.error("调用数据清洗服务更新[id:{}]品牌相关数据接口异常", id);
        }else{
            log.info("调用数据清洗服务新品牌相关数据成功");
        }
    }

}