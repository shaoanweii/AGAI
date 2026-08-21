package com.voc.service.insights.engine.data.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.StringUtil;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.IInsChannelInfoService;
import com.voc.service.insights.engine.api.ILargeDigitaFilesService;
import com.voc.service.insights.engine.api.constants.ContentTypeEnum;
import com.voc.service.insights.engine.api.data.IInsCqCaDataSourceService;
import com.voc.service.insights.engine.api.model.LargeDigitaFilesModel;
import com.voc.service.insights.engine.data.config.InsCqCaRawDataConfig;
import com.voc.service.insights.engine.data.entity.AysCqCaMetaDataAnalysisEntity;
import com.voc.service.insights.engine.data.entity.AysCqCaPostprocessDataEntity;
import com.voc.service.insights.engine.data.entity.InsDataSourceEntity;
import com.voc.service.insights.engine.data.entity.SentimentResultDataEntity;
import com.voc.service.insights.engine.data.mapper.*;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.util.SM4DecryptUtil;
import com.voc.service.insights.engine.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class InsCqCaDataSourceServiceImpl extends ServiceImpl<InsDataSourceMapper, InsDataSourceEntity> implements IInsCqCaDataSourceService {


    private static final Logger log = LoggerFactory.getLogger(InsCqCaDataSourceServiceImpl.class);
    @Autowired
    AysCqCaMetaDataAnalysisMapper aysCqCaMetaDataAnalysisMapper;

    @Autowired
    AysCqCaPostprocessDataMapper aysCqCaPostprocessDataMapper;

    @Autowired
    private IInsChannelInfoService iInsChannelInfoService;
    @Autowired
    private InsQueryHelperDataMapper insQueryHelperDataMapper;
    @Autowired
    private SentimentResultDataMapper sentimentResultDataMapper;

    @Autowired
    private ILargeDigitaFilesService largeDigitaFilesService;

    @Autowired
    private InsCqCaRawDataConfig insCqCaRawDataConfig;


    @Override
    public PageInfo<AysCqCaMetaDataAnalysisVo> getRawData(InsCqCaDataQueryModel insCqCaDataQueryModel) {

        List<String> firstChannelCodeList = insCqCaDataQueryModel.getFirstChannelCodeList();
//        List<String> firstNameChannelCodeList = new ArrayList<>();
//        if (!ObjectUtils.isEmpty(firstChannelCodeList)) {
//            for (String firstChannelCode : firstChannelCodeList) {
//                if (firstChannelCode.equals("chl_001")) {
//                    firstNameChannelCodeList.add("N");
//                }
//                if (firstChannelCode.equals("chl_015")) {
//                    firstNameChannelCodeList.add("Y");
//                }
//            }
//        }
        List<String> firstNameChannelCodeList = convertFirstChannelCodes(firstChannelCodeList, insCqCaRawDataConfig.getChannelCodeMap());
//        fillKeywordList(insCqCaDataQueryModel);
        if (CollectionUtil.isEmpty(insCqCaDataQueryModel.getThreeChannelCodeList())) {
            insCqCaDataQueryModel.setThreeChannelCodeList(insCqCaDataQueryModel.getSecondChannelCodeList());
        }
        if (CollectionUtil.isNotEmpty(firstNameChannelCodeList)) {
            insCqCaDataQueryModel.setFirstChannelCodeList(firstNameChannelCodeList);
        }
//        PageHelper.startPage(insCqCaDataQueryModel.getPageNum(), insCqCaDataQueryModel.getPageSize());
        Integer total = aysCqCaMetaDataAnalysisMapper.countMetaDataAnalysisList(insCqCaDataQueryModel);
        if(ObjectUtils.isEmpty(total)||total==0){
            return new PageInfo();
        }
        if (insCqCaDataQueryModel.getPageNum() == 1) {
            int pageNum = insCqCaDataQueryModel.getPageNum() - 1;
            insCqCaDataQueryModel.setPageNum(pageNum);
        } else {
            int pageNum = (insCqCaDataQueryModel.getPageNum() - 1) * insCqCaDataQueryModel.getPageSize();
            insCqCaDataQueryModel.setPageNum(pageNum);
        }
        List<AysCqCaMetaDataAnalysisEntity> aysCqCaMetaDataAnalysisEntities = aysCqCaMetaDataAnalysisMapper.pageMetaDataAnalysisList(insCqCaDataQueryModel);
        if (ObjectUtils.isEmpty(aysCqCaMetaDataAnalysisEntities)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(aysCqCaMetaDataAnalysisEntities);
        List<AysCqCaMetaDataAnalysisVo> originDataListModelList = this.convertToOriginDataList(aysCqCaMetaDataAnalysisEntities);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(insCqCaDataQueryModel.getPageNum());
        pageInfo.setPageSize(insCqCaDataQueryModel.getPageSize());
        pageInfo.setList(originDataListModelList);
        return pageInfo;
    }


    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public IPage<AysCqCaMetaDataAnalysisVo> getRawDataDetail(InsCqCaDataQueryModel InsCqCaDataQueryModel) {

        if (InsCqCaDataQueryModel.getPageNum() == 1) {
            int pageNum = InsCqCaDataQueryModel.getPageNum() - 1;
            InsCqCaDataQueryModel.setPageNum(pageNum);
        } else {
            int pageNum = (InsCqCaDataQueryModel.getPageNum() - 1) * InsCqCaDataQueryModel.getPageSize();
            InsCqCaDataQueryModel.setPageNum(pageNum);
        }
        IPage<AysCqCaMetaDataAnalysisVo> pages = new Page<>();
        pages.setCurrent(InsCqCaDataQueryModel.getPageNum());
        pages.setSize(InsCqCaDataQueryModel.getPageSize());
//        IPage<AysCqCaMetaDataAnalysisEntity> page = new Page<>(InsCqCaDataQueryModel.getPageNum(), InsCqCaDataQueryModel.getPageSize());
        Integer total = aysCqCaMetaDataAnalysisMapper.countMateDataAnalysisDetailList(InsCqCaDataQueryModel);
        if(ObjectUtils.isEmpty( total)||total==0){
            return pages;
        }
        pages.setTotal(total);
        log.info("获取原始数据明细入参:{}", JSONObject.toJSONString(InsCqCaDataQueryModel));
        List<AysCqCaMetaDataAnalysisEntity> mateDataAnalysisList = aysCqCaMetaDataAnalysisMapper.findMateDataAnalysisList(InsCqCaDataQueryModel);
        if (ObjectUtils.isEmpty(mateDataAnalysisList)) {
            log.info("暂无原始数据");
            return pages;
        }

//        List<AysCqCaMetaDataAnalysisEntity> records = mateDataAnalysisList.getRecords();
        List<AysCqCaMetaDataAnalysisVo> originDataListModelList = this.convertToOriginDataList(mateDataAnalysisList);
        pages.setRecords(originDataListModelList);
        return pages;
    }

    @Override
    public List<BaseCarSeriesDataVo> queryCarSeriesList(InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        List<BaseCarSeriesDataVo> baseCarSeriesDataVoList = aysCqCaPostprocessDataMapper.queryCarSeriesList(InsCqCaDataQueryModel);
        return baseCarSeriesDataVoList;
    }

    @Override
    public List<BaseCarSeriesDataVo> queryBrandList(InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        List<BaseCarSeriesDataVo> brandList = aysCqCaPostprocessDataMapper.queryBrandList(InsCqCaDataQueryModel);
        return brandList;
    }

    @Override
    public List<BaseCarSeriesDataVo> findAllFinalTagLibClientVoList(InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        List<BaseCarSeriesDataVo> baseCarSeriesDataVoList = aysCqCaPostprocessDataMapper.findAllFinalTagLibClientVoList(InsCqCaDataQueryModel);
        return baseCarSeriesDataVoList;
    }

    @Override
    @SwitchClientDS(objectAttribute = "InsCqCaDataQueryModel.appClient")
    public Boolean exportRawData(InsCqCaDataQueryModel InsCqCaDataQueryModel, HttpServletResponse response) {
        String compress = StringUtil.compress(com.alibaba.fastjson.JSONObject.toJSONString(InsCqCaDataQueryModel));
        String fileName = "原始数据-" + LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_FORMAT.getPattern());
        InsCqCaDataQueryModel.setPageSize(20000);
        PageInfo<AysCqCaMetaDataAnalysisVo> rawData = this.getRawData(InsCqCaDataQueryModel);
        log.info("待导出的原始数据总量:{}", rawData.getTotal());
        if (rawData.getTotal() > 100000) {
            throw new RuntimeException("当前系统仅支持导出数据上限为10万条，请合理筛选数据范围后重试。");
        }
        String taskId = InsCqCaDataQueryModel.getTaskId();
        if (StringUtils.isEmpty(InsCqCaDataQueryModel.getTaskId())) {
            taskId = UUID.randomUUID().toString();
            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .userName(ServiceContextHolder.getUser().getFirstname())
                    .taskName(fileName)
                    .type("exportRawData")
                    .status(null)
                    .createTime(LocalDateTime.now())
                    .appId(ServiceContextHolder.getSystemId())
                    .parameters(compress)
                    .build());
        }
        final String finalTaskId = taskId;
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.exportRawDataTask(InsCqCaDataQueryModel, finalTaskId,rawData);
            } catch (Exception e) {
                largeDigitaFilesService.update(LargeDigitaFilesModel.builder().id(finalTaskId).status("0").build());
                log.error("异步导出原始数据失败", e);
            }
        });
        return Boolean.TRUE;
    }

    public void exportRawDataTask(InsCqCaDataQueryModel model, String taskId,PageInfo<AysCqCaMetaDataAnalysisVo> rawData) throws Exception {
        String fileName = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_FORMAT.getPattern()) + "-原数据";
        log.info("原始数据总量:{}", fileName);
        largeDigitaFilesService.start(
                fileName,
                taskId,
                rawData.getTotal(),
                AysCqCaMetaDataAnalysisVo.class,
                page -> {
                    InsCqCaDataQueryModel cloneModel = InsCqCaDataQueryModel.builder().build();
                    BeanUtil.copyProperties(model, cloneModel);
                    if (page.getPageNum() == 1) {
                        return rawData.getList();
                    }
                    cloneModel.setPageNum(page.getPageNum());
                    PageInfo<AysCqCaMetaDataAnalysisVo> data = getRawData(cloneModel);
                    log.info("查询数据>>>>{}", data.getList().size());
                    return data.getList();
                });
    }


    @Override
    @SwitchClientDS(objectAttribute = "InsCqCaDataQueryModel.appClient")
    public Boolean exportRawDataResult(InsCqCaDataQueryModel InsCqCaDataQueryModel, HttpServletResponse response) {
        String compress = StringUtil.compress(com.alibaba.fastjson.JSONObject.toJSONString(InsCqCaDataQueryModel));
        InsCqCaDataQueryModel.setPageSize(20000);
        PageInfo<AysCqCaPostprocessDataVo> resultData = this.getResultData(InsCqCaDataQueryModel);
        log.info("待导出的结果总数据量:{}", resultData.getTotal());
        if (resultData.getTotal() > 100000){
            throw new RuntimeException("当前系统仅支持导出数据上限为10万条，请合理筛选数据范围后重试。");
        }
        String taskId = InsCqCaDataQueryModel.getTaskId();
        if (StringUtils.isEmpty(InsCqCaDataQueryModel.getTaskId())) {
            String fileName = "结果数据-" + LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_FORMAT.getPattern());
            taskId = UUID.randomUUID().toString();
            largeDigitaFilesService.insert(LargeDigitaFilesModel.builder()
                    .id(taskId)
                    .userId(ServiceContextHolder.getUserId())
                    .taskId(taskId)
                    .taskName(fileName)
                    .userName(ServiceContextHolder.getUser().getFirstname())
                    .type("exportRawDataResult")
                    .status(null)
                    .createTime(LocalDateTime.now())
                    .appId(ServiceContextHolder.getSystemId())
                    .parameters(compress)
                    .build());
        }
        final String finalTaskId = taskId;
        ServiceContextHolder.getExecutor().execute(() -> {
            try {
                this.exportRawDataResultTask(InsCqCaDataQueryModel, finalTaskId,resultData);
            } catch (Exception e) {
                largeDigitaFilesService.update(LargeDigitaFilesModel.builder().id(finalTaskId).status("0").build());
                log.error("异步导出结果数据失败", e);
            }
        });
        return Boolean.TRUE;
    }

    public void exportRawDataResultTask(InsCqCaDataQueryModel model, String taskId,PageInfo<AysCqCaPostprocessDataVo> resultData) throws Exception {

        String fileName = LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_FORMAT.getPattern()) + "-结果数据";
        log.info("结果总数据量:{}", fileName);
        largeDigitaFilesService.start(
                fileName,
                taskId,
                resultData.getTotal(),
                AysCqCaPostprocessDataVo.class,
                page -> {
                    InsCqCaDataQueryModel cloneModel = InsCqCaDataQueryModel.builder().build();
                    BeanUtil.copyProperties(model, cloneModel);
                    if (page.getPageNum() == 1) {
                        return resultData.getList();
                    }
                    cloneModel.setPageNum(page.getPageNum());
                    PageInfo<AysCqCaPostprocessDataVo> data = getResultData(cloneModel);
                    log.info("查询结果数据>>>>{}", data.getList().size());
                    return data.getList();
                });
    }

    private List<AysCqCaMetaDataAnalysisVo> convertToOriginDataList(List<AysCqCaMetaDataAnalysisEntity> aysCqCaMetaDataAnalysisEntities) {
        if (CollectionUtil.isEmpty(aysCqCaMetaDataAnalysisEntities)) {
            return new ArrayList<>();
        }

        List<ChannelInfoVo> infoVoList = iInsChannelInfoService.findAll(InsChannelInfoModel.builder().clientId("764547797eb2e192763f5334028d49c9").build());
        Map<String, ChannelInfoVo> channelByCodeMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getCode,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));

        Map<String, ChannelInfoVo> channelIdMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getId,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));


        List<AysCqCaMetaDataAnalysisVo> originDataListModelList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 获取渠道映射表
        Map<String, String> channelCodeNameMap = getChannelCodeNameMap();
        Map<String, ChannelInfoVo> channelMap = getChannelMapping();

        for (AysCqCaMetaDataAnalysisEntity entity : aysCqCaMetaDataAnalysisEntities) {
            AysCqCaMetaDataAnalysisVo originDataListModel = new AysCqCaMetaDataAnalysisVo();

            // 基本属性映射
            mapBasicProperties(entity, originDataListModel, formatter);

            if (channelByCodeMap.containsKey(entity.getChannelCode())) {
                ChannelInfoVo channelInfoVo = channelIdMap.get(channelByCodeMap.get(entity.getChannelCode()).getId());
                originDataListModel.setSecondChannelName(channelInfoVo.getChannelLevelTwoName());
            }
            // 状态和类型转换
            convertStatusAndTypeFields(entity, originDataListModel, channelCodeNameMap);

            // 默认值处理
            setDefaultValues(entity, originDataListModel);

            // 处理attrs2 JSON字段
            processAttrs2Field(entity, originDataListModel);

            //渠道层级映射
            if (ObjectUtils.isNotEmpty(channelMap)) {
                channelMapping(channelMap, originDataListModel, entity.getChannelCode());
            }

            originDataListModelList.add(originDataListModel);
        }

        return originDataListModelList;
    }

    private void channelMapping(Map<String, ChannelInfoVo> channelCode, AysCqCaMetaDataAnalysisVo originDataListModel, String code) {
        ChannelInfoVo channelInfoVo = channelCode.get(code);
        if (ObjectUtils.isNotEmpty(channelInfoVo)) {
            originDataListModel.setChannelLevelOneCode(channelInfoVo.getChannelLevelOneCode());
            originDataListModel.setChannelLevelOneName(channelInfoVo.getChannelLevelOneName());
            originDataListModel.setChannelLevelTwoCode(channelInfoVo.getChannelLevelTwoCode());
            originDataListModel.setChannelLevelTwoName(channelInfoVo.getChannelLevelTwoName());
            originDataListModel.setChannelLevelThreeCode(channelInfoVo.getChannelLevelThreeCode());
            originDataListModel.setChannelLevelThreeName(channelInfoVo.getChannelLevelThreeName());
        }
    }


    private Map<String, ChannelInfoVo> getChannelMapping() {
        final List<ChannelInfoVo> channelInfoVos = iInsChannelInfoService.upwardFindAllChannelHierarchical("764547797eb2e192763f5334028d49c9");
        if (ObjectUtils.isEmpty(channelInfoVos)) {
            return new HashMap<>();
        }

        return channelInfoVos.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getChannelLevelThreeCode, // Key: channelLevelThreeCode
                        vo -> vo,                                // Value: ChannelInfoVo对象本身
                        (existing, replacement) -> existing      // 重复key处理策略：保留已存在的
                ));
    }

    private Map<String, String> getChannelCodeNameMap() {
        List<ChannelInfoVo> all = iInsChannelInfoService.findAll(
                InsChannelInfoModel.builder()
                        .clientId("764547797eb2e192763f5334028d49c9")
                        .build()
        );

        return all.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getCode,
                        ChannelInfoVo::getName,
                        (existing, replacement) -> existing
                ));
    }

    private void mapBasicProperties(AysCqCaMetaDataAnalysisEntity entity,
                                    AysCqCaMetaDataAnalysisVo originDataListModel,
                                    DateTimeFormatter formatter) {
        originDataListModel.setId(entity.getId());
        originDataListModel.setContentType(ContentTypeEnum.getByCode(entity.getContentType()).getText());
        originDataListModel.setDataCreateTime(entity.getDataCreateTime().format(formatter));
        originDataListModel.setDataId(entity.getDataId());
    }

    private void convertStatusAndTypeFields(AysCqCaMetaDataAnalysisEntity entity,
                                            AysCqCaMetaDataAnalysisVo originDataListModel,
                                            Map<String, String> channelCodeNameMap) {
        // 处理是否为外部数据
        if ("N".equals(entity.getIsOuter())) {
            originDataListModel.setIsOuter("私域");
        } else if ("Y".equals(entity.getIsOuter())) {
            originDataListModel.setIsOuter("公域");
        }

        // 处理水军标识
        if ("0".equals(entity.getIsWsaterArmy())) {
            originDataListModel.setIsWsaterArmy("是");
        } else if ("1".equals(entity.getIsWsaterArmy())) {
            originDataListModel.setIsWsaterArmy("否");
        }

        // 处理渠道名称
        if (StringUtils.isNotBlank(entity.getChannelCode()) && channelCodeNameMap.containsKey(entity.getChannelCode())) {
            originDataListModel.setChannelName(channelCodeNameMap.get(entity.getChannelCode()));
        } else {
            originDataListModel.setChannelName("");
        }

        // 处理数据状态
        originDataListModel.setDataStatus(convertDataStatus(entity.getDataStatus()));
    }

    private String convertDataStatus(String dataStatus) {
        if (StringUtils.isBlank(dataStatus)) {
            return "";
        }

        switch (dataStatus) {
            case "1":
                return "过滤数据";
            case "2":
                return "未打标数据";
            case "3":
                return "已打标数据";
            default:
                return "";
        }
    }

    private void setDefaultValues(AysCqCaMetaDataAnalysisEntity entity,
                                  AysCqCaMetaDataAnalysisVo originDataListModel) {
        originDataListModel.setBrand(ObjectUtils.defaultIfNull(entity.getBrand(), ""));
        originDataListModel.setSeries(ObjectUtils.defaultIfNull(entity.getSeries(), ""));
        originDataListModel.setModel(ObjectUtils.defaultIfNull(entity.getModel(), ""));
        originDataListModel.setTitle(ObjectUtils.defaultIfNull(entity.getTitle(), ""));
        originDataListModel.setContent(ObjectUtils.defaultIfNull(entity.getContent(), ""));
        originDataListModel.setWeight(ObjectUtils.defaultIfNull(entity.getWeight(), 0));
        originDataListModel.setOneId(ObjectUtils.defaultIfNull(entity.getOneId(), ""));
        originDataListModel.setIdCarNo(ObjectUtils.defaultIfNull(entity.getIdCarNo(), ""));
        originDataListModel.setMobile(ObjectUtils.defaultIfNull(validateAndDecrypt(entity.getMobile()), ""));
        originDataListModel.setEmail(ObjectUtils.defaultIfNull(entity.getEmail(), ""));
        originDataListModel.setGlobalId(ObjectUtils.defaultIfNull(entity.getGlobalId(), ""));
        originDataListModel.setUserId(ObjectUtils.defaultIfNull(entity.getUserId(), ""));
        originDataListModel.setUserName(ObjectUtils.defaultIfNull(validateAndDecrypt(entity.getUserName()), ""));
        originDataListModel.setVhlId(ObjectUtils.defaultIfNull(entity.getVhlId(), ""));
        originDataListModel.setVhlVin(ObjectUtils.defaultIfNull(validateAndDecrypt(entity.getVhlVin()), ""));
        originDataListModel.setDlrId(ObjectUtils.defaultIfNull(entity.getDlrId(), ""));
        originDataListModel.setDlrCode(ObjectUtils.defaultIfNull(entity.getDlrCode(), ""));
        originDataListModel.setDlrType(ObjectUtils.defaultIfNull(entity.getDlrType(), ""));
        originDataListModel.setMarketId(ObjectUtils.defaultIfNull(entity.getMarketId(), ""));
    }

    /**
     * 验证并解密手机号码
     *
     * @param testBase64Mobile Base64编码的加密手机号
     * @return 解密后的手机号，如果验证失败则返回空字符串
     */
    private String validateAndDecrypt(String testBase64Mobile) {
        // 验证是否为有效的Base64格式
        try {
            boolean validBase64 = SM4DecryptUtil.isValidBase64(testBase64Mobile);

            if (!validBase64) {
                return "";
            }

            // 解密手机号
            return SM4DecryptUtil.decryptMobile(testBase64Mobile);
        } catch (Exception e) {
            log.error("解密手机号失败", e);
            return testBase64Mobile;
        }
    }

    private void processAttrs2Field(AysCqCaMetaDataAnalysisEntity entity,
                                    AysCqCaMetaDataAnalysisVo originDataListModel) {
//        Object attrs2 = entity.getAttrs2();
//        if (ObjectUtils.isNotEmpty(attrs2)) {
//            JSONObject entries = JSONUtil.parseObj(attrs2);

        originDataListModel.setIsManagerFocused(ObjectUtils.defaultIfNull(entity.getIsManagerFocused(), ""));
        originDataListModel.setIsBigV(ObjectUtils.defaultIfNull(entity.getIsBigV(), ""));
        originDataListModel.setAuthorId(ObjectUtils.defaultIfNull(entity.getAuthorId(), ""));
        originDataListModel.setAuthorNick(ObjectUtils.defaultIfNull(entity.getAuthorNick(), ""));
        originDataListModel.setIsMainPost(ObjectUtils.defaultIfNull(entity.getIsMainPost(), ""));
        originDataListModel.setOriginalLink(ObjectUtils.defaultIfNull(entity.getUrl(), ""));
        originDataListModel.setViewCount(ObjectUtils.defaultIfNull(entity.getViewCount(), ""));
        originDataListModel.setCommentCount(ObjectUtils.defaultIfNull(entity.getCommentCount(), ""));
        originDataListModel.setLikeCount(ObjectUtils.defaultIfNull(entity.getLikeCount(), ""));
        originDataListModel.setShareCount(ObjectUtils.defaultIfNull(entity.getShareCount(), ""));
        originDataListModel.setFavoriteCount(ObjectUtils.defaultIfNull(entity.getFavoriteCount(), ""));
        originDataListModel.setWorkOrderId(ObjectUtils.defaultIfNull(entity.getOrderId(), ""));
        originDataListModel.setQuestId(ObjectUtils.defaultIfNull(entity.getQuestId(), ""));
        originDataListModel.setQuestType(ObjectUtils.defaultIfNull(entity.getQuestType(), ""));
        originDataListModel.setQuestAnswerScore(ObjectUtils.defaultIfNull(entity.getQuestAnswerScore(), ""));
        originDataListModel.setQuestBusinessType(ObjectUtils.defaultIfNull(entity.getQuestBusinessType(), ""));
        originDataListModel.setQuestBusinessScenario(ObjectUtils.defaultIfNull(entity.getQuestBusinessScenario(), ""));

        // 处理主贴标识
        if ("N".equals(originDataListModel.getIsMainPost())) {
            originDataListModel.setIsMainPost("否");
        } else if ("Y".equals(originDataListModel.getIsMainPost())) {
            originDataListModel.setIsMainPost("是");
        }
//        }
    }


    @Override
    public PageInfo<AysCqCaPostprocessDataVo> getResultData(InsCqCaDataQueryModel insCqCaDataQueryModel) {
        List<String> firstChannelCodeList = insCqCaDataQueryModel.getFirstChannelCodeList();
//        List<String> firstChannelNameCodeList = new ArrayList<>();
        List<String> firstChannelNameCodeList = convertFirstChannelCodes(firstChannelCodeList, insCqCaRawDataConfig.getResultChannelCodeMap());
        fillKeywordList(insCqCaDataQueryModel);

        if (CollectionUtil.isNotEmpty(firstChannelCodeList)) {
//            for (String firstChannelCode : firstChannelCodeList) {
//                if (firstChannelCode.equals("chl_001")) {
//                    firstChannelNameCodeList.add("私域");
//                }
//                if (firstChannelCode.equals("chl_015")) {
//                    firstChannelNameCodeList.add("公域");
//                }
//            }
            if (CollectionUtil.isNotEmpty(firstChannelNameCodeList)) {
                insCqCaDataQueryModel.setFirstChannelCodeList(firstChannelNameCodeList);
            }
        }
        if (CollectionUtil.isEmpty(insCqCaDataQueryModel.getThreeChannelCodeList())) {
            insCqCaDataQueryModel.setThreeChannelCodeList(insCqCaDataQueryModel.getSecondChannelCodeList());
        }
//        PageHelper.startPage(insCqCaDataQueryModel.getPageNum(), insCqCaDataQueryModel.getPageSize());
        Integer total = aysCqCaPostprocessDataMapper.countPostprocessDataList(insCqCaDataQueryModel);
        if(ObjectUtils.isEmpty( total)||total<=0){
            return new PageInfo();
        }
        if (insCqCaDataQueryModel.getPageNum() == 1) {
            int pageNum = insCqCaDataQueryModel.getPageNum() - 1;
            insCqCaDataQueryModel.setPageNum(pageNum);
        } else {
            int pageNum = (insCqCaDataQueryModel.getPageNum() - 1) * insCqCaDataQueryModel.getPageSize();
            insCqCaDataQueryModel.setPageNum(pageNum);
        }
        List<AysCqCaPostprocessDataEntity> aysPostprocessDataEntities = aysCqCaPostprocessDataMapper.pagePostprocessDataList(insCqCaDataQueryModel);
        if (ObjectUtils.isEmpty(aysPostprocessDataEntities)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(aysPostprocessDataEntities);
        List<AysCqCaPostprocessDataVo> resultDataListModelList = convertToResultDataList(aysPostprocessDataEntities);
        pageInfo.setList(resultDataListModelList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(insCqCaDataQueryModel.getPageNum());
        pageInfo.setPageSize(insCqCaDataQueryModel.getPageSize());
        return pageInfo;
    }

    private void fillKeywordList(InsCqCaDataQueryModel insCqCaDataQueryModel) {
//        insCqCaDataQueryModel.setTitleKeywordList(splitQueryKeywords(insCqCaDataQueryModel.getTitle()));
//        insCqCaDataQueryModel.setContentKeywordList(splitQueryKeywords(insCqCaDataQueryModel.getContent()));
        insCqCaDataQueryModel.setOpinionKeywordList(splitQueryKeywords(insCqCaDataQueryModel.getOpinion()));
    }

    private List<String> splitQueryKeywords(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        List<String> keywordList = Arrays.stream(StringUtils.replace(value, "，", ",").split(","))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        return CollectionUtil.isEmpty(keywordList) ? null : keywordList;
    }

    private List<AysCqCaPostprocessDataVo> convertToResultDataList(List<AysCqCaPostprocessDataEntity> aysPostprocessDataEntities) {
        if (CollectionUtil.isEmpty(aysPostprocessDataEntities)) {
            return new ArrayList<>();
        }

        List<ChannelInfoVo> infoVoList = iInsChannelInfoService.findAll(InsChannelInfoModel.builder().clientId("764547797eb2e192763f5334028d49c9").build());
        Map<String, ChannelInfoVo> channelByCodeMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getCode,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));

        Map<String, ChannelInfoVo> channelIdMap = infoVoList.stream()
                .collect(Collectors.toMap(
                        ChannelInfoVo::getId,
                        Function.identity(),
                        (existing, replacement) -> existing  // 保留第一个值
                ));

        List<AysCqCaPostprocessDataVo> resultDataListModelList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AysCqCaPostprocessDataEntity entity : aysPostprocessDataEntities) {
            AysCqCaPostprocessDataVo resultDataListModel = new AysCqCaPostprocessDataVo();

            // 基础属性拷贝
            BeanUtils.copyProperties(entity, resultDataListModel);
            if (channelByCodeMap.containsKey(entity.getChannelCode())) {
                ChannelInfoVo channelInfoVo = channelIdMap.get(channelByCodeMap.get(entity.getChannelCode()).getId());
                resultDataListModel.setSecondChannelName(channelInfoVo.getChannelLevelTwoName());
            }
            // 特殊字段处理
            processSpecialFields(entity, resultDataListModel, formatter);

            // 标签相关字段处理
            processTagFields(entity, resultDataListModel);

            // 用户相关信息处理
            processCustomerFields(entity, resultDataListModel);

            // 车辆相关信息处理
            processVehicleFields(entity, resultDataListModel);

            // 经销商相关信息处理
            processDealerFields(entity, resultDataListModel);

            resultDataListModelList.add(resultDataListModel);
        }

        return resultDataListModelList;
    }

    private void processSpecialFields(AysCqCaPostprocessDataEntity entity,
                                      AysCqCaPostprocessDataVo resultDataListModel,
                                      DateTimeFormatter formatter) {
        // 时间格式化
        resultDataListModel.setPublishTime(entity.getPublishTime().format(formatter));

        // 数据状态固定为"统计数据"
        resultDataListModel.setDataStatus("1".equals(entity.getAbandon()) ? "无效数据" : "统计数据");

        // 类型转换字段
        convertTypeFields(entity, resultDataListModel);
    }

    private void convertTypeFields(AysCqCaPostprocessDataEntity entity,
                                   AysCqCaPostprocessDataVo resultDataListModel) {
        // 外部标识转换
        if ("N".equals(entity.getIsOuter())) {
            resultDataListModel.setIsOuter("私域");
        } else if ("Y".equals(entity.getIsOuter())) {
            resultDataListModel.setIsOuter("公域");
        }

        // 主贴标识转换
        if ("N".equals(entity.getIsMainPost())) {
            resultDataListModel.setIsMainPost("否");
        } else if ("Y".equals(entity.getIsMainPost())) {
            resultDataListModel.setIsMainPost("是");
        }

        // 水军标识转换
        if ("0".equals(entity.getIsWsaterArmy())) {
            resultDataListModel.setIsWsaterArmy("是");
        } else if ("1".equals(entity.getIsWsaterArmy())) {
            resultDataListModel.setIsWsaterArmy("否");
        }

        // 内容类型转换
        if (StringUtils.isNotBlank(entity.getContentType())) {
            resultDataListModel.setContentType(ContentTypeEnum.getByCode(entity.getContentType()).getText());
        }
    }

    private void processTagFields(AysCqCaPostprocessDataEntity entity,
                                  AysCqCaPostprocessDataVo resultDataListModel) {
        // 高质量声音标识转换
        if ("0".equals(entity.getTagHighQualityVocFlag())) {
            resultDataListModel.setTagHighQualityVocFlag("非高质量声音");
        } else if ("1".equals(entity.getTagHighQualityVocFlag())) {
            resultDataListModel.setTagHighQualityVocFlag("高质量声音");
        } else {
            resultDataListModel.setTagHighQualityVocFlag(ObjectUtils.defaultIfNull(entity.getTagHighQualityVocFlag(), ""));
        }

        // 其他标签字段处理
        resultDataListModel.setSeriesFactory(ObjectUtils.defaultIfNull(entity.getAutomark(), ""));
        resultDataListModel.setTagAccuracy("null".equals(entity.getTagAccuracy()) ? "" : entity.getTagAccuracy());
        resultDataListModel.setTagEventClarity("null".equals(entity.getTagEventClarity()) ? "" : entity.getTagEventClarity());
        resultDataListModel.setTagHighValueFlag(ObjectUtils.defaultIfNull(entity.getTagHighValueFlag(), ""));
        resultDataListModel.setTagComplaintFlagNeedingReply(ObjectUtils.defaultIfNull(entity.getTagComplaintFlagNeedingReply(), ""));
        resultDataListModel.setTagNewEnergyOrFuel(ObjectUtils.defaultIfNull(entity.getTagNewEnergyOrFuel(), ""));
        resultDataListModel.setTagNeedForvclosedLoop(ObjectUtils.defaultIfNull(entity.getTagNeedForvclosedLoop(), ""));

        // D2C相关字段
        resultDataListModel.setD2cResponsibleDept(ObjectUtils.defaultIfNull(entity.getD2cResponsibleDept(), ""));
        resultDataListModel.setD2cAccountableDept(ObjectUtils.defaultIfNull(entity.getD2cAccountableDept(), ""));
        resultDataListModel.setD2cCcDept(ObjectUtils.defaultIfNull(entity.getD2cCcDept(), ""));
        resultDataListModel.setCustClassify(ObjectUtils.defaultIfNull(entity.getCustClassify(), ""));
    }

    private void processCustomerFields(AysCqCaPostprocessDataEntity entity,
                                       AysCqCaPostprocessDataVo resultDataListModel) {
        // 基本客户信息
        resultDataListModel.setOneId(ObjectUtils.defaultIfNull(entity.getOneId(), ""));
        resultDataListModel.setCustGlobalId(ObjectUtils.defaultIfNull(entity.getCustGlobalId(), ""));
        resultDataListModel.setCustName(ObjectUtils.defaultIfNull(entity.getCustName(), ""));
        resultDataListModel.setCustMainPhone(ObjectUtils.defaultIfNull(entity.getCustMainPhone(), ""));
        resultDataListModel.setIsCarOwner(ObjectUtils.defaultIfNull(entity.getIsCarOwner(), ""));
        resultDataListModel.setCustAge(ObjectUtils.defaultIfNull(entity.getCustAge(), ""));
        resultDataListModel.setCustAgeGroup(ObjectUtils.defaultIfNull(entity.getCustAgeGroup(), ""));
        resultDataListModel.setCustGender(ObjectUtils.defaultIfNull(entity.getCustGender(), ""));
        resultDataListModel.setCustHighEducaion(ObjectUtils.defaultIfNull(entity.getCustHighEducaion(), ""));
        resultDataListModel.setMarrigeStatue(ObjectUtils.defaultIfNull(entity.getMarrigeStatue(), ""));
        resultDataListModel.setFamilyIncome(ObjectUtils.defaultIfNull(entity.getFamilyIncome(), ""));
        resultDataListModel.setIsExchangeFlg(ObjectUtils.defaultIfNull(entity.getIsExchangeFlg(), ""));
        resultDataListModel.setPurchaseCarTimes(ObjectUtils.defaultIfNull(entity.getPurchaseCarTimes(), ""));
        resultDataListModel.setIsMemberFlg(ObjectUtils.defaultIfNull(entity.getIsMemberFlg(), ""));

        // 客户地址信息
        resultDataListModel.setCustProvinceCode(ObjectUtils.defaultIfNull(entity.getCustProvinceCode(), ""));
        resultDataListModel.setCustProvince(ObjectUtils.defaultIfNull(entity.getCustProvince(), ""));
        resultDataListModel.setCustCityCode(ObjectUtils.defaultIfNull(entity.getCustCityCode(), ""));
        resultDataListModel.setCustCity(ObjectUtils.defaultIfNull(entity.getCustCity(), ""));
        resultDataListModel.setCustType(ObjectUtils.defaultIfNull(entity.getCustType(), ""));
        resultDataListModel.setCustLivedProv(ObjectUtils.defaultIfNull(entity.getCustLivedProv(), ""));
        resultDataListModel.setCustLivedCity(ObjectUtils.defaultIfNull(entity.getCustLivedCity(), ""));
        resultDataListModel.setCustProfession(ObjectUtils.defaultIfNull(entity.getCustProfession(), ""));
    }

    private void processVehicleFields(AysCqCaPostprocessDataEntity entity,
                                      AysCqCaPostprocessDataVo resultDataListModel) {
        resultDataListModel.setVhlVin(ObjectUtils.defaultIfNull(validateAndDecrypt(entity.getVhlVin()), ""));
        resultDataListModel.setVhlColorName(ObjectUtils.defaultIfNull(entity.getVhlColorName(), ""));
        resultDataListModel.setVhlProductDate(ObjectUtils.defaultIfNull(entity.getVhlProductDate(), ""));
        resultDataListModel.setVhlOfflineDate(ObjectUtils.defaultIfNull(entity.getVhlOfflineDate(), ""));
        resultDataListModel.setVhlIsAbroad(ObjectUtils.defaultIfNull(entity.getVhlIsAbroad(), ""));
        resultDataListModel.setVhlDisCh(ObjectUtils.defaultIfNull(entity.getVhlDisCh(), ""));
        resultDataListModel.setVhlDisMt(ObjectUtils.defaultIfNull(entity.getVhlDisMt(), ""));
        resultDataListModel.setVhlEngClsf(ObjectUtils.defaultIfNull(entity.getVhlEngClsf(), ""));
        resultDataListModel.setVhlEngSeris(ObjectUtils.defaultIfNull(entity.getVhlEngSeris(), ""));
        resultDataListModel.setVhlVehType(ObjectUtils.defaultIfNull(entity.getVhlVehType(), ""));
        resultDataListModel.setVhlCountry(ObjectUtils.defaultIfNull(entity.getVhlCountry(), ""));
        resultDataListModel.setVhlBdClsf(ObjectUtils.defaultIfNull(entity.getVhlBdClsf(), ""));
        resultDataListModel.setVhlSegMt(ObjectUtils.defaultIfNull(entity.getVhlSegMt(), ""));
        resultDataListModel.setVhlPowClsf(ObjectUtils.defaultIfNull(entity.getVhlPowClsf(), ""));
        resultDataListModel.setVhlFuClsf(ObjectUtils.defaultIfNull(entity.getVhlFuClsf(), ""));
        resultDataListModel.setVhlModlSt(ObjectUtils.defaultIfNull(entity.getVhlModlSt(), ""));
        resultDataListModel.setVhlStdPlntCode(ObjectUtils.defaultIfNull(entity.getVhlStdPlntCode(), ""));
    }

    private void processDealerFields(AysCqCaPostprocessDataEntity entity,
                                     AysCqCaPostprocessDataVo resultDataListModel) {
        // OC经销商信息
        resultDataListModel.setDlrOcId(ObjectUtils.defaultIfNull(entity.getDlrOcId(), ""));
        resultDataListModel.setDlrOcName(ObjectUtils.defaultIfNull(entity.getDlrOcName(), ""));
        resultDataListModel.setDlrOcProvince(ObjectUtils.defaultIfNull(entity.getDlrOcProvince(), ""));
        resultDataListModel.setDlrOcCity(ObjectUtils.defaultIfNull(entity.getDlrOcCity(), ""));

        // DC经销商信息
        resultDataListModel.setDlrDcId(ObjectUtils.defaultIfNull(entity.getDlrDcId(), ""));
        resultDataListModel.setDlrDcName(ObjectUtils.defaultIfNull(entity.getDlrDcName(), ""));
        resultDataListModel.setDlrDcProvince(ObjectUtils.defaultIfNull(entity.getDlrDcProvince(), ""));
        resultDataListModel.setDlrDcCity(ObjectUtils.defaultIfNull(entity.getDlrDcCity(), ""));

        // MC经销商信息
        resultDataListModel.setDlrMcId(ObjectUtils.defaultIfNull(entity.getDlrMcId(), ""));
        resultDataListModel.setDlrMcName(ObjectUtils.defaultIfNull(entity.getDlrMcName(), ""));
        resultDataListModel.setDlrMcProvince(ObjectUtils.defaultIfNull(entity.getDlrMcProvince(), ""));
        resultDataListModel.setDlrMcCity(ObjectUtils.defaultIfNull(entity.getDlrMcCity(), ""));
    }

    // 在原有基础上补充缺失的字段处理
    private void processAdditionalFields(AysCqCaPostprocessDataEntity entity,
                                         AysCqCaPostprocessDataVo resultDataListModel) {
        // 作者相关信息
        resultDataListModel.setIsManagerFocused(ObjectUtils.defaultIfNull(entity.getIsManagerFocused(), ""));
        resultDataListModel.setIsBigV(ObjectUtils.defaultIfNull(entity.getIsBigV(), ""));
        resultDataListModel.setAuthorId(ObjectUtils.defaultIfNull(entity.getAuthorId(), ""));
        resultDataListModel.setUserNick(ObjectUtils.defaultIfNull(entity.getAuthorNick(), ""));
        resultDataListModel.setIsMainPost(ObjectUtils.defaultIfNull(entity.getIsMainPost(), ""));
        resultDataListModel.setOriginalLink(ObjectUtils.defaultIfNull(entity.getOriginalLink(), ""));

        // 互动数据
        resultDataListModel.setViewCount(ObjectUtils.defaultIfNull(entity.getViewCount(), ""));
        resultDataListModel.setCommentCount(ObjectUtils.defaultIfNull(entity.getCommentCount(), ""));
        resultDataListModel.setLikeCount(ObjectUtils.defaultIfNull(entity.getLikeCount(), ""));
        resultDataListModel.setShareCount(ObjectUtils.defaultIfNull(entity.getShareCount(), ""));
        resultDataListModel.setFavoriteCount(ObjectUtils.defaultIfNull(entity.getFavoriteCount(), ""));

        // 工单和问卷信息
        resultDataListModel.setWorkOrderId(ObjectUtils.defaultIfNull(entity.getWorkOrderId(), ""));
        resultDataListModel.setQuestId(ObjectUtils.defaultIfNull(entity.getQuestId(), ""));
        resultDataListModel.setQuestType(ObjectUtils.defaultIfNull(entity.getQuestType(), ""));
        resultDataListModel.setQuestAnswerScore(ObjectUtils.defaultIfNull(entity.getQuestAnswerScore(), ""));
        resultDataListModel.setQuestBusinessType(ObjectUtils.defaultIfNull(entity.getQuestBusinessType(), ""));
        resultDataListModel.setQuestBusinessScenario(ObjectUtils.defaultIfNull(entity.getQuestBusinessScenario(), ""));
    }

    @Override
    @SwitchClientDS(datasource = "starrock_dndc")
    public PageInfo getSentimentResultData(InsCqCaDataQueryModel insCqCaDataQueryModel) {
        PageHelper.startPage(insCqCaDataQueryModel.getPageNum(), insCqCaDataQueryModel.getPageSize());
        List<SentimentResultDataEntity> sentimentResultDataEntities = sentimentResultDataMapper.pageSentimentResultDataList(insCqCaDataQueryModel);
        if (ObjectUtils.isEmpty(sentimentResultDataEntities)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(sentimentResultDataEntities);
        List<SentimentResultDataVo> sentimentResultDataVoList = convertToSentimentResultDataList(sentimentResultDataEntities);
        pageInfo.setList(sentimentResultDataVoList);
        return pageInfo;
    }

    /**
     * 转换情感分析结果数据实体列表为VO列表
     *
     * @param sentimentResultDataEntities 情感分析结果数据实体列表
     * @return 情感分析结果数据VO列表
     */
    private List<SentimentResultDataVo> convertToSentimentResultDataList(List<SentimentResultDataEntity> sentimentResultDataEntities) {
        if (CollectionUtil.isEmpty(sentimentResultDataEntities)) {
            return new ArrayList<>();
        }

        List<SentimentResultDataVo> sentimentResultDataVoList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (SentimentResultDataEntity entity : sentimentResultDataEntities) {
            SentimentResultDataVo vo = new SentimentResultDataVo();

            // 基本字段映射
            vo.setId(entity.getId());

            if (entity.getPublishTime() != null) {
                vo.setPublishTime(entity.getPublishTime().format(formatter));
            }

            vo.setChannelId(entity.getChannelId());
            vo.setDataId(entity.getDataId());
            vo.setOneId(entity.getOneId());
            vo.setWorkId(entity.getWorkId());
            vo.setClientId(entity.getClientId());
            vo.setContentType(entity.getContentType());
            vo.setSampleDataType(entity.getSampleDataType());
            vo.setOriginalId(entity.getOriginalId());
            vo.setInputDataId(entity.getInputDataId());
            vo.setOriginalTextScene(entity.getOriginalTextScene());
            vo.setBrandCode(entity.getBrandCode());
            vo.setCarSeriesCode(entity.getCarSeriesCode());
            vo.setLabelType(entity.getLabelType());
            vo.setScenario(entity.getScenario());
            vo.setSentiment(entity.getSentiment());
            vo.setIntentionType(entity.getIntentionType());
            vo.setTopic(entity.getTopic());
            vo.setOpinion(entity.getOpinion());
            vo.setSubject(entity.getSubject());
            vo.setFaultLevel(entity.getFaultLevel());
            vo.setDescription(entity.getDescription());
            vo.setSentimentScore(entity.getSentimentScore());
            vo.setKeywords(entity.getKeywords());
            vo.setModelType(entity.getModelType());
            vo.setAbandon(entity.getAbandon());
            vo.setDone(entity.getDone());
            vo.setRawData(entity.getRawData());
            vo.setExtFields(entity.getExtFields());
            vo.setBizExtAttrs(entity.getBizExtAttrs());
            vo.setBizExtAttrs2(entity.getBizExtAttrs2());
            vo.setBizExtAttrs3(entity.getBizExtAttrs3());
            vo.setCustExtAttrs(entity.getCustExtAttrs());
            vo.setVhlExtAttrs(entity.getVhlExtAttrs());
            vo.setDealerExtAttrs(entity.getDealerExtAttrs());
            vo.setPrdExtAttrs(entity.getPrdExtAttrs());
            vo.setTagsExtAttrs(entity.getTagsExtAttrs());
            // 在其他字段映射的地方添加这一行：
            vo.setTitle(entity.getTitle());


            if (entity.getCreateTime() != null) {
                vo.setCreateTime(entity.getCreateTime().format(formatter));
            }

            if (entity.getUpdateTime() != null) {
                vo.setUpdateTime(entity.getUpdateTime().format(formatter));
            }

            sentimentResultDataVoList.add(vo);
        }

        return sentimentResultDataVoList;
    }

    private List<String> convertFirstChannelCodes(List<String> firstChannelCodeList, Map<String, String> channelCodeMap) {
        List<String> convertedChannelCodeList = new ArrayList<>();
        if (ObjectUtils.isEmpty(firstChannelCodeList) || ObjectUtils.isEmpty(channelCodeMap)) {
            return convertedChannelCodeList;
        }
        for (String firstChannelCode : firstChannelCodeList) {
            String channelNameCode = channelCodeMap.get(firstChannelCode);
            if (StringUtils.isNotBlank(channelNameCode)) {
                convertedChannelCodeList.add(channelNameCode);
            }
        }
        return convertedChannelCodeList;
    }


}
