package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.ttl.TtlWrappers;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.api.IUploadFileService;
import com.voc.service.common.model.UploadModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.StopWatch;
import com.voc.service.common.util.StringUtil;
import com.voc.service.insights.engine.api.*;
import com.voc.service.insights.engine.api.data.IInsDataSourceService;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.api.model.RawDataParamModel;
import com.voc.service.insights.engine.api.model.ResultDataParamModel;
import com.voc.service.insights.engine.common.config.DynamicParameterMapping;
import com.voc.service.insights.engine.common.util.ExcelUtil;
import com.voc.service.insights.engine.dao.InsChannelInfoDao;
import com.voc.service.insights.engine.data.dao.InsDataSourceDao;
import com.voc.service.insights.engine.data.dao.InsDataSourceDetailDao;
import com.voc.service.insights.engine.data.dao.InsDataSourceTemplateDao;
import com.voc.service.insights.engine.data.dao.InsSIDataSourceDao;
import com.voc.service.insights.engine.data.entity.InsDataSourceDescEntity;
import com.voc.service.insights.engine.data.entity.InsDataSourceEntity;
import com.voc.service.insights.engine.data.entity.InsDataSourceTemplateEntity;
import com.voc.service.insights.engine.data.entity.InsSIDataSourceEntity;
import com.voc.service.insights.engine.data.impl.converts.InsDataSourceConvertService;
import com.voc.service.insights.engine.data.listener.ExcelListener;
import com.voc.service.insights.engine.data.mapper.AysPostprocessDataMapper;
import com.voc.service.insights.engine.data.mapper.InsDataSourceMapper;
import com.voc.service.insights.engine.data.mapper.InsDataSourceDescMapper;
import com.voc.service.insights.engine.entity.InsChannelInfoEntity;
import com.voc.service.insights.engine.enums.*;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.model.InsDataSourceRequestModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 数据源集(InsDataSource)表服务实现类
 *
 * @author leiww
 * @since 2024-02-27 15:31:46
 */
@Service
public class InsDataSourceServiceImpl extends ServiceImpl<InsDataSourceMapper, InsDataSourceEntity> implements IInsDataSourceService {
    private static final Logger log = LoggerFactory.getLogger(InsDataSourceServiceImpl.class);
    @Resource
    private InsDataSourceConvertService convertService;
    @Autowired
    IUploadFileService uploadFileService;
    @Autowired
    IInsChannelInfoService iInsChannelInfoService;
    @Autowired
    InsDataSourceTemplateDao insDataSourceTemplateDao;
    @Autowired
    InsDataSourceDao insDataSourceDao;
    @Autowired
    InsDataSourceDescMapper insDataSourceDescMapper;
    @Autowired
    InsDataSourceDetailDao insDataSourceDetailDao;
    @Autowired
    InsChannelInfoDao channelDao;
    @Value("${default.common.clientId:764547797eb2e192763f5334028d49c9}")
    String commonClientId;

    @Value("${default.common.commonDataSourceId:1850720952091041794}")
    String commonDataSourceId;
    @Autowired
    ILargeDigitaFilesService largeDigitaFilesService;
    @Autowired
    IInsChannelInfoService channelInfoService;
    @Autowired
    AysExtAttrsMappingValuesService extAttrsMappingValuesService;
    @Autowired
    AysPostprocessDataMapper aysPostprocessDataMapper;

    private static final String START_PROCESSING_KEY = "{}:startProcessing:{}";
    private static final String DATA_SOURCE_RESULT_KEY = "{}:dataSourceResult:{}";
    private static final String DATA_SOURCE_TEMPLATE_KEY = "{}:dataSourceTemplate:{}";

    @CreateCache(area = "VDP", name = ":", cacheType = CacheType.REMOTE)
    private Cache<String, String> startProcessingCache;
    @Autowired
    private DynamicParameterMapping dynamicParameterMapping;
    //本地缓存 若不手动清除，则5分钟后过期
//    @CreateCache(area = "VDP", name = ":",  cacheType = CacheType.LOCAL,expire = 60000*5)
    @CreateCache(name = "dataSourceTemplate_", cacheType = CacheType.LOCAL, expire = 60000 * 5)
    private Cache<String, List<InsDataSourceTemplateEntity>> dataSourceTemplateCache;

    @CreateCache(area = "VDP", name = ":", cacheType = CacheType.REMOTE)
    private Cache<String, String> dataSourceResultCache;
    @Autowired
    IInsBasicInfoService basicInfoService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    InsSIDataSourceDao siDataSourceDao;
    @Autowired
    IAysPostprocessDataService aysPostprocessDataService;
    @Value("${ins.project.id}")
    private String projectId;


    private String getStartProcessingKey(String... params) {
        return StrUtil.format(START_PROCESSING_KEY, ServiceContextHolder.getSystemId(), params);
    }

    private String getDataSourceTemplateKey(String... params) {
        return StrUtil.format(DATA_SOURCE_TEMPLATE_KEY, ServiceContextHolder.getSystemId(), params);
    }

    private String getDataSourceResultKey(String... params) {
        return StrUtil.format(DATA_SOURCE_RESULT_KEY, ServiceContextHolder.getSystemId(), params);
    }
    @Override
    public UploadModel uploadDataSource(MultipartFile file) throws IOException {
        Assert.isTrue(!file.isEmpty(), "文件未上传");

        //获取文件后缀
        final String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        if(!Arrays.asList("xlsx","xls").contains(suffix.toLowerCase())){
            return null;
        }
        //重新拼接文件名称
        final String fileName_ = IdWorker.getId().concat(".").concat(suffix);
        UploadModel model = new UploadModel();
        model.setKey(fileName_);
        //开启文件输入流
        @Cleanup
        InputStream fileIs = file.getInputStream();
        //将文件上传至minio指定的桶中
        uploadFileService.putObject(this.getFileName(fileName_), fileIs);
        log.debug("文件上传地址：{}",this.getFileName(fileName_));
        return model;
    }

