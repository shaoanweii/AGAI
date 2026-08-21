package com.voc.service.insights.engine.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.voc.service.insights.engine.api.IInsAutomarkService;
import com.voc.service.insights.engine.common.enums.InsCommonErrorEnum;
import com.voc.service.insights.engine.common.util.QueryUtil;
import com.voc.service.insights.engine.entity.InsAutomarkEntity;
import com.voc.service.insights.engine.entity.InsBrandInfoEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.listener.AutomarkExcelListener;
import com.voc.service.insights.engine.mapper.AysLabelPostprocessDataMapper;
import com.voc.service.insights.engine.mapper.InsAutomarkMapper;
import com.voc.service.insights.engine.mapper.InsBrandInfoMapper;
import com.voc.service.insights.engine.model.InsAutomarkExcelModel;
import com.voc.service.insights.engine.model.InsAutomarkModel;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/11 15:31
 * @描述:
 **/
@Service
public class InsAutomarkServiceImpl extends ServiceImpl<InsAutomarkMapper, InsAutomarkEntity> implements IInsAutomarkService {
    private static final Logger log = LoggerFactory.getLogger(InsAutomarkServiceImpl.class);
    @Autowired
    InsConvertMapperService convertMapperService;

    @Autowired
    IUploadFileService uploadFileService;

    @Autowired
    InsBrandInfoMapper insBrandInfoMapper;

    @Autowired
    AysLabelPostprocessDataMapper postprocessDataMapper;
    @Autowired
    private IAnalysisDataServiceClient dataServiceClient;

