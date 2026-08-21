package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.ttl.TtlWrappers;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.*;
import com.voc.service.insights.engine.api.data.IInsDataSourceService;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.api.model.ProjectRawDataParamModel;
import com.voc.service.insights.engine.api.model.ProjectResultDataParamModel;
import com.voc.service.insights.engine.common.util.ExcelUtil;
import com.voc.service.insights.engine.dao.InsProjectDetailsDao;
import com.voc.service.insights.engine.dao.InsProjectInfoDao;
import com.voc.service.insights.engine.entity.*;
import com.voc.service.insights.engine.enums.LabelTypeEnum;
import com.voc.service.insights.engine.enums.LargeDigitaFilesType;
import com.voc.service.insights.engine.enums.TagLibeType;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/28 17:35
 * @描述:
 **/
@Service
public class InsProjectInfoServiceImpl implements IInsProjectInfoService {

    private static final Logger log = LoggerFactory.getLogger(InsProjectInfoServiceImpl.class);
    @Autowired
    InsConvertMapperService mapperService;
    @Autowired
    InsProjectInfoDao insProjectInfoDao;
    @Resource
    private IInsDataSourceService dataSourceService;
    @Autowired
    IInsRegionConfigService regionConfigService;
    @Autowired
    InsProjectDetailsDao insProjectDetailsDao;
    @Autowired
    IInsTagLibClientService insTagLibClientService;
    @Autowired
    IInsChannelInfoService channelInfoService;

    @Autowired
    IInsCarSeriesInfoService carSeriesInfoService;
    @Value("${insights.dataSource.id:1850720952091041794}")
    String dataSourceID;

    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    IAysPostprocessDataService aysPostprocessDataService;
    @Autowired
    ILargeDigitaFilesService largeDigitaFilesService;