    @SneakyThrows
    @Override
    public void downloadDataSource(HttpServletResponse response, String clientId, String fileName) {
        List<InsDataSourceTemplateVo> list = new ArrayList<>();
        try {
            InsChannelInfoModel insChannelInfoModel = new InsChannelInfoModel();
            insChannelInfoModel.setType(ChannelType.CHANNEL.getCode());
            insChannelInfoModel.setClientId(clientId);
            List<InsChannelInfoEntity> allChannelCategory = channelDao.findChannel(insChannelInfoModel);
            Map<Integer, List<String>> map = null;
            if (ObjectUtils.isNotEmpty(allChannelCategory)) {
                List<String> collect = allChannelCategory.stream().map(InsChannelInfoEntity::getName).collect(Collectors.toList());
                map = new HashMap<>();
                map.put(5, collect);
            }
            final String suffix = FileUtil.getSuffix(fileName);
            if ("xlsx".equals(suffix)) {
                ExcelUtil.writeExcel(response, list, fileName, fileName.substring(0, fileName.lastIndexOf(".")), InsDataSourceTemplateVo.class, map);
            } else {
                ExcelUtil.writeExcel(response, list, fileName, fileName.substring(0, fileName.lastIndexOf(".")), InsDataSourceTemplateVo.class, map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InsDataSourceValidateVo checkUploadDataSource(InsDataSourceModel insDataSourceModel) throws Exception {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getFileName(), "文件名称不允许为空");
        InsDataSourceValidateVo dataSourceValidateVo = new InsDataSourceValidateVo();
        String message = "";
        Map<String, Object> map = new ConcurrentHashMap<>();
        final String batchId = IdWorker.getId();
        AtomicInteger fail = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        final String username = ServiceContextHolder.getUsername();
        final String clientId = insDataSourceModel.getClientId();
        List<ChannelInfoVo> allChannelInfo = iInsChannelInfoService.findAllChannelInfo(InsChannelInfoModel.builder().clientId(clientId).build());
        log.debug("获取全部渠道",allChannelInfo);
//        List<ProvinceAreaVo> provinceAreaVos = basicInfoService.findAll();
        try {
            log.debug("开始读取minio中的文件");
            @Cleanup
            InputStream objectInputStream = uploadFileService.getObjectInputStream(this.getFileName(insDataSourceModel.getFileName()));
            long start = System.currentTimeMillis();
            log.debug("开始解析文件");
            EasyExcel.read(objectInputStream, InsDataSourceTemplateVo.class, new ExcelListener(this, map, batchId, insDataSourceModel.getClientId(), fail, success, allChannelInfo, null)).sheet().doRead();
            long end = System.currentTimeMillis();
            log.info("读取耗时：{}", TimeUnit.MILLISECONDS.toSeconds(end - start) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end - start) + "秒" : (end - start) + "毫秒");
            long start1 = System.currentTimeMillis();
            Integer failTotal = ObjectUtils.isNotEmpty(map.get("fail")) ? Integer.valueOf(map.get("fail").toString()) : 0;
            Integer successTotal = ObjectUtils.isNotEmpty(map.get("success")) ? Integer.valueOf(map.get("success").toString()) : 0;
            List<InsDataSourceTemplateEntity> dataSourceTemplateEntityList = ObjectUtils.isNotEmpty(map.get("dataSource")) ? (List<InsDataSourceTemplateEntity>) map.get("dataSource") : List.of();
            Integer total = failTotal + successTotal;
            dataSourceTemplateEntityList.stream().forEach(e -> {
                e.setFailNum(Long.valueOf(failTotal));
                e.setSuccessNum(Long.valueOf(successTotal));
                e.setTotalNum(Long.valueOf(total));
                e.setCreateUser(username);
            });
            dataSourceValidateVo.setBatchId(batchId);
            dataSourceValidateVo.setSuccess(String.valueOf(successTotal));
            dataSourceValidateVo.setTotal(String.valueOf(total));
            if (successTotal.equals(total)) {
                message = "校验完成，共：" + total + "条，有效数据：" + successTotal + "条";
            } else if (successTotal < total && successTotal > 0) {
                message = "校验完成，共：" + total + "条，有效数据：" + successTotal + "条，系统仅导入有效数据";
            } else if (successTotal == 0) {
                message = "校验完成，共：" + total + "条，有效数据：" + success + "条,请重新上传";
            }
            dataSourceValidateVo.setMessage(message);
            long end1 = System.currentTimeMillis();
            log.info("数据组装耗时：{}", TimeUnit.MILLISECONDS.toSeconds(end1 - start1) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end1 - start1) + "秒" : (end1 - start1) + "毫秒");
            dataSourceTemplateCache.put(this.getDataSourceTemplateKey(batchId), dataSourceTemplateEntityList);
        } catch (Exception e) {
            log.error("{}",e);
            throw new Exception("校验失败，请重新校验");
        }
        return dataSourceValidateVo;
    }

    @Override
    public Map<String, Object> analyzeExcelData(List<InsDataSourceTemplateVo> list, String batchId, String clientId, AtomicInteger fail, AtomicInteger success, Map<String, Object> map, List<ChannelInfoVo> allChannelInfo, List<ProvinceAreaVo> proviceAndCityInfo) {
        if (ObjectUtils.isNotEmpty(list)) {
            log.debug("获取数据源信息");
            List<InsDataSourceTemplateEntity> dataSourceEntities;
            if (map.containsKey("dataSource")) {
                dataSourceEntities = (List<InsDataSourceTemplateEntity>) map.get("dataSource");
            } else {
                dataSourceEntities = new ArrayList<>();
            }
            Map<String, List<ChannelInfoVo>> collect = allChannelInfo.stream().collect(Collectors.groupingBy(ChannelInfoVo::getName));
            log.debug("根据渠道名称进行分组:{}", collect);
//            Map<String, List<ProvinceAreaVo>> provinceAndCity = proviceAndCityInfo.stream().collect(Collectors.groupingBy(ProvinceAreaVo::getProvinceName));
            long start1 = System.currentTimeMillis();
            list.stream().forEach(e -> {
                InsDataSourceTemplateEntity insDataSourceEntity = convertService.dataSourceTemplateVoToEntity(e);
                insDataSourceEntity.setNewId(IdWorker.getId());
                insDataSourceEntity.setId(IdWorker.getId());
                insDataSourceEntity.setBatchId(batchId);
                insDataSourceEntity.setCreateTime(LocalDateTime.now());
                log.debug("对象转换:{}",insDataSourceEntity);
                if (ObjectUtils.isEmpty(e.getChannel()) || ObjectUtils.isEmpty(e.getVoc_content()) || ObjectUtils.isEmpty(e.getVoc_process_at())) {
                    log.error("当前数据无效,必填项:[{}]为空", ObjectUtils.isEmpty(e.getChannel()) ? "channel" : ObjectUtils.isEmpty(e.getDeal_content1()) ? "content" : ObjectUtils.isEmpty(e.getVoc_process_at()) ? "publishTime" : "");
                    insDataSourceEntity.setDataValidity("0");
                    fail.getAndIncrement();
                } else {
                    if (collect.containsKey(e.getChannel())) {
                        final List<ChannelInfoVo> channelInfoVos = collect.get(e.getChannel());
                        if (ObjectUtils.isNotEmpty(channelInfoVos)) {
                            final String channelId = channelInfoVos.get(0).getCode();
                            insDataSourceEntity.setChannelId(channelId);
                            success.getAndIncrement();
                        } else {
                            log.error("当前数据无效,渠道[{}]不存在与当前客户中", e.getChannel());
                            insDataSourceEntity.setDataValidity("0");
                            fail.getAndIncrement();
                        }
                    } else {
                        log.error("当前数据无效,渠道[{}]不存在与当前客户中", e.getChannel());
                        insDataSourceEntity.setDataValidity("0");
                        fail.getAndIncrement();
                    }
                }
                dataSourceEntities.add(insDataSourceEntity);
            });
            long end1 = System.currentTimeMillis();
            log.info("数据源数据批量处理完成，总耗时:{}", TimeUnit.MILLISECONDS.toSeconds(end1 - start1) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end1 - start1) + "秒" : (end1 - start1) + "毫秒");
            map.put("dataSource", dataSourceEntities);
        }
        map.put("fail", fail.get());
        map.put("success", success.get());
        return map;
    }

    @Override
    public void saveUploadDataSource(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源id不允许为空");
        Assert.hasLength(insDataSourceModel.getDataName(), "数据名称不允许为空");
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getFileName(), "文件名称不允许为空");
        Assert.hasLength(insDataSourceModel.getBatchId(), "数据源批次id不允许为空");
        List<InsDataSourceTemplateEntity> dataSourceTemplateEntityList = dataSourceTemplateCache.get(this.getDataSourceTemplateKey(insDataSourceModel.getBatchId()));
        Assert.notEmpty(dataSourceTemplateEntityList, "当前批次的数据源中不存在数据");
        String dataSourceDetail = insDataSourceDetailDao.findDataSourceName(insDataSourceModel.getClientId(), insDataSourceModel.getDataSourceId(), insDataSourceModel.getDataName());
        Assert.isTrue(ObjectUtils.isEmpty(dataSourceDetail), "当前数据名称已存在");
//        //保存数据源信息
        List<InsDataSourceDescEntity> collect = dataSourceTemplateEntityList.stream().map(e -> {
            //转换json对象
            final String json = JSON.toJSONString(e, SerializerFeature.WriteMapNullValue);
            final JSONObject jsonObject = JSON.parseObject(json);
//            String compress = StringUtil.uncompress(String.valueOf(jsonObject));
//            JSONObject object = new JSONObject();
//            jsonObject.entrySet().stream().forEach(k -> {
//                final String key = k.getKey();
//                final Object value = k.getValue();
//                if ("content".equalsIgnoreCase(key)) {
//                    String compress = StringUtil.uncompress(String.valueOf(value));
//                    object.put(key, compress);
//                } else if ("URL".equalsIgnoreCase(key) || "channelId".equalsIgnoreCase(key)) {
//                    object.put(key, ObjectUtils.isEmpty(value) ? "" : value);
//                } else {
//                    final String underlineCase = StrUtil.toUnderlineCase(key);
//                    object.put(underlineCase, ObjectUtils.isEmpty(value) ? "" : value);
//                }
//            });
            jsonObject.put("type", insDataSourceModel.getDataSourceType());
            jsonObject.put("clientId", insDataSourceModel.getClientId());
            String compress = StringUtil.compress(JSONObject.toJSONString(jsonObject));
            InsDataSourceDescEntity build = InsDataSourceDescEntity.builder()
                    .newId(IdWorker.getId())
                    .content(compress)
                    .dataName(insDataSourceModel.getDataName())
                    .dataSourceId(insDataSourceModel.getDataSourceId())
                    .batchId(insDataSourceModel.getBatchId())
                    .status("0")
                    .createTime(LocalDateTime.now())
                    .workId(insDataSourceModel.getWorkId())
                    .totalNum(e.getTotalNum())
                    .successNum(e.getSuccessNum())
                    .failNum(e.getFailNum())
                    .createTime(LocalDateTime.now())
                    .dataValidity(e.getDataValidity())
                    .build();
            return build;
        }).collect(Collectors.toList());
        //保存数据源详情
        List<List<InsDataSourceDescEntity>> split;
        if (collect.size() > 4) {
            split = CollUtil.split(collect, collect.size() / 4);
        } else {
            split = CollUtil.split(collect, 1);
        }
        List<CompletableFuture<Void>> futureList = new CopyOnWriteArrayList<>();
        split.stream().forEach(sub -> {
            futureList.add(CompletableFuture.supplyAsync(TtlWrappers.wrap(() -> {
                insDataSourceDetailDao.saveBatchDataSourceDetail(sub, insDataSourceModel.getClientId());
                return null;
            })));
        });
        try {
            CompletableFuture.allOf(futureList.stream().toArray(CompletableFuture[]::new)).get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        uploadFileService.removeObject(this.getFileName(insDataSourceModel.getFileName()));
        dataSourceTemplateCache.remove(this.getDataSourceTemplateKey(insDataSourceModel.getBatchId()));
    }