    static final String AUTOMARK_PACKAGE_PATH = "static/车企";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAutomark(InsAutomarkModel model) {
        this.checkParameter(model);
        final String id = IdWorker.getId();
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        InsAutomarkEntity insAutomarkEntity =  convertMapperService.automarkModelConvertToEntity(model);
        insAutomarkEntity.setId(id);
        insAutomarkEntity.setCreateTime(LocalDateTime.now());
        insAutomarkEntity.setOperator(userName);
        insAutomarkEntity.setUpdateOperator(userName);
        boolean save = this.save(insAutomarkEntity);
        if (save) {
            log.info("新增车企成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.SAVE_BRAND_ERROR);
        }

        if(ObjectUtils.isNotEmpty(model.getCompetitiveProduct())){
            List<AutomarkVo> competitiveProduct = model.getCompetitiveProduct();
            Set<String> collect = competitiveProduct.stream().map(AutomarkVo::getId).collect(Collectors.toSet());
            QueryWrapper<InsAutomarkEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(InsAutomarkEntity::getId,collect);
            List<InsAutomarkEntity> insAutomarkEntities = this.list(queryWrapper);
            if(ObjectUtils.isNotEmpty(insAutomarkEntities)){
                List<InsAutomarkEntity> collect1 = insAutomarkEntities.stream().map(e -> {
                    AutomarkVo build = AutomarkVo.builder()
                            .id(insAutomarkEntity.getId())
                            .name(insAutomarkEntity.getName())
                            .build();
                    List<AutomarkVo> competitiveCar = e.getCompetitiveProduct();
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
                    log.info("更新竞品车企成功");
                }else{
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                }
            }else{
                throw new BussinessException(InsCommonErrorEnum.COMPETITIVE_AUTOMARK_NOT_EXIST);
            }
        }else{
            log.info("未选择竞品车企");
        }
    }

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public void updateAutomark(InsAutomarkModel model) {
        //独立校验
        Assert.hasLength(model.getId(), "id不能为空");
        this.checkParameter(model);
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        InsAutomarkEntity insAutomarkEntity =  convertMapperService.automarkModelConvertToEntity(model);
        insAutomarkEntity.setUpdateTime(LocalDateTime.now());
        insAutomarkEntity.setUpdateOperator(userName);
        if(ObjectUtils.isNotEmpty(model.getImg())&&model.getImg().startsWith("/")){
            insAutomarkEntity.setImg(null);
        }

        //获取根据id获车系
        QueryWrapper<InsAutomarkEntity> queryOne = new QueryWrapper<>();
        queryOne.lambda().eq(InsAutomarkEntity::getId, model.getId());
        final InsAutomarkEntity one = this.getOne(queryOne);

        boolean update = this.updateById(insAutomarkEntity);
        if (update) {
            log.info("更新车企成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.SAVE_BRAND_ERROR);
        }

        //获取本竞品字段已包含本车系的车系
        QueryWrapper<InsAutomarkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().like(InsAutomarkEntity::getCompetitiveProduct, model.getId());
        List<InsAutomarkEntity> competitiveProduct = this.list(queryWrapper);
        if(ObjectUtils.isNotEmpty(model.getCompetitiveProduct())){
            //本次代更新本竞品信息
            List<AutomarkVo> competitiveProduct1 = model.getCompetitiveProduct();
            Set<String> collect = competitiveProduct1.stream().map(AutomarkVo::getId).collect(Collectors.toSet());
            QueryWrapper<InsAutomarkEntity> wrappers = new QueryWrapper<>();
            wrappers.lambda().in(InsAutomarkEntity::getId, collect);
            List<InsAutomarkEntity> insBrandInfoEntities = this.list(wrappers);
            if(ObjectUtils.isEmpty(insBrandInfoEntities)){
                throw new BussinessException(InsCommonErrorEnum.COMPETITIVE_AUTOMARK_NOT_EXIST);
            }

            if(ObjectUtils.isNotEmpty(competitiveProduct)){
                //过滤出不在本次代更新的本竞品车系，删除本竞品字段中本车系的信息
                Set<String> collect1 = insBrandInfoEntities.stream().map(InsAutomarkEntity::getId).collect(Collectors.toSet());
                List<InsAutomarkEntity> list = competitiveProduct.stream().filter(e -> !collect1.contains(e.getId())).map(e -> {
                    List<AutomarkVo> competitiveProduct2 = e.getCompetitiveProduct();
                    List<AutomarkVo> list1 = competitiveProduct2.stream().filter(k -> !k.getId().equals(insAutomarkEntity.getId())).toList();
                    e.setCompetitiveProduct(list1);
                    return e;
                }).toList();
                if(ObjectUtils.isNotEmpty( list)){
                    boolean b = this.updateBatchById(list);
                    if (b) {
                        log.info("更新竞品车企成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                    }
                }else{
                    log.info("无新增的本竞品车企");
                }


                //过滤出新的竞品车系，将本车系信息加入到本竞品字段中
                Set<String> collect2 = competitiveProduct.stream().map(e -> e.getId()).collect(Collectors.toSet());
                List<InsAutomarkEntity> list1 = insBrandInfoEntities.stream().filter(e -> !collect2.contains(e.getId())).map(e -> {
                    List<AutomarkVo> competitiveProduct2 = e.getCompetitiveProduct();
                    AutomarkVo build = AutomarkVo.builder()
                            .id(insAutomarkEntity.getId())
                            .name(insAutomarkEntity.getName())
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
                        log.info("更新竞品车企成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                    }
                }else{
                    log.info("无新增的本竞品车企");
                }

            }else {
                List<InsAutomarkEntity> collect1 = insBrandInfoEntities.stream().map(e -> {
                    AutomarkVo build = AutomarkVo.builder()
                            .id(insAutomarkEntity.getId())
                            .name(insAutomarkEntity.getName())
                            .build();
                    List<AutomarkVo> competitiveCar = e.getCompetitiveProduct();
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
                    log.info("更新竞品车企成功");
                } else {
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                }
            }
        }else{
            //删除所有本竞品字段中关联本车系的信息
            if(ObjectUtils.isNotEmpty(competitiveProduct)){
                List<InsAutomarkEntity> list = competitiveProduct.stream().map(e -> {
                    if(ObjectUtils.isNotEmpty(e.getCompetitiveProduct())){
//                        e.setCompetitiveProduct(null);
                        List<AutomarkVo> list1 = e.getCompetitiveProduct().stream().filter(k -> !k.getId().equals(insAutomarkEntity.getId())).toList();
                        e.setCompetitiveProduct(list1);
                    }
                    return e;
                }).toList();
                boolean b = this.updateBatchById(list);
                if (b) {
                    log.info("更新竞品车企成功");
                } else {
                    throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                }
            }else{
                log.info("暂无本竞品车企，不做任何处理");
            }
        }

        //更新品牌表
        UpdateWrapper<InsBrandInfoEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(InsBrandInfoEntity::getAutomarkId, insAutomarkEntity.getId());
        updateWrapper.lambda().set(InsBrandInfoEntity::getAutomark, insAutomarkEntity.getName());
        insBrandInfoMapper.update(updateWrapper);
//        int update1 = insBrandInfoMapper.update(updateWrapper);
//        if (update1 > 0) {
//            log.info("更新品牌表成功");
//        } else {
//            throw new BussinessException(InsCommonErrorEnum.UPDATE_BRAND_ERROR);
//        }

        if(!one.getName().equals(model.getName())||!one.getStatus().equals(model.getStatus())){
//            InsCqCaDataQueryModel build = InsCqCaDataQueryModel.builder().automark(model.getName()).build();
//            final List<String> dataIds = postprocessDataMapper.findResultDataIdsByBrandCode(build);
//            if(ObjectUtils.isEmpty(dataIds)){
//                log.warn("未查询到相关车系的结果数据，不作任何处理");
//            }else{
//
//            }
            List<ModifyDataModel.ModifyAttrs> arrts = new ArrayList<>();
            List<ModifyDataModel.FilterEntity> filters = new ArrayList<>();
            filters.add(ModifyDataModel.FilterEntity.builder().field("automark").value(one.getName()).build());
            if(!one.getName().equals(model.getName())){
                arrts.add(ModifyDataModel.ModifyAttrs.builder().field("automark").value(model.getName()).build());
            }

            if(!one.getStatus().equals(model.getStatus())){
                arrts.add(ModifyDataModel.ModifyAttrs.builder().field("abandon").value("1".equals(model.getStatus())?"0":"1").build());
            }
            ServiceContextHolder.getExecutor().execute(() -> {
                try {
                    this.modifyResultData(filters, arrts);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }else{
            log.info("未修改关键字段，不调用接口更新结果表数据");
        }

    }

    @Override
    public InsAutomarkInfoVo findAutomarkInfo(InsAutomarkModel model) {
        Assert.hasLength(model.getId(), "id不能为空");
        QueryWrapper<InsAutomarkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsAutomarkEntity::getId, model.getId());
        InsAutomarkEntity insAutomarkEntity = this.baseMapper.selectOne(queryWrapper);
        if(ObjectUtils.isEmpty(insAutomarkEntity)){
            throw new BussinessException(InsCommonErrorEnum.AUTOMARK_NOT_EXIST);
        }
        InsAutomarkInfoVo insAutomarkInfoVo = convertMapperService.automarkEntityConvartToVo(insAutomarkEntity);
        if(ObjectUtils.isNotEmpty(insAutomarkInfoVo.getImg())){
            final String url = uploadFileService.getObjectUrl(AUTOMARK_PACKAGE_PATH.concat("/").concat(insAutomarkInfoVo.getImg()));
            String imgUrl = "/files".concat(url.substring(url.indexOf("resource") - 1));
            insAutomarkInfoVo.setImg(imgUrl);
        }

        return insAutomarkInfoVo;
    }

    @Override
    public IPage<InsAutomarkInfoVo> findAutomarkList(InsAutomarkModel model) {
        IPage<InsAutomarkEntity> page = new Page<>(model.getPageNum(), model.getPageSize());
        IPage<InsAutomarkEntity> insAutomarkEntityIPage = this.baseMapper.findAutomarkList(page, model);
        IPage<InsAutomarkInfoVo> pages = new Page<>();
        if(ObjectUtils.isEmpty(insAutomarkEntityIPage.getRecords())){
            log.info("暂无车企数据");
        }else{
            pages.setSize(insAutomarkEntityIPage.getSize());
            pages.setCurrent(insAutomarkEntityIPage.getCurrent());
            pages.setTotal(insAutomarkEntityIPage.getTotal());
            List<InsAutomarkEntity> records = insAutomarkEntityIPage.getRecords();
            List<InsAutomarkInfoVo> automarkInfoVos = convertMapperService.automarkEntityConvartToVoList(records);
            pages.setRecords(automarkInfoVos);
        }
        return pages;
    }

    @Override
    public List<AutomarkVo> findAutomarkInfoList(InsAutomarkModel model) {
        QueryWrapper<InsAutomarkEntity> queryWrapper = new QueryWrapper<>();
        if(ObjectUtils.isNotEmpty(model.getStatus())){
            queryWrapper.lambda().eq(InsAutomarkEntity::getStatus,model.getStatus());
        }
        if(ObjectUtils.isNotEmpty(model.getCompetitiveType())){
            queryWrapper.lambda().eq(InsAutomarkEntity::getCompetitiveType,model.getCompetitiveType());
        }

        queryWrapper.lambda().orderByAsc(InsAutomarkEntity::getCompetitiveType).orderByDesc(InsAutomarkEntity::getIsCore);
        List<InsAutomarkEntity> list = this.list(queryWrapper);
        if(ObjectUtils.isEmpty( list)){
            log.info("暂无车企数据");
            return List.of();
        }
        return convertMapperService.automarkEntityConvertToAutomarkList(list);
    }

    @Override
    public void batchChangeStatus(InsAutomarkModel model) {
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIds()), "id不能为空");
        Assert.hasLength(model.getStatus(), "status不能为空");
        UserModel user = ServiceContextHolder.getUser();
        String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
        UpdateWrapper<InsAutomarkEntity> queryWrapper = new UpdateWrapper<>();
        queryWrapper.lambda().in(InsAutomarkEntity::getId,model.getIds());
        queryWrapper.lambda().set(InsAutomarkEntity::getStatus,model.getStatus());
        queryWrapper.lambda().set(InsAutomarkEntity::getUpdateOperator,userName);
        queryWrapper.lambda().set(InsAutomarkEntity::getUpdateTime,LocalDateTime.now());
        boolean update = this.update(queryWrapper);
        if(update){
            log.info("批量更新车企状态成功");
        }else{
            throw new BussinessException(InsCommonErrorEnum.BATCH_CHANGE_STATUS_ERROR);
        }

        QueryWrapper<InsAutomarkEntity> automarkQueryWrapper = new QueryWrapper<>();
        automarkQueryWrapper.lambda().in(InsAutomarkEntity::getId, model.getIds());
        automarkQueryWrapper.lambda().select(InsAutomarkEntity::getName);
        List<InsAutomarkEntity> automarkEntities = this.list(automarkQueryWrapper);
        List<ModifyDataModel.FilterEntity> automarkNames = automarkEntities.stream()
                .map(InsAutomarkEntity::getName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .map(e->ModifyDataModel.FilterEntity.builder().field("automark").value(e).build())
                .toList();
        if (ObjectUtils.isEmpty(automarkNames)) {
            log.warn("未查询到相关车企名称，不作结果数据推送");
            return;
        }

//        InsCqCaDataQueryModel build = InsCqCaDataQueryModel.builder().automarkList(automarkNames).build();
//        List<String> queriedDataIds = postprocessDataMapper.findResultDataIdsByBrandCode(build);
//        Set<String> dataIdSet = ObjectUtils.isEmpty(queriedDataIds) ? new LinkedHashSet<>() : new LinkedHashSet<>(queriedDataIds);
//        if (ObjectUtils.isEmpty(dataIdSet)) {
//            log.warn("未查询到相关车企的结果数据，不作任何处理");
//            return;
//        }

        ModifyDataModel.ModifyAttrs arrts = ModifyDataModel.ModifyAttrs.builder()
                .field("abandon")
                .value("1".equals(model.getStatus()) ? "0" : "1")
                .build();
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.modifyResultData(automarkNames, Collections.singletonList(arrts));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public void uploadExcel(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), InsAutomarkExcelModel.class, new AutomarkExcelListener(this)).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void analyzeExcelData(List<InsAutomarkExcelModel> list) {
        list.stream().forEach(e->{
//            新增
//            final String id = IdWorker.getId();
//            UserModel user = ServiceContextHolder.getUser();
//            String userName = user.getFirstname().concat("(").concat(user.getEmployeeId()).concat(")");
//            InsAutomarkEntity insAutomarkEntity =InsAutomarkEntity.builder()
//                    .id( id)
//                    .name(e.getName())
//                    .isCore(e.getIsCore())
//                    .competitiveType(e.getCompetitiveType())
//                    .operator(userName)
//                    .updateOperator(userName)
//                    .createTime(LocalDateTime.now())
//                    .build();
//            boolean save = this.save(insAutomarkEntity);
//            if (save) {
//                log.info("新增车企成功");
//            }else{
//                throw new BussinessException(InsCommonErrorEnum.SAVE_BRAND_ERROR);
//            }


//            //更新
            QueryWrapper<InsAutomarkEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(InsAutomarkEntity::getName, e.getName());
            InsAutomarkEntity insAutomarkEntity = this.getOne(queryWrapper);

            //获取本竞品字段已包含本车系的车系
            QueryWrapper<InsAutomarkEntity> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().like(InsAutomarkEntity::getCompetitiveProduct, insAutomarkEntity.getId());
            List<InsAutomarkEntity> competitiveProduct = this.list(queryWrapper1);
            if(ObjectUtils.isNotEmpty(e.getCompetitiveProduct())){
                List<String> names = new ArrayList<>();
                String competitiveProduct3 = e.getCompetitiveProduct();
                if(competitiveProduct3.contains(",")){
                    Collections.addAll(names, competitiveProduct3.split(","));
                }else {
                    names.add(competitiveProduct3);
                }
                //本次代更新本竞品信息
                QueryWrapper<InsAutomarkEntity> wrappers = new QueryWrapper<>();
                wrappers.lambda().in(InsAutomarkEntity::getName, names);
                List<InsAutomarkEntity> insBrandInfoEntities = this.list(wrappers);
                if(ObjectUtils.isEmpty(insBrandInfoEntities)){
                    throw new BussinessException(InsCommonErrorEnum.COMPETITIVE_AUTOMARK_NOT_EXIST);
                }

                if(ObjectUtils.isNotEmpty(competitiveProduct)){
                    //过滤出不在本次代更新的本竞品车系，删除本竞品字段中本车系的信息
                    Set<String> collect1 = insBrandInfoEntities.stream().map(InsAutomarkEntity::getId).collect(Collectors.toSet());
                    List<InsAutomarkEntity> list1 = competitiveProduct.stream().filter(k -> !collect1.contains(k.getId())).map(k -> {
                        List<AutomarkVo> competitiveProduct2 = k.getCompetitiveProduct();
                        List<AutomarkVo> list2 = competitiveProduct2.stream().filter(v -> !v.getId().equals(insAutomarkEntity.getId())).toList();
                        k.setCompetitiveProduct(list2);
                        return k;
                    }).toList();
                    if(ObjectUtils.isNotEmpty( list1)){
                        boolean b = this.updateBatchById(list1);
                        if (b) {
                            log.info("更新竞品车企成功");
                        } else {
                            throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                        }
                    }else{
                        log.info("无新增的本竞品车企");
                    }


                    //过滤出新的竞品车系，将本车系信息加入到本竞品字段中
                    Set<String> collect2 = competitiveProduct.stream().map(k -> k.getId()).collect(Collectors.toSet());
                    List<InsAutomarkEntity> list3 = insBrandInfoEntities.stream().filter(k -> !collect2.contains(k.getId())).map(k -> {
                        List<AutomarkVo> competitiveProduct2 = k.getCompetitiveProduct();
                        AutomarkVo build = AutomarkVo.builder()
                                .id(insAutomarkEntity.getId())
                                .name(insAutomarkEntity.getName())
                                .build();
                        if (ObjectUtils.isNotEmpty(competitiveProduct2)) {
                            competitiveProduct2.add(build);
                        } else {
                            competitiveProduct2 = new ArrayList<>();
                            competitiveProduct2.add(build);
                        }
                        k.setCompetitiveProduct(competitiveProduct2);
                        return k;
                    }).toList();
                    if(ObjectUtils.isNotEmpty( list3)){
                        boolean b = this.updateBatchById(list3);
                        if (b) {
                            log.info("更新竞品车企成功");
                        } else {
                            throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                        }
                    }else{
                        log.info("无新增的本竞品车企");
                    }

                }else {
                    List<InsAutomarkEntity> collect1 = insBrandInfoEntities.stream().map(k -> {
                        AutomarkVo build = AutomarkVo.builder()
                                .id(insAutomarkEntity.getId())
                                .name(insAutomarkEntity.getName())
                                .build();
                        List<AutomarkVo> competitiveCar = k.getCompetitiveProduct();
                        if (ObjectUtils.isNotEmpty(competitiveCar)) {
                            competitiveCar.add(build);
                        } else {
                            competitiveCar = new ArrayList<>();
                            competitiveCar.add(build);
                        }
                        k.setCompetitiveProduct(competitiveCar);
                        return k;
                    }).toList();
                    boolean b = this.updateBatchById(collect1);
                    if (b) {
                        log.info("更新竞品车企成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                    }
                }
            }else{
                //删除所有本竞品字段中关联本车系的信息
                if(ObjectUtils.isNotEmpty(competitiveProduct)){
                    List<InsAutomarkEntity> list1 = competitiveProduct.stream().map(k -> {
                        k.setCompetitiveProduct(null);
                        return k;
                    }).toList();
                    boolean b = this.updateBatchById(list1);
                    if (b) {
                        log.info("更新竞品车企成功");
                    } else {
                        throw new BussinessException(InsCommonErrorEnum.UPDATE_COMPETITIVE_AUTOMARK_ERROR);
                    }
                }else{
                    log.info("暂无本竞品车企，不做任何处理");
                }
            }
        });
    }

    protected void checkParameter(InsAutomarkModel model) {
        Assert.hasLength(model.getName(), "名称不能为空");
        Assert.hasLength(model.getImg(), "图片不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getIsCore()), "是否核心不能为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(model.getCompetitiveType()), "本竞品类型不能为空");
        Assert.hasLength(model.getStatus(), "状态不能为空");
        QueryWrapper<InsAutomarkEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InsAutomarkEntity::getName, model.getName());
        if(ObjectUtils.isNotEmpty(model.getId())){
            queryWrapper.lambda().notIn(InsAutomarkEntity::getId, model.getId());
        }
        InsAutomarkEntity one = this.getOne(queryWrapper);
        Assert.isTrue(ObjectUtils.isEmpty(one), "车企名称不允许重复");
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
            log.error("调用数据清洗服务更新[id:{}]车企相关数据接口异常", id);
        }else{
            log.info("调用数据清洗服务更新车企相关数据成功");
        }
    }

}