    @CreateCache(area = "VDP", name = ":insights:channelInfo", expire = 60 * 3, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String, List<ChannelInfoVo>> channelCache;
    @CreateCache(area = "VDP", name = ":insights:regionConfig", expire = 60 * 1, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.REMOTE)
    private Cache<String, List<RegionConfigVo>> regionConfigCache;


    @Override
    public void saveProjectInfo(InsProjectInfoModel insProjectInfoModel) {
        //参数校验
        this.checkParams(insProjectInfoModel);
        //用户名
        final String username = ServiceContextHolder.getUsername();
        //客户id
        final String clientId = insProjectInfoModel.getClientId();
        InsProjectInfoEntity projectEntity = mapperService.projectModelConvertEntity(insProjectInfoModel);
        final String projectId = IdWorker.getId();
        projectEntity.setId(projectId);
        projectEntity.setCreateTime(LocalDateTime.now());
        projectEntity.setCreateUser(username);
        final List<BrandModel> brand = insProjectInfoModel.getBrand();
        //项目详情
        List<InsProjectDetailsEntity> detailsEntities = brand.stream().map(e -> {
            InsProjectDetailsEntity projectDetailsEntity = mapperService.projectDetailsModelConvertEntity(e);
            projectDetailsEntity.setProjectId(projectId);
            projectDetailsEntity.setId(IdWorker.getId());
            return projectDetailsEntity;
        }).collect(Collectors.toList());
        //保存项目信息
        insProjectInfoDao.saveProjectInfo(clientId, projectEntity);
        //批量保存项目详情
        insProjectDetailsDao.saveBatchProjectDetails(clientId, detailsEntities);
    }

    @Override
    public void updateProjectInfo(InsProjectInfoModel insProjectInfoModel) {
        //参数校验
        this.checkParams(insProjectInfoModel);
        //单独参数校验
        Assert.hasLength(insProjectInfoModel.getId(), "项目id不允许为空");
        //用户名
        final String username = ServiceContextHolder.getUsername();
        //客户id
        final String clientId = insProjectInfoModel.getClientId();
        InsProjectInfoEntity projectEntity = mapperService.projectModelConvertEntity(insProjectInfoModel);
        projectEntity.setUpdateTime(LocalDateTime.now());
        projectEntity.setUpdateUser(username);
        final List<BrandModel> brand = insProjectInfoModel.getBrand();
        //项目详情
        List<InsProjectDetailsEntity> detailsEntities = brand.stream().map(e -> {
            InsProjectDetailsEntity projectDetailsEntity = mapperService.projectDetailsModelConvertEntity(e);
            if (ObjectUtils.isEmpty(projectDetailsEntity.getId())) {
                projectDetailsEntity.setId(IdWorker.getId());
                projectDetailsEntity.setProjectId(insProjectInfoModel.getId());
            }
            if (ObjectUtils.isEmpty(projectDetailsEntity.getProjectId())) {
                projectDetailsEntity.setProjectId(insProjectInfoModel.getId());
            }
            return projectDetailsEntity;
        }).collect(Collectors.toList());
        //更新项目信息
        insProjectInfoDao.updateProjectInfo(clientId, projectEntity);
        //批量更新项目详情
        insProjectDetailsDao.deleteProjectInfo(clientId, insProjectInfoModel.getId());
        insProjectDetailsDao.saveBatchProjectDetails(clientId, detailsEntities);
        try {
            insTagLibClientService.removeTagLibeCache(clientId);
            regionConfigCache.remove(clientId);
            channelCache.remove(clientId);
        } catch (Exception e) {
            log.error("清除缓存失败");
        }

    }


    @Override
    public PageInfo findProjectList(InsProjectInfoModel insProjectInfoModel) {
        Assert.hasLength(insProjectInfoModel.getClientId(), "客户id不允许为空");
        PageHelper.startPage(insProjectInfoModel.getPageNum(), insProjectInfoModel.getPageSize());
        List<InsProjectInfoEntity> projectList = insProjectInfoDao.findProjectList(insProjectInfoModel);
        if (ObjectUtils.isEmpty(projectList)) {
            log.info("无项目信息");
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(projectList);
        if (log.isDebugEnabled()) {
            log.debug("转换前 projectList:{}", JSONArray.toJSONString(projectList));
        }
        List<ProjectInfoVo> projectInfoVos = mapperService.projectEntityListConvertVoList(projectList);
        projectInfoVos.stream().forEach(e -> {
            String createTime = e.getCreateTime();
            LocalDateTime localDateTime = LocalDateTime.parse(createTime);
            String format = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            e.setCreateTime(format);
        });
        if (log.isDebugEnabled()) {
            log.debug("转换后 projectInfoVos:{}", JSONArray.toJSONString(projectInfoVos));
        }
        pageInfo.setList(projectInfoVos);
        return pageInfo;
    }

    @Autowired
    IInsChannelInfoService insChannelInfoService;


    private List<ChannelInfoVo> getChannalInfoList(String clientId, Set<String> ids) {
        List<ChannelInfoVo> rs = channelInfoService.upwardFindChannelHierarchical(InsChannelInfoModel.builder().channelCodes(new ArrayList<>(ids)).clientId(clientId).build());
        if (CollUtil.isEmpty(rs)) {
            return null;
        }
        List<ChannelInfoVo> collect = rs.stream().sorted(Comparator.comparing(ChannelInfoVo::getCreateTime)).collect(Collectors.toList());
        return collect;
    }

    private List<RegionConfigVo> getRegionConfigList(String clientId, Set<String> ids, String brandName) {
        final String key = clientId + ids.hashCode();
        final List<RegionConfigVo> list = regionConfigCache.computeIfAbsent(key, k -> {
            List<RegionConfigVo> rs = regionConfigService.findRegionTreeByIds(InsRegionConfigModel.builder().clientId(clientId).regionIds(new ArrayList<>(ids)).brandName(brandName).build());

            if (CollUtil.isEmpty(rs)) {
                return null;
            }
            return rs;
        });

        return list;
    }

    private List<TagLibCategoryVo> getTagLibCategoryList(String clientId, Set<String> types) {

        final List<TagLibCategoryVo> list = insTagLibClientService.findTagLibClientTreeLevel(clientId, null);

        ;/*final List<TagLibCategoryVo> list = tagLibCategoryCache.computeIfAbsent(clientId, k -> {
            return  insTagLibClientService.findTagLibClientTreeLevel(clientId,  null);
        });*/

        return list.stream().filter(e -> types.contains(e.getTagType())).collect(Collectors.toList());
    }


    @Override
    public ProjectInfoVo findProjectInfo(InsProjectInfoModel insProjectInfoModel) {
        StopWatch watch = StopWatch.create("findProjectInfo");
        //单独参数校验
        Assert.hasLength(insProjectInfoModel.getId(), "项目id不允许为空");
        Assert.hasLength(insProjectInfoModel.getClientId(), "客户id不允许为空");


        /**
         * 项目先关信息获取
         */
        watch.start("项目先关信息获取");
        final InsProjectInfoEntity projectInfo = insProjectInfoDao.findProjectInfo(insProjectInfoModel);
        ProjectInfoVo build = mapperService.projectEntityConvertVo(projectInfo);
        //品牌分类
        final List<InsProjectDetailsEntity> projectDetailsEntity = insProjectDetailsDao.findProjectInfo(insProjectInfoModel.getClientId(), insProjectInfoModel.getId());

        if (log.isDebugEnabled()) {
            log.debug("转换前 projectInfoEntity:{}", JSONArray.toJSONString(projectDetailsEntity));
        }
        watch.stop();
        watch.start("获取标签数据");
        final List<TagLibCategoryVo> tagList = this.getTagLibCategoryList(insProjectInfoModel.getClientId()
                , Set.of(LabelTypeEnum.PROD.getCode(), LabelTypeEnum.SERVICE.getCode(), LabelTypeEnum.QY.getCode()));
        final Map<String, List<TagLibCategoryVo>> tagMap = tagList.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagType, Collectors.toList()));


        watch.stop();
        watch.start("projectDetailsEntity.stream");
        List<BrandVo> projectDetailsVos = projectDetailsEntity.stream().map(e -> {
            List<CompletableFuture<Void>> futureList = new CopyOnWriteArrayList<>();
            BrandVo brandVo = mapperService.projectDetailsEntityConvertBrandVo(e);

            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                //车系
                final List<CarSeriesEntity> carSeries = e.getCarSeries();
                final String jsonString = JSONArray.toJSONString(carSeries);
                final List<CarSeriesVo> carSeriesVos = JSONUtil.toList(jsonString, CarSeriesVo.class);
                List<CarSeriesVo> collect = carSeriesVos.stream().sorted(Comparator.comparing(CarSeriesVo::getCarSeriesCode)).collect(Collectors.toList());
                brandVo.setCarSeries(collect);
                return null;
            })));


            //风险
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                final List<InsRiskEarlyWarningEntity> riskEarlyWarning = e.getRiskEarlyWarning();
                String risk = JSONArray.toJSONString(riskEarlyWarning);
                List<InsRiskEarlyWarningVo> list = JSONUtil.toList(risk, InsRiskEarlyWarningVo.class);
                brandVo.setRiskEarlyWarning(list);
                final List<CompetitiveProductEntity> competitiveProduct = e.getCompetitiveProduct();
                String competitive = JSONArray.toJSONString(competitiveProduct);
                List<CompetitiveProductVo> competitiveProductVos = JSONUtil.toList(competitive, CompetitiveProductVo.class);
                brandVo.setCompetitiveProduct(competitiveProductVos);
                return null;
            })));


            //渠道
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                final List<ChannelInfoVo> channelInfoVos = this.getChannalInfoList(insProjectInfoModel.getClientId(), new HashSet<>(e.getChannel()));
                brandVo.setChannelTree(channelInfoVos);
                //渠道码
                brandVo.setChannelCode(e.getChannel());
                return null;
            })));

            watch.stop();
            watch.start("区域 ".concat(e.getBrandCode()));
            //区域
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                final List<String> region = e.getRegion();
                final List<RegionConfigVo> regionTreeByIds = this.getRegionConfigList(insProjectInfoModel.getClientId(), new HashSet<>(region), brandVo.getBrandName());
                brandVo.setRegionTree(regionTreeByIds);
                return null;
            })));

            try {
                CompletableFuture.allOf(futureList.stream().toArray(CompletableFuture[]::new)).get(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }


            //标签类型
            e.getTags().stream().forEach(tagType -> {
                if (LabelTypeEnum.PROD.getCode().equalsIgnoreCase(tagType)) {
                    brandVo.setBIZ(tagMap.get(tagType));
                } else if (LabelTypeEnum.QY.getCode().equalsIgnoreCase(tagType)) {
                    brandVo.setQY(tagMap.get(tagType));
                } else if (LabelTypeEnum.SERVICE.getCode().equalsIgnoreCase(tagType)) {
                    brandVo.setSIC(tagMap.get(tagType));
                }
            });

            return brandVo;
        }).collect(Collectors.toList());
        if (log.isDebugEnabled()) {
            log.debug("转换后 projectInfoVo:{}", JSONArray.toJSONString(projectDetailsVos));
        }

        build.setBrand(projectDetailsVos);
        watch.stop();
        log.info("获取【项目】标签信息耗时：{}", watch.prettyPrint(TimeUnit.MILLISECONDS));
        return build;
    }

    @Override
    public List<ProjectInfoVo> findAllProjectInfo(String clientId) {
        List<InsProjectInfoEntity> projectList = insProjectInfoDao.findProjectList(InsProjectInfoModel.builder().clientId(clientId).build());
        if (log.isDebugEnabled()) {
            log.debug("转换前 projectList:{}", JSONArray.toJSONString(projectList));
        }
        List<ProjectInfoVo> projectInfoVos = mapperService.projectEntityListConvertVoList(projectList);
        if (log.isDebugEnabled()) {
            log.debug("转换后 projectInfoVos:{}", JSONArray.toJSONString(projectInfoVos));
        }
        return projectInfoVos;
    }

    @Override
    public PageInfo findRawData(InsDataSourceModel dataSourceModel) {
        Assert.hasLength(dataSourceModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(dataSourceModel.getProjectId(), "项目id不允许为空");
        this.dataAssembly(dataSourceModel);
        return insProjectInfoDao.findRawData(dataSourceModel);
    }

    @Override
    public PageInfo findResultData(InsDataSourceModel dataSourceModel) {
        Assert.hasLength(dataSourceModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(dataSourceModel.getProjectId(), "项目id不允许为空");
        this.dataAssembly(dataSourceModel);
        return insProjectInfoDao.findResultData(dataSourceModel);
    }

    @Override
    public Boolean exportRawData(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(insDataSourceModel.getProjectId(), "项目id不允许为空");
        this.dataAssembly(insDataSourceModel);
        ProjectRawDataParamModel build = ProjectRawDataParamModel.builder().build();
        BeanUtils.copyProperties(insDataSourceModel, build);
        build.setLabelTypeLevelFourDisableList(getDisableTagLib(insDataSourceModel.getClientId()));
//        List<InsOriginDataListVo> originDataListVos = insProjectInfoDao.exportProjectRawDataResult(insDataSourceModel);
        try {
            final String taskId = IdWorker.getId();
            build.setTaskId(taskId);
            LocalDateTime now = LocalDateTime.now();
            LocalDate parse = LocalDate.parse(build.getStartTime());
            String channelName = "";
            if (ObjectUtils.isNotEmpty(build.getChannelIdList())) {
                final String channelCode = build.getChannelIdList().stream().findAny().get();
                channelName = channelInfoService.findChannelNameByChannelCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).code(channelCode).build());
                List<String> downChannelByCode = channelInfoService.findDownChannelByCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).channelCodes(ObjectUtils.isNotEmpty(insDataSourceModel.getChannelIdList()) ? insDataSourceModel.getChannelIdList() : List.of()).build());
                build.setChannelIdList(downChannelByCode);
            } else {
                channelName = "全部渠道";
            }