    @Override
    public void saveDataSource(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getDataSourceName(), "数据源名称不允许为空");
//        Assert.hasLength(insDataSourceModel.getDataSourceType(),"数据源类型不允许为空");
//        Assert.hasLength(insDataSourceModel.getDataSourceAccessWay(),"数据源接入方式不允许为空");
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
//        Assert.isTrue(ObjectUtils.isNotEmpty(insDataSourceModel.getLabelType()),"数据源标签类型不允许为空");
//        Assert.hasLength(insDataSourceModel.getModelType(),"数据源处理模型不允许为空");
        InsDataSourceEntity dataSourceByName = insDataSourceDao.findDataSourceByName(insDataSourceModel.getClientId(), insDataSourceModel.getDataSourceName());
        Assert.isTrue(ObjectUtils.isEmpty(dataSourceByName), "数据源名称已存在");
        InsDataSourceEntity insDataSourceEntity = convertService.convertTo(insDataSourceModel);
        final String username = ServiceContextHolder.getUsername();
        insDataSourceEntity.setCreateUser(username);
        insDataSourceDao.saveDataSource(insDataSourceEntity);
    }

    @Override
    public void updateDataSource(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getDataSourceName(), "数据源名称不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceType(), "数据源类型不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceAccessWay(), "数据源接入方式不允许为空");
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getId(), "数据源ID不允许为空");
        Assert.isTrue(ObjectUtils.isNotEmpty(insDataSourceModel.getLabelType()), "数据源标签类型不允许为空");
        Assert.hasLength(insDataSourceModel.getModelType(), "数据源处理模型不允许为空");
        InsDataSourceEntity dataSourceById = insDataSourceDao.findDataSourceById(insDataSourceModel.getClientId(), insDataSourceModel.getId());
        Assert.isTrue(ObjectUtils.isNotEmpty(dataSourceById), "数据源不存在");
        List<InsDataSourceDescEntity> dataSourceDetail = insDataSourceDetailDao.findDataSourceDetail(insDataSourceModel.getClientId(), insDataSourceModel.getId());
        if (dataSourceById.getDataSourceType().equalsIgnoreCase(insDataSourceModel.getDataSourceType()) && dataSourceById.getDataSourceAccessWay().equalsIgnoreCase(insDataSourceModel.getDataSourceAccessWay())) {
            dataSourceById.setClientId(insDataSourceModel.getClientId());
            dataSourceById.setDataSourceName(insDataSourceModel.getDataSourceName());
            dataSourceById.setLabelType(insDataSourceModel.getLabelType());
            dataSourceById.setModelType(insDataSourceModel.getModelType());
            insDataSourceDao.updateDataSource(dataSourceById);
        } else {
            Assert.isTrue(ObjectUtils.isEmpty(dataSourceDetail), "该数据源下存在数据详情，不允许修改数据源类型及数据源接入方式");
        }
    }

    @Override
    public PageInfo findDataSource(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        PageHelper.startPage(insDataSourceModel.getPageNum(), insDataSourceModel.getPageSize());
        List<InsDataSourceEntity> dataSource = insDataSourceDao.findDataSource(insDataSourceModel);
        if (ObjectUtils.isEmpty(dataSource)) {
            log.info("暂无数据源信息");
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(dataSource);
        List<InsDataSourceVo> insDataSourceVos = convertService.dataSourceEntityToVo(dataSource);
        pageInfo.setList(insDataSourceVos);
        return pageInfo;
    }

    @Override
    public PageInfo findDataProcessingTasks(DataProcessingTaskQuery query) {
        Assert.hasLength(query.getClientId(), "客户ID不允许为空");
        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        return new PageInfo<>(insDataSourceDescMapper.findDataProcessingTasks(query));
    }

    @Override
    public PageInfo findDataSourceDetail(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源id不允许为空");
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        PageHelper.startPage(insDataSourceModel.getPageNum(), insDataSourceModel.getPageSize());
        StopWatch watch = new StopWatch();
        watch.start("findDataSourceDetail");
        //根据数据源id获取batchId
        List<InsDataSourceDescEntity> dataSourceDetail = insDataSourceDetailDao.findDataSourceDetail(insDataSourceModel.getClientId(), insDataSourceModel.getDataSourceId());
        if (ObjectUtils.isEmpty(dataSourceDetail)) {
            log.info("暂无数据源详情信息");
            return new PageInfo();
        }
        watch.stop();
        watch.start("findDataSourceDetailsByBatchIds");
        List<String> batchIds = dataSourceDetail.stream().map(e -> e.getBatchId()).collect(Collectors.toList());

        //根据batchId获取批次信息
        List<InsDataSourceDescEntity> dataSourceDetailsByBatchIds = insDataSourceDetailDao.findDataSourceDetailsByBatchIds(insDataSourceModel.getClientId(), batchIds, insDataSourceModel.getDataSourceId());
        watch.stop();
        watch.start("findDataSourceDetailMaxStatus");
        //根据batchId获取每个批次的最大状态值
        List<InsDataSourceDescEntity> dataSourceDetailMaxStatus = insDataSourceDetailDao.findDataSourceDetailMaxStatus(insDataSourceModel.getClientId(), batchIds);
        watch.stop();
        watch.start("findFailDataSourceDetails");
        Map<String, String> collect2 = dataSourceDetailMaxStatus.stream().collect(Collectors.toMap(k -> k.getBatchId(), v -> v.getSumTotal(), (v1, v2) -> v1));

        //根据batchId获取每个批次的失败数据
        List<InsDataSourceDescEntity> dataSourceDetails = insDataSourceDetailDao.findFailDataSourceDetails(insDataSourceModel.getClientId(), batchIds, insDataSourceModel.getDataSourceId(), Arrays.asList("-1"));
        Map<String, InsDataSourceDescEntity> collect1 = dataSourceDetails.stream().collect(Collectors.toMap(k -> k.getBatchId(), v -> v, (v1, v2) -> v1));
        watch.stop();
        watch.start("数据处理");
        PageInfo pageInfo = new PageInfo(dataSourceDetail);
        List<InsDataSourceDescVo> collect = dataSourceDetailsByBatchIds.stream().map(e -> {
            final String states = collect2.get(e.getBatchId());
            InsDataSourceDescVo insDataSourceDescVo = InsDataSourceDescVo.builder()
                    .dataName(e.getDataName())
                    .batchId(e.getBatchId())
                    .createTime(e.getCreateTime())
                    .status(e.getSumTotal())
                    .processible(true)
                    .status(states)
                    .build();
            //获取批次下所有失败数据
            InsDataSourceDescEntity dataSourceDetailAll = collect1.get(e.getBatchId());
//            List<InsDataSourceDescEntity> dataSourceDetailAll = insDataSourceDetailDao.findDataSourceDetailAll(insDataSourceModel.getClientId(), e.getBatchId(), insDataSourceModel.getDataSourceId(), Arrays.asList("-1"));
            String result = "共" + e.getTotalNum() + "条 |   入库" + e.getSuccessNum() + "条";
            if (ObjectUtils.isNotEmpty(dataSourceDetailAll)) {
                result = result.concat(" |   处理").concat((e.getSuccessNum() - dataSourceDetailAll.getFailNum()) + "条");
                insDataSourceDescVo.setFail(true);
                if (e.getSuccessNum() - dataSourceDetailAll.getFailNum() == 0) {
                    insDataSourceDescVo.setStatus("2");
                }
            } else if ("2".equalsIgnoreCase(states)) {
                result = result.concat(" |   处理").concat(e.getSuccessNum() + "条");
            }
            insDataSourceDescVo.setImportResult(result);
            if (!"0".equalsIgnoreCase(states)) {
                insDataSourceDescVo.setProcessible(false);
            }
            if (e.getFailNum().longValue() > 0) {
                insDataSourceDescVo.setInvalid(true);
            }
            return insDataSourceDescVo;
        }).collect(Collectors.toList());
        watch.stop();
        pageInfo.setList(collect);
        watch.stop();
        log.debug("findDataSourceDetail.watch:{}", watch.prettyPrint());
        return pageInfo;
    }

    @Override
    public void deleteDataSourceDetail(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getBatchId(), "批次ID不允许为空");
        insDataSourceDetailDao.deleteDataSourceDetail(insDataSourceModel.getClientId(), insDataSourceModel.getBatchId());
    }

    @Override
    public void deleteDataSource(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getId(), "数据源ID不允许为空");
        List<InsDataSourceDescEntity> dataSourceDetail = insDataSourceDetailDao.findDataSourceDetail(insDataSourceModel.getClientId(), insDataSourceModel.getId());
        Assert.isTrue(ObjectUtils.isEmpty(dataSourceDetail), "该数据源下存在数据详情，请先删除数据详情");
        insDataSourceDao.deleteDataSource(insDataSourceModel.getClientId(), insDataSourceModel.getId());
    }

    @Override
    public void startProcessing(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getBatchId(), "批次ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源ID不允许为空");
        final String key = this.getStartProcessingKey(insDataSourceModel.getClientId() + "_" + insDataSourceModel.getDataSourceId() + "_" + insDataSourceModel.getBatchId());
        String s = startProcessingCache.get(key);