//            final String taskName = "项目-原始-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            final String taskName = "项目-原始-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            build.setFileName(taskName);
            this.exportProjectRawDataResult(build);
            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .taskName(taskName)
                    .type(LargeDigitaFilesType.PROJECT_RAW_RESULT_DATA.getCode())
                    .status(null)
                    .createTime(now)
                    .build());
            return true;
//            ExcelUtil.writeExcel(response, originDataListVos, "原始数据.xlsx", "原始数据", InsOriginDataListVo.class, null);
        } catch (Exception e) {
            log.error("导出原始数据异常:{}", e);
            throw new RuntimeException(e);
        }
    }

    //获取全部禁用的标签
    private Set<String> getDisableTagLib(String clientId) {
        InsTagLibVo allDisableTagLibClient = insTagLibClientService.findAllDisableTagLibClient(InsTagLibClientModel.builder().appClient(clientId).build());
        if (ObjectUtils.isNotEmpty(allDisableTagLibClient) && CollUtil.isNotEmpty(allDisableTagLibClient.getFinalTagLib())) {
            Set<String> collect = allDisableTagLibClient.getFinalTagLib().stream().map(TagLibClientTreeVo::getTagCode).collect(Collectors.toSet());
            log.info("获取全部禁用标签:{}", collect);
            return collect;
        } else {
            log.info("获取全部禁用标签为空");
        }
        return new HashSet<>();
    }

    @Override
    public Boolean exportResultData(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(insDataSourceModel.getProjectId(), "项目id不允许为空");
        try {
            this.dataAssembly(insDataSourceModel);
            ProjectResultDataParamModel build = ProjectResultDataParamModel.builder().build();
            BeanUtils.copyProperties(insDataSourceModel, build);
            build.setLabelTypeLevelFourDisableList(getDisableTagLib(insDataSourceModel.getClientId()));
            final String taskId = IdWorker.getId();
            build.setTaskId(taskId);
            LocalDateTime now = LocalDateTime.now();
            LocalDate parse = LocalDate.parse(build.getStartTime());
            String channelName = "";
            if (ObjectUtils.isNotEmpty(build.getChannelIdList())) {
                final String channelCode = build.getChannelIdList().stream().findAny().get();
                channelName = channelInfoService.findChannelNameByChannelCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).code(channelCode).build());
                List<String> downChannelByCode = channelInfoService.findDownChannelByCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).channelCodes(ObjectUtils.isNotEmpty(insDataSourceModel.getChannelIdList()) ? insDataSourceModel.getChannelIdList() : List.of()).build());
                build.setChannelIdList(downChannelByCode);
            } else {
                channelName = "全部渠道";
            }