//        String s = startProcessingCache.get(insDataSourceModel.getClientId() + "_" + insDataSourceModel.getDataSourceId() + "_" + insDataSourceModel.getBatchId());
        Assert.isTrue(ObjectUtils.isEmpty(s), "该批次数据正在处理中，请勿重复点击");
        startProcessingCache.put(key, "true");
//        startProcessingCache.put(insDataSourceModel.getClientId() + "_" + insDataSourceModel.getDataSourceId() + "_" + insDataSourceModel.getBatchId(), "true");
        try {
            InsDataSourceEntity dataSourceById = insDataSourceDao.findDataSourceById(insDataSourceModel.getClientId(), insDataSourceModel.getDataSourceId());
            final String dataSourceType = dataSourceById.getDataSourceType();
            final List<String> labelType = dataSourceById.getLabelType();
            final String modelType = dataSourceById.getModelType();
            List<InsDataSourceDescEntity> dataSourceDetailAll = insDataSourceDetailDao.findDataSourceDetailAll(insDataSourceModel.getClientId(), insDataSourceModel.getBatchId(), insDataSourceModel.getDataSourceId(), Arrays.asList("0"));
            //过滤掉无效数据
            List<InsDataSourceDescEntity> collect = dataSourceDetailAll.stream().filter(e -> "1".equalsIgnoreCase(e.getDataValidity())).collect(Collectors.toList());
            final int total = collect.size();
            //拆分批次结果集
            CopyOnWriteArrayList<CopyOnWriteArrayList<JSONObject>> result = new CopyOnWriteArrayList<>();
            final int size = collect.toString().getBytes().length;
            final Long jsonDataSize = dynamicParameterMapping.getJsonDataSize();
            long start = System.currentTimeMillis();
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("startProcessing-json转换");
            final Map<String, String> extAttrsMap = extAttrsMappingValuesService.getAttrs(insDataSourceModel.getClientId());
            List<JSONObject> jsonObjectList = collect.stream().map(e -> {
                String data = e.getContent();
                String compress = StringUtil.uncompress(data);
                JSONObject jsonObject = JSONObject.parseObject(compress);
                JSONObject object = new JSONObject();
                JSONObject bizExtAttrs = new JSONObject();
                JSONObject bizExtAttrs2 = new JSONObject();
                JSONObject bizExtAttrs3 = new JSONObject();
                jsonObject.entrySet().stream().forEach(k->{
                    final String keys = k.getKey();
                    final Object value = k.getValue();
                    if(extAttrsMap.containsKey(keys)&&"voc_content".equalsIgnoreCase(keys)){
                        bizExtAttrs2.put(keys, value);
                        bizExtAttrs.put(keys, value);
                    }else if(extAttrsMap.containsKey(keys)&&"user_name".equalsIgnoreCase(keys)){
                        bizExtAttrs3.put("customer_name", value);
                    }else if(extAttrsMap.containsKey(keys)&&"gender".equalsIgnoreCase(keys)){
                        bizExtAttrs3.put("customer_sex", value);
                    }else if(extAttrsMap.containsKey(keys)&&"dlr_code_".equalsIgnoreCase(keys)){
                        bizExtAttrs3.put("dlr_code", value);
                    }else if(extAttrsMap.containsKey(keys)&&"voc_process_at".equalsIgnoreCase(keys)){
                        bizExtAttrs3.put("publish_time", value);
                        object.put("publish_time",value);
                    }else if(extAttrsMap.containsKey(keys)){
                        bizExtAttrs.put(keys, value);
                    }

                    if("abstracts".equalsIgnoreCase(keys)){
                        bizExtAttrs.put("abstract", value);
                    }
                    bizExtAttrs3.put("project_id",projectId);
                   if("createTime".equalsIgnoreCase(keys)){
                       object.put("bizCreateTime",value);
                       object.put("createTime",value);
                   }else if("car_series_code".equalsIgnoreCase(keys)){
                       object.put("carSeriesCode",value);
                   }else if("voc_content".equalsIgnoreCase(keys)){
                       object.put("content", value);
                   }else if("user_name".equalsIgnoreCase(keys)){
                       object.put("customerName", value);
                   }else if("gender".equalsIgnoreCase(keys)){
                       object.put("customerSex", value);
                   }else if("user_name".equalsIgnoreCase(keys)){
                       object.put("customerName", value);
                   }else if("id".equalsIgnoreCase(keys)){
                       object.put("id", value);
                   }else if("one_id".equalsIgnoreCase(keys)){
                       object.put("oneId", value);
                   }else if("channelId".equalsIgnoreCase(keys)){
                       object.put("channelId",value);
                   }
                   object.put("type","text");
                });
                object.put("bizExtAttrs", bizExtAttrs);
                object.put("bizExtAttrs2", bizExtAttrs2);
                object.put("bizExtAttrs3", bizExtAttrs3);
                return object;
            }).collect(Collectors.toList());
//            List<JSONObject> jsonObjectList = collect.stream().map(e->{
//                //转换json对象
//                final String json = JSON.toJSONString(e,SerializerFeature.WriteMapNullValue);
//                final JSONObject jsonObject = JSON.parseObject(json);
//                JSONObject object = new JSONObject();
//                jsonObject.entrySet().stream().forEach(k->{
//                    final String key = k.getKey();
//                    final Object value = k.getValue();
//                    if("content".equalsIgnoreCase(key)){
//                        String compress = StringUtil.uncompress(String.valueOf(value));
//                        object.put(key,compress);
//                    } else if("URL".equalsIgnoreCase(key)||"channelId".equalsIgnoreCase(key)){
//                        object.put(key,ObjectUtils.isEmpty(value)?"":value);
//                    }else {
//                        final String underlineCase = StrUtil.toUnderlineCase(key);
//                        object.put(underlineCase, ObjectUtils.isEmpty(value) ? "" : value);
//                    }
//                });
//
//                object.put("type",dataSourceType);
//                object.put("clientId",insDataSourceModel.getClientId());
//                return object;
//            }).collect(Collectors.toList());
            stopWatch.stop();
            stopWatch.prettyPrint();
            stopWatch.start("startProcessing-数据分批");
            AtomicLong ls = new AtomicLong(0L);
            jsonObjectList.stream().forEach(e -> {
                if (ObjectUtils.isEmpty(result)) {
                    //第一次
                    CopyOnWriteArrayList<JSONObject> list = new CopyOnWriteArrayList<>();
                    list.add(e);
                    result.add(list);
                } else {
                    //获取当前json数据的大小
                    int length1 = e.toJSONString().getBytes().length;
                    //判断最后一个集合的json大小是否小于jsonDataSize，并且当前json数据大小加上最后一个集合的json大小是否小于jsonDataSize
                    if (ls.get() < (jsonDataSize.longValue()) && (ls.get() + length1) <= (jsonDataSize.longValue())) {
                        //获取最后一个集合
                        final int resultSize = result.size();
                        List<JSONObject> jsonObjects = result.get(resultSize - 1);
                        jsonObjects.add(e);
                        ls.addAndGet(length1);
                    } else {
                        CopyOnWriteArrayList<JSONObject> list = new CopyOnWriteArrayList<>();
                        list.add(e);
                        result.add(list);
                        ls.set(0);
                    }
                }
            });
            stopWatch.stop();
            stopWatch.prettyPrint();
            long end = System.currentTimeMillis();
            log.info("数据拆分耗时:[{}]", TimeUnit.MILLISECONDS.toSeconds(end - start) > 0 ? TimeUnit.MILLISECONDS.toSeconds(end - start) + "秒" : (end - start) + "毫秒");
            log.info("待推送数据量:[{}条],总大小:[{}],满足[{}]是进行拆分,拆分成:[{}批]推送", total, size, jsonDataSize, result.size());

            Set<String> workIds = new HashSet<>();
            AtomicInteger currentCount = new AtomicInteger(1);
            result.stream().forEach(sub -> {
                InsDataSourceModel build = InsDataSourceModel.builder()
                        .clientId(insDataSourceModel.getClientId())
                        .requestId(insDataSourceModel.getBatchId())
                        .total(String.valueOf(total))
                        .dataSource(insDataSourceModel.getDataSourceId())
                        .currentBatchTotal(String.valueOf(sub.size()))
                        .currentBatchPage(String.valueOf(currentCount.getAndIncrement()))
                        .batchPageTotal(String.valueOf(result.size()))
                        .data(sub)
                        .labelType(labelType)
                        .modelType(modelType)
                        .showType("1")
                        .build();
                try {
                    final String workId = insDataSourceDetailDao.batchPushData(build);
                    workIds.add(workId);
                    log.info("总批次:[{}],当前推送批次:[{}],dataSize:[{}],当前批次总条数:[{}]", result.size(), currentCount.get() - 1, sub.toString().getBytes().length, sub.size());
                } catch (Exception e) {
                    log.error("推送异常:{}", e);
                    throw new RuntimeException(e.getMessage());
                }
            });
            workIds.stream().forEach(e -> {
                insDataSourceDetailDao.updateDataSourceDetailStatusAndWorkId(insDataSourceModel.getClientId(), insDataSourceModel.getBatchId(), "1", e);
            });
        } finally {
//            startProcessingCache.remove(insDataSourceModel.getClientId() + "_" + insDataSourceModel.getDataSourceId() + "_" + insDataSourceModel.getBatchId());
            startProcessingCache.remove(key);
        }

    }


    @Override
    public void pushResultData(List<InsDataSourceModel> insDataSourceModel) {
        this.checkParameter(insDataSourceModel);
        log.debug("pushResultData入参:{}", insDataSourceModel);

        insDataSourceModel.stream()
                .filter(e -> StrUtil.isNotBlank(e.getClientId()))
                .filter(e -> StrUtil.isNotBlank(e.getBatchId()))
                .filter(e -> StrUtil.isNotBlank(e.getStatus()))
                .forEach(e -> {
                    insDataSourceDetailDao.updateDataSourceDetail(e.getClientId(), e.getBatchId(), e.getStatus());
                    if (ObjectUtils.isNotEmpty(e.getErrorIds())) {
                        log.debug("存在错误数据，开始更新错误数据");
                        insDataSourceDetailDao.batchUpdateDataSourceDetail(e.getClientId(), e.getBatchId(), "-1", e.getErrorIds());
                    }
                });
    }

    @Override
    public PageInfo getRawData(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源id不允许为空");
        Set<String> workIds = insDataSourceDetailDao.findDataSourceWorkIds(insDataSourceModel.getClientId(), ObjectUtils.isNotEmpty(insDataSourceModel.getBatchId()) ? insDataSourceModel.getBatchId() : null, insDataSourceModel.getDataSourceId(), Arrays.asList("2", "-1"));
        if (ObjectUtils.isEmpty(workIds)) {
            log.warn("该批次下没有数据详情");
            return new PageInfo();
        }
        insDataSourceModel.setWorkIdList(workIds);
        insDataSourceModel.setShowType("1");
        return insDataSourceDetailDao.getRawData(insDataSourceModel);
    }

    @Override
    public PageInfo getSIRawData(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
//        Assert.hasLength(insDataSourceModel.getDataName(),"数据名称不允许为空");
        insDataSourceModel.setDate(insDataSourceModel.getDataName());
        return insDataSourceDetailDao.getRawData(insDataSourceModel);
    }

    @Override
    public PageInfo getRawDataResult(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源id不允许为空");
        Set<String> dataSourceDetailAll = insDataSourceDetailDao.findDataSourceWorkIds(insDataSourceModel.getClientId(), ObjectUtils.isNotEmpty(insDataSourceModel.getBatchId()) ? insDataSourceModel.getBatchId() : null, insDataSourceModel.getDataSourceId(), Arrays.asList("2", "-1"));
        if (ObjectUtils.isEmpty(dataSourceDetailAll)) {
            log.warn("该批次下没有数据详情");
            return new PageInfo();
        }
        insDataSourceModel.setWorkIdList(dataSourceDetailAll);
        insDataSourceModel.setShowType("1");
        return insDataSourceDetailDao.getRawDataResult(insDataSourceModel);
    }

    @Override
    public PageInfo getSIRawDataResult(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
//        Assert.hasLength(insDataSourceModel.getDataName(),"数据名称不允许为空");
        insDataSourceModel.setDate(insDataSourceModel.getDataName());
        return insDataSourceDetailDao.getRawDataResult(insDataSourceModel);
    }

    @Override
    public Boolean exportRawData(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        try {
            Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
            Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源id不允许为空");
            Set<String> dataSourceDetailAll = insDataSourceDetailDao.findDataSourceWorkIds(insDataSourceModel.getClientId(), ObjectUtils.isNotEmpty(insDataSourceModel.getBatchId()) ? insDataSourceModel.getBatchId() : null, insDataSourceModel.getDataSourceId(), Arrays.asList("2"));
            List<InsDataSourceOriginDataVo> originDataVo;
            RawDataParamModel build = RawDataParamModel.builder().build();
            BeanUtils.copyProperties(insDataSourceModel, build);
            if (ObjectUtils.isEmpty(dataSourceDetailAll)) {
                log.warn("该批次下没有数据详情");
                originDataVo = List.of();
            } else {
//                insDataSourceModel.setWorkIdList(dataSourceDetailAll);
                build.setWorkIdList(dataSourceDetailAll);
//                originDataVo = insDataSourceDetailDao.exportRawData(insDataSourceModel);
            }

            final String taskId = IdWorker.getId();
            build.setTaskId(taskId);
            LocalDateTime now = LocalDateTime.now();
            build.setShowType(null);
            LocalDate parse = LocalDate.parse(build.getStartTime());
            String channelName = "";
            if(ObjectUtils.isNotEmpty(build.getChannelIdList())){
                final String channelCode = build.getChannelIdList().stream().findAny().get();
                channelName = channelInfoService.findChannelNameByChannelCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).code(channelCode).build());
                List<String> downChannelByCode = channelInfoService.findDownChannelByCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).channelCodes(ObjectUtils.isNotEmpty(insDataSourceModel.getChannelIdList())?insDataSourceModel.getChannelIdList():List.of()).build());
                build.setChannelIdList(downChannelByCode);
            }else {
                channelName = "全部渠道";
            }
            final String taskName = "上传-原始-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            build.setFileName(taskName);
            this.findVerificationResultByCondition(build);

            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .taskName(taskName)
                            .fileName(taskName)
                    .type(LargeDigitaFilesType.DATA_SOURCE_API_RAW_DATA.getCode())
                    .status(null)
                    .createTime(now)
                    .build());
            return true;