//            final String taskName = "项目-结果-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            final String taskName = "项目-结果-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            build.setFileName(taskName);
            this.exportProjectResultData(build);

            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .taskName(taskName)
                    .type(LargeDigitaFilesType.PROJECT_RAW_DATA.getCode())
                    .status(null)
                    .createTime(now)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("导出原始数据异常:{}", e);
            throw new RuntimeException(e);
        }

//        List<InsResultDataListVo> insResultDataListVos = insProjectInfoDao.exportProjectResultData(insDataSourceModel);
//        try {
//            ExcelUtil.writeExcel(response, insResultDataListVos, "结果数据.xlsx", "结果数据", InsResultDataListVo.class, null);
//        } catch (Exception e) {
//            log.error("导出原始数据异常:{}", e);
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public InsDataSourceSearchCriteriaVo findSearchCriteria(InsDataSourceModel dataSourceModel) {
        Assert.hasLength(dataSourceModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(dataSourceModel.getProjectId(), "项目id不允许为空");
        String defaultEndTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String defaultStartTime = LocalDateTime.now().plusDays(-30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        dataSourceModel.setStartTime(defaultStartTime);
        dataSourceModel.setEndTime(defaultEndTime);
        this.dataAssembly(dataSourceModel);
        InsDataSourceSearchCriteriaVo searchCriteria = insProjectInfoDao.findSearchCriteria(dataSourceModel);
        searchCriteria.setDefaultEndTime(defaultEndTime);
        searchCriteria.setDefaultStartTime(defaultStartTime);
        List<TagLibCategoryVo> prod = insTagLibClientService.findTagLibClientCategoryTree(dataSourceModel.getClientId(), TagLibeType.PROD.getCode());
        List<TagLibCategoryVo> service = insTagLibClientService.findTagLibClientCategoryTree(dataSourceModel.getClientId(), TagLibeType.SERVICE.getCode());
        List<TagLibCategoryVo> qy = insTagLibClientService.findTagLibClientCategoryTree(dataSourceModel.getClientId(), TagLibeType.QY.getCode());
        //产品标签
        TagLibCategoryVo prods = TagLibCategoryVo.builder().id(TagLibeType.PROD.getText()).tagName(TagLibeType.PROD.getText()).child(prod).build();
        //服务标签
        TagLibCategoryVo services = TagLibCategoryVo.builder().id(TagLibeType.SERVICE.getText()).tagName(TagLibeType.SERVICE.getText()).child(service).build();
        //品质标签
        TagLibCategoryVo qys = TagLibCategoryVo.builder().id(TagLibeType.QY.getText()).tagName(TagLibeType.QY.getText()).child(qy).build();
        List<TagLibCategoryVo> list = Arrays.asList(prods, services, qys);
        searchCriteria.setTagLibCategoryVos(list);
        //根据项目id获取项目详情
        final List<InsProjectDetailsEntity> projectDetailsEntity = insProjectDetailsDao.findProjectInfo(dataSourceModel.getClientId(), dataSourceModel.getProjectId());
        List<BrandTreeVo> brandVoList = new ArrayList<>();
        List<CompetitiveCarSeriesVo> list1 = new ArrayList<>();
        projectDetailsEntity.stream().forEach(e -> {
            BrandTreeVo brandVos = new BrandTreeVo();
            BrandVo brandVo = mapperService.projectDetailsEntityConvertBrandVo(e);
            brandVos.setBrandCode(brandVo.getBrandCode());
            brandVos.setBrandName(brandVo.getBrandName());
            //本品车系
            List<CarSeriesEntity> carSeries = e.getCarSeries();
            final String jsonString = JSONArray.toJSONString(carSeries);
            final List<CarSeriesVo> carSeriesVos = JSONUtil.toList(jsonString, CarSeriesVo.class);
            List<CarSeriesTreeVo> collect = carSeriesVos.stream().map(k -> {
                return CarSeriesTreeVo.builder()
                        .code(k.getCarSeriesCode())
                        .name(k.getCarSeriesName())
                        .value(k.getCarSeriesName())
                        .build();
            }).collect(Collectors.toList());
            brandVos.setCarSeries(collect);

            //本品车系下全部的竞品车系+竞品品牌
            carSeriesVos.stream().forEach(k -> {
                if (ObjectUtils.isNotEmpty(k.getCompetitiveCarSeries())) {
                    final List<CompetitiveCarSeriesVo> competitiveCarSeries = k.getCompetitiveCarSeries().stream().filter(v -> ObjectUtils.isNotEmpty(v.getCompetitiveCarSeriesCode())).toList();
                    list1.addAll(competitiveCarSeries);
                }
            });
            Map<String, List<CompetitiveCarSeriesVo>> collect1 = list1.stream().collect(Collectors.groupingBy(CompetitiveCarSeriesVo::getCompetitiveCarSeriesCode));
            List<CarSeriesTreeVo> collect2 = collect1.entrySet().stream().map(k -> {
                //竞品品牌
                final String key = k.getKey();
                final List<CompetitiveCarSeriesVo> value = k.getValue();
                CompetitiveCarSeriesVo competitiveCarSeriesVo = value.stream().findAny().orElseGet(null);

                Set<String> set = new HashSet<>();

                List<CarSeriesTreeVo> collect3 = value.stream().filter(v -> set.add(v.getCompetitiveCarSeriesCode())).map(v -> {
                    return CarSeriesTreeVo.builder()
                            .name(v.getCompetitiveCarSeriesName()) //竞品车系名称
                            .value(v.getCompetitiveCarSeriesName()) //竞品车系code
                            .code(v.getCompetitiveCarSeriesCode())
                            .build();
                }).collect(Collectors.toList());

                CarSeriesTreeVo build = CarSeriesTreeVo.builder()
                        .name(competitiveCarSeriesVo.getCompetitiveBrandName())  //竞品车系名称
                        .value(competitiveCarSeriesVo.getCompetitiveBrandName()) //竞品标识
                        .code(key) //品牌code
                        .child(collect3)
                        .build();
                return build;
            }).collect(Collectors.toList());
            brandVos.setCompetitiveCarSeries(collect2);

            //同时提及车系
            if (ObjectUtils.isNotEmpty(searchCriteria.getMentionCarSeriesList())) {
                final List<String> mentionCarSeriesList = searchCriteria.getMentionCarSeriesList();
                final List<CarSeriesTreeVo> brandCarSeriesByCarName = carSeriesInfoService.findBrandCarSeriesByCarName(mentionCarSeriesList);
                brandVos.setMentionCarSeriesList(brandCarSeriesByCarName);
            }
//            List<CarSeriesTreeVo> integrationList = new ArrayList<>();
//            integrationList.addAll(brandVos.getCompetitiveCarSeries());
//            integrationList.addAll(brandVos.getCarSeries());
//            integrationList.addAll(brandVos.getMentionCarSeriesList());
//
//            brandVos.setIntegrationList(integrationList);
            brandVoList.add(brandVos);
        });
        searchCriteria.setBrandVos(brandVoList);
        return searchCriteria;
    }

    @Override
    public List<TagLibCategoryVo> allLibClientCategoryTree(String clientId) {

        List<TagLibCategoryVo> prod = insTagLibClientService.allLibClientCategoryTree(clientId, TagLibeType.PROD.getCode());
        List<TagLibCategoryVo> service = insTagLibClientService.allLibClientCategoryTree(clientId, TagLibeType.SERVICE.getCode());
        List<TagLibCategoryVo> qy = insTagLibClientService.allLibClientCategoryTree(clientId, TagLibeType.QY.getCode());
        //产品标签
        TagLibCategoryVo prods = TagLibCategoryVo.builder().id(TagLibeType.PROD.getText()).tagName(TagLibeType.PROD.getText()).child(prod).build();
        //服务标签
        TagLibCategoryVo services = TagLibCategoryVo.builder().id(TagLibeType.SERVICE.getText()).tagName(TagLibeType.SERVICE.getText()).child(service).build();
        //品质标签
        TagLibCategoryVo qys = TagLibCategoryVo.builder().id(TagLibeType.QY.getText()).tagName(TagLibeType.QY.getText()).child(qy).build();
        List<TagLibCategoryVo> list = Arrays.asList(prods, services, qys);
        return list;
    }

    @Override
    public PageInfo findRiskWarningData(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(insDataSourceModel.getProjectId(), "项目id不允许为空");
        Assert.hasLength(insDataSourceModel.getRiskType(), "风险类型不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(insDataSourceModel.getBrand()), "品牌不允许为空");
        insDataSourceModel.setBrandCode(Arrays.asList(insDataSourceModel.getBrand()));
        return insProjectInfoDao.findRiskWarningData(insDataSourceModel);
    }

    @Override
    public void exportRiskWarningData(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(insDataSourceModel.getProjectId(), "项目id不允许为空");
        Assert.hasLength(insDataSourceModel.getRiskType(), "风险类型不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(insDataSourceModel.getBrand()), "品牌不允许为空");
        insDataSourceModel.setBrandCode(Arrays.asList(insDataSourceModel.getBrand()));
        List<InsRiskWarningResultData> riskWarningResultData = insProjectInfoDao.exportRiskWarningData(insDataSourceModel);
        try {
            ExcelUtil.writeExcel(response, riskWarningResultData, "风险预警数据.xlsx", "风险预警数据", InsRiskWarningResultData.class, null);
        } catch (Exception e) {
            log.error("导出风险预警数据异常:{}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProjectInfoVo> findRiskWarningInfo(InsProjectInfoModel projectInfoModel) {
        Assert.hasLength(projectInfoModel.getClientId(), "客户id不允许为空");
        List<InsProjectInfoEntity> projectList = insProjectInfoDao.findProjectList(projectInfoModel);
        if (ObjectUtils.isEmpty(projectList)) {
            log.info("无项目信息");
            return null;
        }


        List<ProjectInfoVo> projectInfoVos = projectList.stream()
                .filter(e -> e.getId().equals(projectInfoModel.getId()))
                .filter(e -> "1".equalsIgnoreCase(e.getStatus())).map(e -> {
                    final String id = e.getId();
                    final ProjectInfoVo projectInfo = this.findProjectInfo(InsProjectInfoModel.builder().clientId(projectInfoModel.getClientId()).id(id).build());
                    projectInfo.setId(id);
                    projectInfo.setProjectName(e.getProjectName());
                    return projectInfo;
                }).collect(Collectors.toList());
        return projectInfoVos;
    }

    @Override
    public List<BrandVo> findBrandTabLabelByProjectId(InsProjectInfoModel projectInfoModel) {
        Assert.hasLength(projectInfoModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(projectInfoModel.getId(), "项目id不允许为空");
        //获取项目详情
        final List<InsProjectDetailsEntity> projectDetailsEntity = insProjectDetailsDao.findProjectInfo(projectInfoModel.getClientId(), projectInfoModel.getId());
        if(ObjectUtils.isEmpty(projectDetailsEntity)){
            log.info("暂无项目信息");
            return List.of();
        }
        //获取项目标签
        final List<TagLibCategoryVo> tagLib = insTagLibClientService.findTagLibTwoLevel(projectInfoModel.getClientId(), Arrays.asList(1,2));
        //一级标签
        List<TagLibCategoryVo> top = tagLib.stream().filter(e -> "0".equals(e.getTagParentId())).collect(Collectors.toList());
        //二级标签
        Map<String, List<TagLibCategoryVo>> child = tagLib.stream().filter(e -> !"0".equals(e.getTagParentId())).collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
        top.stream().forEach(e->{
            if(ObjectUtils.isNotEmpty(child)&&child.containsKey(e.getId())){
                //二级标签
                final List<TagLibCategoryVo> tagLibCategoryVos = child.get(e.getId());
                e.setChild(tagLibCategoryVos);
            }
        });
        Map<String, List<TagLibCategoryVo>> collect1 = top.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagType));
        List<BrandVo> collect = projectDetailsEntity.stream().map(e -> {
            BrandVo brandVo = BrandVo.builder().brandCode(e.getBrandCode()).brandName(e.getBrandName()).build();
            //标签类型
            e.getTags().stream().forEach(tagType -> {
                if (LabelTypeEnum.PROD.getCode().equalsIgnoreCase(tagType)&&collect1.containsKey(tagType)) {
                    brandVo.setBIZ(collect1.get(tagType));
                } else if (LabelTypeEnum.QY.getCode().equalsIgnoreCase(tagType)&&collect1.containsKey(tagType)) {
                    brandVo.setQY(collect1.get(tagType));
                } else if (LabelTypeEnum.SERVICE.getCode().equalsIgnoreCase(tagType)&&collect1.containsKey(tagType)) {
                    brandVo.setSIC(collect1.get(tagType));
                }
            });
            return brandVo;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<BrandVo> findBrandInfo(InsProjectInfoModel projectInfoModel) {
        Assert.hasLength(projectInfoModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(projectInfoModel.getId(), "项目id不允许为空");
        //获取项目详情
        final List<InsProjectDetailsEntity> projectDetailsEntity = insProjectDetailsDao.findProjectInfo(projectInfoModel.getClientId(), projectInfoModel.getId());

        if(ObjectUtils.isEmpty(projectDetailsEntity)){
            log.info("暂无项目信息");
            return List.of();
        }
        List<CompletableFuture<Void>> futures = new CopyOnWriteArrayList<>();
//        AtomicReference<Map<String, List<TagLibCategoryVo>>> tagLib = new AtomicReference<>();
        //获取项目标签

//        try {
//            CompletableFuture.allOf(futures.stream().toArray(CompletableFuture[]::new)).get(5, java.util.concurrent.TimeUnit.SECONDS);
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//            throw new RuntimeException(ex);
//        }
//        Map<String, List<TagLibCategoryVo>> collect1 = tagLib.get();
        final List<BrandVo> brandVos = projectDetailsEntity.stream().map(e -> {
            BrandVo brandVo = mapperService.projectDetailsEntityConvertBrandVo(e);
            List<CompletableFuture<Void>> futureList = new CopyOnWriteArrayList<>();
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                //车系
                final List<CarSeriesEntity> carSeries = e.getCarSeries();
                final String jsonString = JSONArray.toJSONString(carSeries);
                final List<CarSeriesVo> carSeriesVos = JSONUtil.toList(jsonString, CarSeriesVo.class);
                List<CarSeriesVo> collect = carSeriesVos.stream().sorted(Comparator.comparing(CarSeriesVo::getCarSeriesCode)).collect(Collectors.toList());
                brandVo.setCarSeries(collect);
                return null;
            })));
            //渠道
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                final List<ChannelInfoVo> channelInfoVos = this.getChannalInfoList(projectInfoModel.getClientId(), new HashSet<>(e.getChannel()));
                brandVo.setChannelTree(channelInfoVos);
                //渠道码
                brandVo.setChannelCode(e.getChannel());
                return null;
            })));

            //区域
//            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
//                final List<String> region = e.getRegion();
//                final List<RegionConfigVo> regionTreeByIds = this.getRegionConfigList(projectInfoModel.getClientId(), new HashSet<>(region), brandVo.getBrandName());
//                brandVo.setRegionTree(regionTreeByIds);
//                return null;
//            })));

            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                final List<TagLibCategoryVo> tagLibs = insTagLibClientService.findTagLibTwoLevel(projectInfoModel.getClientId(), Arrays.asList(1,2));
                //一级标签
                final List<TagLibCategoryVo> top = tagLibs.stream().filter(k -> "0".equals(k.getTagParentId())).collect(Collectors.toList());
                //二级标签
                final Map<String, List<TagLibCategoryVo>> child = tagLibs.stream().filter(k -> !"0".equals(k.getTagParentId())).collect(Collectors.groupingBy(TagLibCategoryVo::getTagParentId));
                top.stream().forEach(k->{
                    if(ObjectUtils.isNotEmpty(child)&&child.containsKey(k.getId())){
                        //二级标签
                        final List<TagLibCategoryVo> tagLibCategoryVos = child.get(k.getId());
                        k.setChild(tagLibCategoryVos);
                    }
                });
                final Map<String, List<TagLibCategoryVo>> collect1 = top.stream().collect(Collectors.groupingBy(TagLibCategoryVo::getTagType));
                //标签类型
                e.getTags().stream().forEach(tagType -> {
                    if (LabelTypeEnum.PROD.getCode().equalsIgnoreCase(tagType)&&collect1.containsKey(tagType)) {
                        brandVo.setBIZ(collect1.get(tagType));
                    } else if (LabelTypeEnum.QY.getCode().equalsIgnoreCase(tagType)&&collect1.containsKey(tagType)) {
                        brandVo.setQY(collect1.get(tagType));
                    } else if (LabelTypeEnum.SERVICE.getCode().equalsIgnoreCase(tagType)&&collect1.containsKey(tagType)) {
                        brandVo.setSIC(collect1.get(tagType));
                    }
                });
                return null;
            })));

            try {
                CompletableFuture.allOf(futureList.stream().toArray(CompletableFuture[]::new)).get(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }

            return brandVo;
        }).collect(Collectors.toList());

        return brandVos;
    }

    @Override
    public LargeDigitaFilesModel getFile(LargeDigitaFilesModel model) {
//        Assert.isTrue(StrUtil.isNotBlank(model.getType()), "getType cannot be empty");
        final String userId = ServiceContextHolder.getUserId();
        model.setUserId(userId);
        LargeDigitaFilesModel file = largeDigitaFilesService.getFile(model);
        if (ObjectUtils.isNotEmpty(file)) {
            LargeDigitaFilesType byCode = LargeDigitaFilesType.getByCode(file.getType());
            if (ObjectUtils.isNotEmpty(byCode)) {
                file.setFileName(byCode.getText());
            }
        }
        return file;
    }


    /**
     * @param dataSourceModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/19 下午5:55
     * @描述 数据组装
     **/
    private void dataAssembly(InsDataSourceModel dataSourceModel) {
        final String projectId = dataSourceModel.getProjectId();
        final String clientId = dataSourceModel.getClientId();
        final String brand = dataSourceModel.getBrand();
        ProjectInfoVo projectInfo = this.findProjectInfo(InsProjectInfoModel.builder().clientId(clientId).id(projectId).build());
        final List<BrandVo> brandList = projectInfo.getBrand();
        List<BrandVo> brandVos;
        if (ObjectUtils.isNotEmpty(brand)) {
            brandVos = brandList.stream().filter(e -> e.getBrandName().equals(brand) || e.getBrandCode().equals(brand)).collect(Collectors.toList());
        } else {
            brandVos = brandList;
        }
        List<String> brandCode = new ArrayList<>();
        //本品车系
        List<String> ownCarSeries = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(dataSourceModel.getOwnCarSeries())) {
            ownCarSeries.addAll(dataSourceModel.getOwnCarSeries());
        }
        //竞品车系
        List<String> competitorsCarSeries = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(dataSourceModel.getCompetitorsCarSeries())) {
            competitorsCarSeries.addAll(dataSourceModel.getCompetitorsCarSeries());
        }
        //同时提及车系
        List<String> mentionCarSeriesList = dataSourceModel.getMentionCarSeriesList();
        List<String> cityCode = new ArrayList<>();
        List<String> channel = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(dataSourceModel.getChannelIdList())) {
            channel.addAll(dataSourceModel.getChannelIdList());
        }
        List<String> dataSource = new ArrayList<>();
        //业务末级标签
        final List<String> businessEndTag = dataSourceModel.getBusinessEndTag();
        //质量末级标签
        final List<String> qualityEndTag = dataSourceModel.getQualityEndTag();
        List<String> bTag = new ArrayList<>();
        List<String> qTag = new ArrayList<>();
        List<String> lableTypeList = new ArrayList<>();
        //获取全部区域详情数据
//        List<AreaModel> allRegionDetail = regionConfigService.findRegionAllList(InsRegionConfigModel.builder().clientId(clientId).build());
//        Map<String, AreaModel> collect2 = allRegionDetail.stream().collect(Collectors.toMap(AreaModel::getAreaCode, Function.identity()));
        brandVos.stream().forEach(e -> {
            //渠道
            final List<String> channels = e.getChannel();
            if (ObjectUtils.isEmpty(channel)) {
                channel.addAll(channels);
            }
            if (CollUtil.isNotEmpty(e.getBIZ())) {
                lableTypeList.add(LabelTypeEnum.PROD.getCode());
            }
            if (CollUtil.isNotEmpty(e.getSIC())) {
                lableTypeList.add(LabelTypeEnum.SERVICE.getCode());
            }
            if (CollUtil.isNotEmpty(e.getQY())) {
                lableTypeList.add(LabelTypeEnum.QY.getCode());
            }
            //区域
            final List<String> region1 = e.getRegion();
            cityCode.addAll(region1);
            //本品品牌
            brandCode.add(e.getBrandName());
            //车系
            final List<CarSeriesVo> carSeries = e.getCarSeries();
            //本品车系
            if (ObjectUtils.isEmpty(ownCarSeries)) {
                ownCarSeries.addAll(carSeries.stream().map(CarSeriesVo::getCarSeriesName).collect(Collectors.toList()));
            }

            //竞品
            final List<CompetitiveProductVo> competitiveProduct = e.getCompetitiveProduct();
            //竞品品牌
//            Set<String> collect = competitiveProduct.stream().map(k -> k.getCompetitiveBrandName()).collect(Collectors.toSet());
//            brandCode.addAll(collect);
            if (ObjectUtils.isEmpty(competitorsCarSeries)) {
                carSeries.stream().forEach(k -> {
                    //竞品车系
                    final List<CompetitiveCarSeriesVo> competitiveCarSeries = k.getCompetitiveCarSeries();
                    final Set<String> collect1 = competitiveCarSeries.stream().filter(v -> ObjectUtils.isNotEmpty(v.getCompetitiveCarSeriesCode())).map(CompetitiveCarSeriesVo::getCompetitiveCarSeriesName).collect(Collectors.toSet());
                    competitorsCarSeries.addAll(collect1);
                });
            }

            //数据源
            final List<String> dataSource1 = e.getDataSource();
            if (dataSource1.contains(this.dataSourceID)) {
                dataSourceModel.setDateType(this.dataSourceID);
            }
            dataSource.addAll(dataSource1);
        });
        if (ObjectUtils.isNotEmpty(businessEndTag)) {
            List<TagLibCategoryVo> downTagLibHierarchical = insTagLibClientService.findDownTagLibHierarchical(InsTagLibClientModel.builder().appClient(dataSourceModel.getClientId()).tagParentIds(businessEndTag).build());
            bTag.addAll(downTagLibHierarchical.stream().map(TagLibCategoryVo::getTagCode).collect(Collectors.toList()));
        }
        if (ObjectUtils.isNotEmpty(qualityEndTag)) {
            List<TagLibCategoryVo> downTagLibHierarchical = insTagLibClientService.findDownTagLibHierarchical(InsTagLibClientModel.builder().appClient(dataSourceModel.getClientId()).tagParentIds(qualityEndTag).build());
            qTag.addAll(downTagLibHierarchical.stream().map(TagLibCategoryVo::getTagCode).collect(Collectors.toList()));
        }
        dataSourceModel.setLabelTypeList(lableTypeList);
        dataSourceModel.setOwnCarSeries(ownCarSeries);
        dataSourceModel.setCompetitorsCarSeries(competitorsCarSeries);
        dataSourceModel.setMentionCarSeriesList(mentionCarSeriesList);
        dataSourceModel.setCityCodeList(cityCode);
        dataSourceModel.setChannelIdList(channel);
        dataSourceModel.setBusinessEndTag(bTag);
        dataSourceModel.setQualityEndTag(qTag);
        dataSourceModel.setBrandCode(brandCode);
        Set<String> workIds = dataSourceService.findAllWorkIdByDataSourceIds(clientId, null);
        dataSourceModel.setWorkIdList(workIds);
        BrandVo brandVo = brandVos.stream().findFirst().get();
        dataSourceModel.setBrand(brandVo.getBrandName());
    }

    /**
     * 导出原始数据 项目原始数据
     *
     * @param paramModel
     * @throws Exception
     */
    private void exportProjectRawDataResult(ProjectRawDataParamModel paramModel) throws Exception {
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                metaDataAnalysisService.exportProjectRawDataResultTask(paramModel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * 导出项目结果数据
     *
     * @param paramModel
     * @throws Exception
     */
    private void exportProjectResultData(ProjectResultDataParamModel paramModel) throws Exception {
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                aysPostprocessDataService.exportProjectResultDataTask(paramModel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * @param insProjectInfoModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/14 上午10:22
     * @描述 参数校验
     **/
    private void checkParams(InsProjectInfoModel insProjectInfoModel) {
        Assert.hasLength(insProjectInfoModel.getClientId(), "客户id不允许为空");
        Assert.hasLength(insProjectInfoModel.getProjectName(), "项目名称不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(insProjectInfoModel.getBrand()), "品牌不允许为空");
        final List<BrandModel> brand = insProjectInfoModel.getBrand();
        brand.stream().forEach(e -> {
            if (ObjectUtils.isEmpty(e.getBrandCode())) {
                throw new RuntimeException("本品品牌编码不允许为空");
            }
            final List<String> tags = e.getTags();
            if (ObjectUtils.isEmpty(tags)) {
                throw new RuntimeException("标签不允许为空");
            }
            final List<String> dataSource = e.getDataSource();
            if (ObjectUtils.isEmpty(dataSource)) {
                throw new RuntimeException("数据源不允许为空");
            }
            final List<String> channel = e.getChannel();
            if (ObjectUtils.isEmpty(channel)) {
                throw new RuntimeException("渠道不允许为空");
            }
            final List<String> region = e.getRegion();
            if (ObjectUtils.isEmpty(region)) {
                throw new RuntimeException("区域不允许为空");
            }
            final List<CarSeriesModel> carSeries = e.getCarSeries();
            final List<CarSeriesModel> carSeriesList = carSeries.stream().filter(k -> ObjectUtils.isEmpty(k.getCarSeriesCode())).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(carSeriesList)) {
                throw new RuntimeException("本品车系不允许为空");
            }
            final List<InsRiskEarlyWarning> riskEarlyWarningInfo = e.getRiskEarlyWarning();
            if (ObjectUtils.isNotEmpty(riskEarlyWarningInfo)) {
                riskEarlyWarningInfo.stream().forEach(k -> {
                    final List<InsRiskSetting> riskSetting = k.getRiskSetting();
                    final List<InsRiskLevel> riskLevel = k.getRiskLevel();
                    if (ObjectUtils.isEmpty(riskSetting)) {
                        throw new RuntimeException("预警设置不允许为空");
                    }
                    final List<InsRiskSetting> collect = riskSetting.stream().filter(y -> Boolean.TRUE.equals(y.getIsApply())).collect(Collectors.toList());
                    if (ObjectUtils.isEmpty(collect)) {
                        throw new RuntimeException("预警设置需应用一个或多个");
                    }
                    collect.stream().forEach(y -> {
                        if (ObjectUtils.isEmpty(y.getNegative())) {
                            throw new RuntimeException("负面提及量");
                        }
                        if (ObjectUtils.isEmpty(y.getComplaint())) {
                            throw new RuntimeException("投诉提及量");
                        }
                        if (ObjectUtils.isEmpty(y.getRiskWords())) {
                            throw new RuntimeException("风险词提及量");
                        }
                        if (ObjectUtils.isEmpty(y.getChannelNum())) {
                            throw new RuntimeException("发声渠道不允许为空");
                        }
                        if (ObjectUtils.isEmpty(y.getAffective())) {
                            throw new RuntimeException("净情感值不允许为空");
                        }
                    });
                    if (ObjectUtils.isEmpty(riskLevel)) {
                        throw new RuntimeException("预警等级不允许为空");
                    }
                    final List<InsRiskLevel> collect1 = riskLevel.stream().filter(y -> Boolean.TRUE.equals(y.getIsApply())).collect(Collectors.toList());
                    if (ObjectUtils.isEmpty(collect1)) {
                        throw new RuntimeException("预警等级需应用一个或多个");
                    }
                    collect1.stream().forEach(y -> {
                        if (ObjectUtils.isEmpty(y.getStartValue())) {
                            throw new RuntimeException("预警等级开始G值不允许为空");
                        }
                        if (ObjectUtils.isEmpty(y.getEndValue())) {
                            throw new RuntimeException("预警等级结束G值不允许为空");
                        }
                    });
                });
            } else {
                throw new RuntimeException("预警设置不允许为空");
            }
        });
        Assert.hasLength(insProjectInfoModel.getStatus(), "项目状态不允许为空");
        Assert.isTrue(insProjectInfoModel.getStatus().equalsIgnoreCase("1")
                || insProjectInfoModel.getStatus().equalsIgnoreCase("0"), "状态码无效");
    }

}