//            ExcelUtil.writeExcel(response, originDataVo, "原始数据.xlsx", "原始数据", InsDataSourceOriginDataVo.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean exportSIRawData(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        try {
            Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
//            Assert.hasLength(insDataSourceModel.getDataName(), "数据名称不允许为空");
            insDataSourceModel.setDate(insDataSourceModel.getDataName());
            RawDataParamModel build = RawDataParamModel.builder().build();
            BeanUtils.copyProperties(insDataSourceModel, build);
            final String taskId = IdWorker.getId();
            build.setTaskId(taskId);
            build.setShowType(2);
            LocalDateTime now = LocalDateTime.now();
            LocalDate parse = LocalDate.parse(build.getStartTime());
            String channelName = "";
            if(ObjectUtils.isNotEmpty(build.getChannelIdList())){
                final String channelCode = build.getChannelIdList().stream().findAny().get();
                channelName = channelInfoService.findChannelNameByChannelCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).code(channelCode).build());
                List<String> downChannelByCode = channelInfoService.findDownChannelByCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).channelCodes(ObjectUtils.isNotEmpty(insDataSourceModel.getChannelIdList())?insDataSourceModel.getChannelIdList():List.of()).build());
                build.setChannelIdList(downChannelByCode);
            }else {
                channelName = "全部渠道";
            }
//            final String taskName = "集成-原始-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            final String taskName = "集成-原始-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            build.setFileName(taskName);
            this.findVerificationResultByCondition(build);
            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .taskName(taskName)
                            .fileName(taskName)
                    .type(LargeDigitaFilesType.DATA_SOURCE_API_RAW_DATA.getCode())
                    .status(null)
                    .createTime(now)
                    .build());
            return true;
//            List<InsDataSourceOriginDataVo> originDataVo = insDataSourceDetailDao.exportRawData(insDataSourceModel);
//            ExcelUtil.writeExcel(response, originDataVo, "原始数据.xlsx", "原始数据", InsDataSourceOriginDataVo.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean exportRawDataResult(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        try {
            Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
            Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源id不允许为空");
            Set<String> dataSourceDetailAll = insDataSourceDetailDao.findDataSourceWorkIds(insDataSourceModel.getClientId(), ObjectUtils.isNotEmpty(insDataSourceModel.getBatchId()) ? insDataSourceModel.getBatchId() : null, insDataSourceModel.getDataSourceId(), Arrays.asList("2"));
            List<InsDataSourceResultDataVo> insDataSourceOriginDataVos;
            ResultDataParamModel build = ResultDataParamModel.builder().build();
            BeanUtils.copyProperties(insDataSourceModel, build);
            if (ObjectUtils.isEmpty(dataSourceDetailAll)) {
                log.warn("该批次下没有数据详情");
                insDataSourceOriginDataVos = List.of();
            } else {
//                insDataSourceModel.setWorkIdList(dataSourceDetailAll);
                build.setWorkIdList(dataSourceDetailAll);
//                insDataSourceOriginDataVos = insDataSourceDetailDao.exportRawDataResult(insDataSourceModel);
            }

            final String taskId = IdWorker.getId();
            build.setTaskId(taskId);
            build.setShowType(null);

            LocalDateTime now = LocalDateTime.now();
            LocalDate parse = LocalDate.parse(build.getStartTime());
            String channelName = "";
            if(ObjectUtils.isNotEmpty(build.getChannelIdList())){
                final String channelCode = build.getChannelIdList().stream().findAny().get();
                channelName = channelInfoService.findChannelNameByChannelCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).code(channelCode).build());
                List<String> downChannelByCode = channelInfoService.findDownChannelByCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).channelCodes(ObjectUtils.isNotEmpty(insDataSourceModel.getChannelIdList())?insDataSourceModel.getChannelIdList():List.of()).build());
                build.setChannelIdList(downChannelByCode);
            }else {
                channelName = "全部渠道";
            }

//            final String taskName = "上传-结果-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            final String taskName = "上传-结果-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            build.setFileName(taskName);
            this.exportResultData(build);
            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .taskName(taskName)
                    .type(LargeDigitaFilesType.DATA_SOURCE_API_RAW_RESULT_DATA.getCode())
                    .status(null)
                    .createTime(now)
                    .build());
            return true;
//            ExcelUtil.writeExcel(response, insDataSourceOriginDataVos, "结果数据.xlsx", "结果数据", InsDataSourceResultDataVo.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean exportSIRawDataResult(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        try {
            Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
//            Assert.hasLength(insDataSourceModel.getDataName(), "数据名称不允许为空");
            insDataSourceModel.setDate(insDataSourceModel.getDataName());
            ResultDataParamModel build = ResultDataParamModel.builder().build();
            BeanUtils.copyProperties(insDataSourceModel, build);
            final String taskId = IdWorker.getId();
            build.setTaskId(taskId);
            build.setShowType("2");
            LocalDateTime now = LocalDateTime.now();
            LocalDate parse = LocalDate.parse(build.getStartTime());
            String channelName = "";
            if(ObjectUtils.isNotEmpty(build.getChannelIdList())){
                final String channelCode = build.getChannelIdList().stream().findAny().get();
                channelName = channelInfoService.findChannelNameByChannelCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).code(channelCode).build());
                List<String> downChannelByCode = channelInfoService.findDownChannelByCode(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).channelCodes(ObjectUtils.isNotEmpty(insDataSourceModel.getChannelIdList())?insDataSourceModel.getChannelIdList():List.of()).build());
                build.setChannelIdList(downChannelByCode);
            }else {
                channelName = "全部渠道";
            }

//            final String taskName = "集成-结果-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            final String taskName = "集成-结果-".concat(String.valueOf(parse.getMonthValue())).concat("月").concat("-").concat(channelName).concat("-").concat(now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

            build.setFileName(taskName);
            /**
             * 完成数据查询+excel生成+附件上传+状态记录等功能
             */
            this.exportResultData(build);

            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .taskName(taskName)
                    .type(LargeDigitaFilesType.DATA_SOURCE_API_RAW_RESULT_DATA.getCode())
                    .status(null)
                    .createTime(now)
                    .build());
            return true;
//            List<InsDataSourceResultDataVo> insDataSourceOriginDataVos = insDataSourceDetailDao.exportRawDataResult(insDataSourceModel);
//            ExcelUtil.writeExcel(response, insDataSourceOriginDataVos, "结果数据.xlsx", "结果数据", InsDataSourceResultDataVo.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InsDataSourceVo> getDataSourceList(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceType(), "数据源类型不允许为空");
        List<InsDataSourceEntity> dataSource = insDataSourceDao.findDataSource(insDataSourceModel);
        if (ObjectUtils.isEmpty(dataSource)) {
            log.info("暂无数据源信息");
            return List.of();
        }
        List<InsDataSourceVo> insDataSourceVos = convertService.dataSourceEntityToVo(dataSource);
        return insDataSourceVos;
    }

    @Override
    public InsDataSourceSearchCriteriaVo getDataSourceSearchCriteria(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源id不允许为空");
        Set<String> dataSourceDetailAll = insDataSourceDetailDao.findDataSourceWorkIds(insDataSourceModel.getClientId(), null, insDataSourceModel.getDataSourceId(), Arrays.asList("2"));
        Assert.notEmpty(dataSourceDetailAll, "该批次下没有数据详情");
        insDataSourceModel.setWorkIdList(dataSourceDetailAll);
        insDataSourceModel.setShowType("1");
        InsDataSourceSearchCriteriaVo dataSourceSearchCriteria = insDataSourceDetailDao.getDataSourceSearchCriteria(insDataSourceModel);
        dataSourceSearchCriteria.setClientId(insDataSourceModel.getClientId());
//        dataSourceSearchCriteria.setDefaultEndTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        dataSourceSearchCriteria.setDefaultStartTime(LocalDateTime.now().plusDays(-30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return dataSourceSearchCriteria;
    }

    @Override
    public InsDataSourceSearchCriteriaVo getSIDataSourceSearchCriteria(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
//        Assert.hasLength(insDataSourceModel.getDataName(),"数据名称不允许为空");
        InsDataSourceSearchCriteriaVo dataSourceSearchCriteria = insDataSourceDetailDao.getDataSourceSearchCriteria(insDataSourceModel);
        dataSourceSearchCriteria.setClientId(insDataSourceModel.getClientId());
        String dataName = insDataSourceModel.getDataName();
        String time = aysPostprocessDataMapper.getDateTime(dataName);
        log.info("getSIDataSourceSearchCriteria获取最近的数据发布时间：{}", time);
        if (!StringUtils.isEmpty(time)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            // 将字符串解析为 LocalDateTime
            LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
            // 减去 30 天
            LocalDateTime newDateTime = dateTime.minusDays(30);
            // 将结果转换回字符串
            String result = newDateTime.format(formatter);
            dataSourceSearchCriteria.setDefaultEndTime(time);
            dataSourceSearchCriteria.setDefaultStartTime(result);
        }
        return dataSourceSearchCriteria;
    }

    @Override
    public void exportRawDataByStatus(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getBatchId(), "批次ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDataSourceId(), "数据源ID不允许为空");
        List<InsDataSourceOriginDataVo> rawData = List.of();
        String fileName = "原始数据";
        //获取已完成及失败的数据
        List<InsDataSourceDescEntity> dataSourceDetailAll = insDataSourceDetailDao.findDataSourceDetailAll(insDataSourceModel.getClientId(), insDataSourceModel.getBatchId(), insDataSourceModel.getDataSourceId(), null);
        //获取所有渠道信息
        List<InsChannelInfoEntity> allChannelInfo = channelDao.findAllChannelInfo(InsChannelInfoModel.builder().clientId(insDataSourceModel.getClientId()).build());
        Map<String, String> channelMap = allChannelInfo.stream().collect(Collectors.toMap(InsChannelInfoEntity::getId, InsChannelInfoEntity::getName, (v1, v2) -> v1));
        if (ObjectUtils.isEmpty(insDataSourceModel.getStatus()) && ObjectUtils.isEmpty(insDataSourceModel.getDataValidity())) {
            log.debug("获取全部原始数据");
            rawData = this.rawDataAssembly(dataSourceDetailAll, channelMap);
            fileName = "全部数据";
        } else if (ObjectUtils.isNotEmpty(insDataSourceModel.getStatus()) && ObjectUtils.isEmpty(insDataSourceModel.getDataValidity()) && "-1".equalsIgnoreCase(insDataSourceModel.getStatus())) {
            log.debug("获取处理失败的有效数据");
            List<InsDataSourceDescEntity> collect = dataSourceDetailAll.stream().filter(e -> DataValidityType.VALID.getCode().equalsIgnoreCase(e.getDataValidity()) && DataStatusType.FAILURE.getCode().equalsIgnoreCase(e.getStatus())).collect(Collectors.toList());
            if (ObjectUtils.isEmpty(collect)) {
                log.info("暂无处理失败的有效数据");
            } else {
                rawData = this.rawDataAssembly(collect, channelMap);
            }
            fileName = "失败数据";
        } else if (ObjectUtils.isEmpty(insDataSourceModel.getStatus()) && ObjectUtils.isNotEmpty(insDataSourceModel.getDataValidity()) && "0".equalsIgnoreCase(insDataSourceModel.getDataValidity())) {
            log.debug("获取无效数据");
            List<InsDataSourceDescEntity> collect = dataSourceDetailAll.stream().filter(e -> DataValidityType.INVALID.getCode().equalsIgnoreCase(e.getDataValidity())).collect(Collectors.toList());
            if (ObjectUtils.isEmpty(collect)) {
                log.info("暂无无效数据");
            } else {
                rawData = this.rawDataAssembly(collect, channelMap);
            }
            fileName = "无效数据";
        }

        try {
            long start = System.currentTimeMillis();
            ExcelUtil.writeExcel(response, rawData, fileName + ".xlsx", fileName, InsDataSourceOriginDataVo.class, null);
            log.info("导出原始数据耗时：" + (System.currentTimeMillis() - start) );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 导出原始数据 本地上传和数据集成
     * @param insDataSourceModel
     * @throws Exception
     */
    private void findVerificationResultByCondition(RawDataParamModel insDataSourceModel) throws Exception {
        ServiceContextHolder.getExecutor().execute(()->{
            try {
                metaDataAnalysisService.exportRawDataResultTask(insDataSourceModel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


    }


    /**
     * 导出结果数据 本地上传和数据集成
     * @param paramModel
     * @throws Exception
     */
    private void exportResultData(ResultDataParamModel paramModel) throws Exception {
        ServiceContextHolder.getExecutor().execute(()->{
            try {
                aysPostprocessDataService.exportResultDataTask(paramModel);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /*public void test() {
        try {
            this.findVerificationResultByCondition(RawDataParamModel.builder().clientId("764547797eb2e192763f5334028d49c9").build());
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

    }*/

    @Override
    public void exportSIRawDataByStatus(InsDataSourceModel insDataSourceModel, HttpServletResponse response) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDataName(), "数据名称不允许为空");

        insDataSourceModel.setDate(insDataSourceModel.getDataName());
        insDataSourceModel.setDataSourceAccessWay(null);
        List<InsDataSourceOriginDataVo> originDataVo = new ArrayList<>();
        String fileName = "原始数据";
        if(ObjectUtils.isEmpty(insDataSourceModel.getStatus()) && ObjectUtils.isEmpty(insDataSourceModel.getDataValidity())){
            log.debug("获取全部数据");
            //调用数据清洗获取数据
            insDataSourceModel.setDate(insDataSourceModel.getDataName());
            originDataVo = insDataSourceDetailDao.getFailDataList(insDataSourceModel);
            List<InsDataSourceOriginDataVo> verificationResultByCondition = siDataSourceDao.findVerificationResultByCondition(InsDataSourceRequestModel.builder().clientId(insDataSourceModel.getClientId()).date(insDataSourceModel.getDataName()).build());
            if(ObjectUtils.isNotEmpty(originDataVo)){
                originDataVo.addAll(verificationResultByCondition);
            }else{
                originDataVo = verificationResultByCondition;
            }
            fileName = "全部数据";
        }else if(ObjectUtils.isNotEmpty(insDataSourceModel.getStatus()) && ObjectUtils.isEmpty(insDataSourceModel.getDataValidity()) && "-1".equalsIgnoreCase(insDataSourceModel.getStatus())){
            log.debug("获取处理失败的有效数据");
            insDataSourceModel.setDate(insDataSourceModel.getDataName());
            originDataVo = insDataSourceDetailDao.getFailDataList(insDataSourceModel);
            fileName = "失败数据";
        }else if(ObjectUtils.isEmpty(insDataSourceModel.getStatus()) && ObjectUtils.isNotEmpty(insDataSourceModel.getDataValidity()) && "0".equalsIgnoreCase(insDataSourceModel.getDataValidity())){
            log.debug("获取无效数据");
            //调用数据接收获取无效数据
            originDataVo = siDataSourceDao.findVerificationResultByCondition(InsDataSourceRequestModel.builder().clientId(insDataSourceModel.getClientId()).date(insDataSourceModel.getDataName()).build());
            fileName = "无效数据";
        }

        try {
            ExcelUtil.writeExcel(response, originDataVo, fileName + ".xlsx", fileName, InsDataSourceOriginDataVo.class, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<InsDataSourceTreeVo> findAllDataSource(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        insDataSourceModel.setDataSourceAccessWay("");
        List<InsDataSourceEntity> dataSource = insDataSourceDao.findDataSource(insDataSourceModel);
        if (ObjectUtils.isEmpty(dataSource)) {
            log.info("暂无数据源信息");
            return List.of();
        }
        Map<String, List<InsDataSourceEntity>> collect = dataSource.stream().collect(Collectors.groupingBy(InsDataSourceEntity::getDataSourceAccessWay));
        List<InsDataSourceTreeVo> insDataSourceVos = collect.entrySet().stream().map(e -> {
            InsDataSourceTreeVo build = InsDataSourceTreeVo.builder().build();
            if (DataSourceAccessWay.API.getCode().equalsIgnoreCase(e.getKey())) {
                List<InsDataSourceEntity> value = e.getValue();
                List<InsDataSourceTreeVo> api = value.stream().map(e1 -> {
                    InsDataSourceTreeVo insDataSourceTreeVo = new InsDataSourceTreeVo();
                    BeanUtils.copyProperties(e1, insDataSourceTreeVo);
                    return insDataSourceTreeVo;
                }).collect(Collectors.toList());
                build.setDataSourceName(DataSourceAccessWay.API.getText());
                build.setId(DataSourceAccessWay.API.getCode());
                build.setChild(api);
            } else if (DataSourceAccessWay.UPLOAD.getCode().equalsIgnoreCase(e.getKey())) {
                List<InsDataSourceEntity> value = e.getValue();
                List<InsDataSourceTreeVo> upload = value.stream().map(e1 -> {
                    InsDataSourceTreeVo insDataSourceTreeVo = new InsDataSourceTreeVo();
                    BeanUtils.copyProperties(e1, insDataSourceTreeVo);
                    return insDataSourceTreeVo;
                }).collect(Collectors.toList());
                build.setDataSourceName(DataSourceAccessWay.UPLOAD.getText());
                build.setId(DataSourceAccessWay.UPLOAD.getCode());
                build.setChild(upload);
            }
            return build;
        }).collect(Collectors.toList());
        //获取全部数据源
//        List<InsDataSourceVo> insDataSourceVos = convertService.dataSourceEntityToVo(dataSource);
//        //获取全部数据源详情
//        List<InsDataSourceDescEntity> allDataSourceDetail = insDataSourceDetailDao.findAllDataSourceDetail(insDataSourceModel.getClientId());
//        Map<String, List<InsDataSourceDescEntity>> collect = allDataSourceDetail.stream().filter(e->ObjectUtils.isNotEmpty(e.getDataSourceId())).collect(Collectors.groupingBy(InsDataSourceDescEntity::getDataSourceId));
//        insDataSourceVos.stream().forEach(e->{
//            if(collect.containsKey(e.getId())){
//                List<InsDataSourceDescEntity> dataSourceDetail = collect.get(e.getId());
//                List<InsDataSourceDescVo> dataSourceVo =  convertService.dataSourceDescEntityListToVoList(dataSourceDetail);
//                e.setDataSourceDesc(dataSourceVo);
//            }
//        });
        return insDataSourceVos;
    }

    @Override
    public Set<String> findAllWorkIdByDataSourceIds(String clientId, List<String> dataSourceIds) {
        Assert.hasLength(clientId, "客户ID不允许为空");
        List<InsDataSourceDescEntity> allDataSourceDetail = insDataSourceDetailDao.findAllDataSourceDetail(clientId, dataSourceIds);
        Set<String> collect = allDataSourceDetail.stream().map(e -> e.getWorkId()).collect(Collectors.toSet());
        return collect;
    }

    @Override
    public PageInfo findSIDataSourceList(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        PageHelper.startPage(insDataSourceModel.getPageNum(), insDataSourceModel.getPageSize());
        List<InsSIDataSourceEntity> siDataSourceList = siDataSourceDao.findSIDataSourceList(insDataSourceModel);
        if (ObjectUtils.isEmpty(siDataSourceList)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(siDataSourceList);
        List<InsDataSourceDescVo> collect = siDataSourceList.stream().map(e -> {
            InsDataSourceDescVo insDataSourceDescVo = InsDataSourceDescVo.builder()
                    .dataName(e.getDataName())
                    .status(e.getStatus())
                    .processible(false)
                    .build();
            String result = "共" + (ObjectUtils.isNotEmpty(e.getTotalCount())?e.getTotalCount():0) + "条 |   入库" + (ObjectUtils.isNotEmpty(e.getVerificationSuccessCount())?e.getVerificationSuccessCount():0) + "条";
            if (ObjectUtils.isNotEmpty(e.getExecuteSuccessCount())) {
                result = result.concat(" |   处理").concat(e.getExecuteSuccessCount() + "条");
            }
            if (ObjectUtils.isNotEmpty(e.getExecuteFailCount())) {
                insDataSourceDescVo.setFail(true);
            }
            if (Optional.ofNullable(e.getTotalCount()).orElse(0) - Optional.ofNullable(e.getVerificationSuccessCount()).orElse(0)  > 0) {
                insDataSourceDescVo.setInvalid(true);
            }
            insDataSourceDescVo.setImportResult(result);
            return insDataSourceDescVo;
        }).sorted(Comparator.comparing(InsDataSourceDescVo::getDataName).reversed()).collect(Collectors.toList());
        pageInfo.setList(collect);
        return pageInfo;
    }

//    @Scheduled(cron = "0 0/3 * * * ?")
    public void getSIDataSourceResult(){
        log.info("开始执行定时任务");
        String date = LocalDate.now().toString();
        final String dataSourceResultKey = this.getDataSourceResultKey(date);
        //调用数据接收服务，获取当天的接收数据信息，包括:当前的数据总量，当前校验成功的数据量
        String dataCache = dataSourceResultCache.get(dataSourceResultKey);
        Assert.isTrue(ObjectUtils.isEmpty(dataCache), "该批次数据正在处理中，请勿重复点击");
        startProcessingCache.put(dataSourceResultKey, "true");
        try {
            log.info("开始获取客户当日推送数据总数");
            InsDataSourceRequestModel build = InsDataSourceRequestModel.builder().date(date).build();
            InsDataSourceResultVo dataSourceResult = siDataSourceDao.getDataSourceResult(build);
            if (ObjectUtils.isNotEmpty(dataSourceResult)) {
                //更新数据接收服务返回的数据
                dataSourceResult.setClientId(commonClientId);
                dataSourceResult.setDataSourceId(commonDataSourceId);
                dataSourceResult.setDate(date);
                dataSourceResult.setExecuteSuccessCount(ObjectUtils.isNotEmpty(dataSourceResult.getExecuteSuccessCount()) ? dataSourceResult.getExecuteSuccessCount() : 0);
                siDataSourceDao.saveOrUpdateDataSource(dataSourceResult);
            }
//            else {
//                dataSourceResult.setClientId(commonClientId);
//                dataSourceResult.setDate(date);
//                dataSourceResult.setTotalCount(0);
//                dataSourceResult.setVerificationSuccessCount(0);
//                dataSourceResult.setExecuteSuccessCount(0);
//                siDataSourceDao.saveOrUpdateDataSource(dataSourceResult);
//            }
            //调用数据清洗服务，获取截止到目前为止全部信息，包括:处理成功的数据量，处理失败的数据量
            log.info("开始调用数据清洗服务获取当日已处理数据总数");
            List<InsDataSourceResultVo> dataResultStatus = insDataSourceDetailDao.getDataResultStatus(InsDataSourceModel.builder().clientId(commonClientId).build());
            if (ObjectUtils.isNotEmpty(dataResultStatus)) {
                siDataSourceDao.saveOrUpdateBatchDataSource(commonClientId, dataResultStatus);
            }
            log.info("调用数据清洗服务获取当日已处理数据总数结束");
        }catch (Exception e){
            log.error("定时更新系统集成数据异常:{}",e);
        } finally{
            startProcessingCache.remove(dataSourceResultKey);
        }
        log.info("定时任务执行完毕");
    }

    @Override
    public void updateSIDataSource(InsDataSourceModel insDataSourceModel) {
        Assert.hasLength(insDataSourceModel.getClientId(), "客户ID不允许为空");
        Assert.hasLength(insDataSourceModel.getDate(), "日期不允许为空");
        Assert.hasLength(insDataSourceModel.getStatus(), "数据状态不允许为空");
        siDataSourceDao.updateSIDataSource(insDataSourceModel);
    }


    /**
     * @param insDataSourceModel
     * @return void
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/6/18 上午9:56
     * @描述 参数校验
     **/
    private void checkParameter(List<InsDataSourceModel> insDataSourceModel) {
        for (int i = 0; i < insDataSourceModel.size(); i++) {
            Assert.hasLength(insDataSourceModel.get(i).getClientId(), "客户ID不允许为空");
            Assert.hasLength(insDataSourceModel.get(i).getBatchId(), "批次ID不允许为空");
            Assert.hasLength(insDataSourceModel.get(i).getStatus(), "状态不允许为空");
        }
    }


    private String getFileName(String name) {
        return ServiceContextHolder.getSystemId().concat("/").concat("file-temp").concat("/").concat(name);
    }


    /**
     * @param dataSourceDetail
     * @param channelMap
     * @return java.util.List<com.voc.service.insights.engine.vo.InsDataSourceOriginDataVo>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/25 上午10:40
     * @描述 数据重组
     **/
    private List<InsDataSourceOriginDataVo> rawDataAssembly(List<InsDataSourceDescEntity> dataSourceDetail, Map<String, String> channelMap) {
        return dataSourceDetail.stream().map(e -> {
            final String content = e.getContent();
            String uncompress = StringUtil.uncompress(content);
            JSONObject jsonObject = JSONObject.parseObject(uncompress);
            InsDataSourceOriginDataVo javaObject = jsonObject.toJavaObject(InsDataSourceOriginDataVo.class);
//            InsDataSourceOriginDataVo build = InsDataSourceOriginDataVo.builder()
//                    .id(e.getId())
//                    .title(e.getTitle())
//                    .content(StringUtil.uncompress(e.getContent()))
//                    .publishTime(e.getPublishTime())
//                    .userName(e.getUserName())
//                    .readingCount(e.getReadingCount())
//                    .favorCount(e.getFavorCount())
//                    .focusCount(e.getFocusCount())
//                    .commentsCount(e.getCommentsCount())
//                    .collectionsCount(e.getCollectionsCount())
//                    .redirectionCount(e.getRedirectionCount())
//                    .build();
            if (DataValidityType.INVALID.getCode().equalsIgnoreCase(e.getDataValidity())) {
                //无效数据
                javaObject.setDataStatus(DataValidityType.INVALID.getText());
            } else {
                //有效数据
                if (DataStatusType.PROCESSED.getCode().equalsIgnoreCase(e.getStatus())) {
                    //处理成功
                    javaObject.setDataStatus(DataStatusType.PROCESSED.getText());
                } else if (DataStatusType.FAILURE.getCode().equalsIgnoreCase(e.getStatus())) {
                    //处理失败
                    javaObject.setDataStatus(DataStatusType.FAILURE.getText());
                }
            }
//            if(ObjectUtils.isNotEmpty(e.getChannelId())&&channelMap.containsKey(e.getChannelId())){
//                build.setChannelName(channelMap.get(e.getChannelId()));
//            }
            return javaObject;
        }).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String s = "H4sIAAAAAAAA/1WRz2/TMBTH/5Uq5xbZjh0nOXLjwoWNa+XYr1pElkyJO5gQB1AvSPyQGGL8mraiTZom1h6QtrFR8c80TnviX8BOD5SLZX/f+773/HlPvd1C9mWRa8i1F3uL/cPm5af59ev6dNa8P+uYg0vz4cKeze1Fp/49a0aXi6sfi9n3zQd3va4nt0SeQ2aN5ut5M54sJifm+nj5fN/G9N4O2ICGJ9q+lNDiochSleo9q2IrpcpeAo56IWKM0hA7cSDS7P5w24tR1yty6LdJnPouVkG5C2VRKijXxKGUUFWtBzPsd71EaLl1z/lEwJUfChQxDBIkTwhJcMgoGiBEMCb/ftCmq1xZFiuhH7iog7NTFq5BXzg+BBHWQ7iHog5mMWUxQq5Illp8bQ0eUEY5jzgkBHBEeOAPmO9TREJFIxnZbJFUuhRSV4739Gp5MDEvzurpTzN+tTz8Nr85md+8sxswt2Mzett8HtVvjuovx+bj9M+vI2vP4XHbKSBMUAwUSxX6gsgQFEu4VEpxIIgmbq4ShIaNdBv+G30DRzFFMcZ3/Ij4QcB56PZVaJGtYVyZN6sW9grNMJWPHPJiWMr11Tz7CzDsD1hHAgAA";
        System.out.println(StringUtil.uncompress(s));
    }
}
