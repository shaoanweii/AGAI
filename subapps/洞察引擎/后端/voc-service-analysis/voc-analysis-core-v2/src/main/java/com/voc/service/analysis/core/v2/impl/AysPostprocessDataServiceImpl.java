package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.*;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.entity.AysPostprocessDataEntity;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.AysPostprocessDataMapper;
import com.voc.service.analysis.enums.TagTypeEnum;
import com.voc.service.analysis.model.*;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.api.clients.InsTagLibServiceClient;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.vo.InsTagLibVo;
import com.voc.service.insights.engine.vo.TagLibClientTreeVo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@Service
public class AysPostprocessDataServiceImpl extends ServiceImpl<AysPostprocessDataMapper, AysPostprocessDataEntity> implements IAysPostprocessDataService {
    private static final Logger log = LoggerFactory.getLogger(AysPostprocessDataServiceImpl.class);
    @Autowired
    AysConvertMapperService aysConvertMapperService;
    /*@Autowired
    IChannelServiceClient iChannelServiceClient;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    ProcessPostRulesProducer processPostRulesProducer;*/
    @Autowired
    IAysErrorPushService errorPushService;
    @Autowired
    AysExtAttrsMappingValuesService aysExtAttrsMappingValuesService;
    @Autowired
    IAysBatchUpdateService iAysBatchUpdateService;
    @Autowired
    IStaDlrCodeCityMappingMinService iStaDlrCodeCityMappingMinService;

    @Autowired
    InsTagLibServiceClient insTagLibServiceClient;
    @Autowired
    com.voc.service.analysis.core.v2.config.AnalysisConfig config;


    @SwitchClientDS
    @Override
    public Set<String> saveBatch(String clientId, List<AysProcessDataModel> data) throws Exception {
        Assert.isTrue(StrUtil.isNotEmpty(clientId), "clientId clientId be empty");
        List<AysPostprocessDataEntity> saveList = new ArrayList<>();
        try {
            String workId = null;
            for (AysProcessDataModel model : data) {
                AysPostprocessDataEntity entity = JSONUtil.toBean(String.valueOf(model.getData()), AysPostprocessDataEntity.class);
                entity.setDataId(model.getDataId());
                entity.setDone("1");
                entity.setAbandon(model.getAbandon());
                entity.setPublishTime(model.getPublishTime());
                entity.setModelType(model.getModelType());
                entity.setOneId(model.getOneId());
                entity.setCreateTime(LocalDateTime.now());
                workId = model.getWorkId();
                clientId = model.getClientId();
                if (CollUtil.isNotEmpty(model.getHitRuleList())) {
                    log.info("saveBatch hitRules:{}", JSONUtil.toJsonStr(model.getHitRuleList()));
                    final String hitRules = JSONUtil.toJsonStr(JSONUtil.createObj().putOpt("rule_ids", model.getHitRuleList().stream().map(RuleModel::getId).collect(Collectors.toSet())));
                    entity.setHitRules(hitRules);

                }
                entity.setUpdateTime(LocalDateTime.now());
                if (ObjectUtil.isNotNull(model.getExtFields())) {
                    entity.setExtFields(JSONUtil.parseObj(model.getExtFields()));
                }
                if (ObjectUtil.isNotNull(model.getBizExtAttrs())) {
                    entity.setBizExtAttrs(JSONUtil.parseObj(model.getBizExtAttrs()));
                }
                if (ObjectUtil.isNotNull(model.getBizExtAttrs2())) {
                    entity.setBizExtAttrs2(JSONUtil.parseObj(model.getBizExtAttrs2()));
                }
                if (ObjectUtil.isNotNull(model.getBizExtAttrs3())) {
                    entity.setBizExtAttrs3(JSONUtil.parseObj(model.getBizExtAttrs3()));
                }
                try {
                    Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getChannelId()), "getChannelId cannot be empty");

                } catch (IllegalArgumentException e) {
                    //异常入库数据纪录
                    errorPushService.push(ErrorPushModel.builder().table("dwd_voc2_post_rules_result_data").clientId(clientId).action(IAysErrorPushService.ACTION_ADD).data(entity).workId(entity.getWorkId()).tid(ServiceContextHolder.traceId()).build());
                    log.error(e.getMessage(), e);
                    continue;
                }

                saveList.add(entity);
            }
            log.info("本次解析后需保存{}条数据", saveList.size());
            //  processPostRulesProducer.pushData(MessageDTO.builder().source(clientId).data(saveList).build());
            log.info("saveBatch success");
        } catch (Exception e) {
            throw e;
        }
        return saveList.stream().map(AysPostprocessDataEntity::getDataId).collect(Collectors.toSet());
    }

    @SwitchClientDS
    @Override
    public int modifyToDone(String clientId, Set<String> ids, String workId) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        UpdateWrapper<AysPostprocessDataEntity> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(AysPostprocessDataEntity::getWorkId, workId);
        wrapper.in("new_id", ids);
        wrapper.set("done", "1");
        return this.baseMapper.update(null, wrapper);
    }
/*
    @SwitchClientDS
    @Override
    public long removeHistoryData(String clientId, int days) {
        //查询
        Long count = this.baseMapper.checkData(days);
        if (count <= 0) {
            return 0;
        }
        //备份
        this.baseMapper.moveToHistoryData(days);

        long checkLen = this.baseMapper.checkHistoryData();
        if (checkLen > 0) {
            this.baseMapper.deleteHistoryData(days);
        }

        return checkLen;
    }*/

    /*@SwitchClientDS
    @Override
    public PageInfo getResultDataList(String clientId, ResultDataParamModel paramModel) {
        PageHelper.startPage(paramModel.getPageNum(), paramModel.getPageSize());
        List<AysPostprocessDataEntity> aysPostprocessDataEntities = this.baseMapper.pagePostprocessDataList(paramModel);
        if (ObjectUtils.isEmpty(aysPostprocessDataEntities)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(aysPostprocessDataEntities);
        List<JSONObject> resultDataListModelList = convertToDataList(aysPostprocessDataEntities, paramModel.getClientId(), 0);
        pageInfo.setList(resultDataListModelList);
        return pageInfo;
    }*/


    private void addNewKey(Map<String, String> attrsMap, JSONObject jsonObject, JSONObject entries, Integer type) {
        for (Map.Entry<String, String> entry : attrsMap.entrySet()) {
            String value = entry.getValue();
            String o = jsonObject.getStr(entry.getKey());
            if (type == 0) {
                entries.put(value, ObjectUtils.isEmpty(o) ? "-" : o);
            } else {
                entries.put(entry.getKey(), o);
            }
        }
    }

    private List<JSONObject> convertToDataList(List<AysPostprocessDataEntity> aysPostprocessDataEntities, String clientId, Integer type) {

        List<JSONObject> resultDataListModelList = new ArrayList<>();
        Map<String, String> attrsMap = aysExtAttrsMappingValuesService.getAttrs(clientId);
        for (AysPostprocessDataEntity entity : aysPostprocessDataEntities) {
            ResultDataListModel resultDataListModel = new ResultDataListModel();
            resultDataListModel.setDataStatus("统计数据");
            if (ObjectUtils.isNotEmpty(entity.getAbandon())) {
                resultDataListModel.setDataStatus("1".equals(entity.getAbandon()) ? "过滤数据" : "统计数据");
            }
            resultDataListModel.setNewId(entity.getId());
            resultDataListModel.setBrandName(entity.getBrandCode());
            resultDataListModel.setCarSeriesName(entity.getCarSeriesCode());
            resultDataListModel.setLabelTypeLevelFirst(entity.getLabelTypeLevelFirst());
            resultDataListModel.setLabelTypeLevelSecond(entity.getLabelTypeLevelSecond());
            resultDataListModel.setLabelTypeLevelThree(entity.getLabelTypeLevelThree());
            resultDataListModel.setLabelTypeLevelFour(entity.getLabelTypeLevelFour());
            resultDataListModel.setLabelTypeLevelFive(entity.getLabelTypeLevelFive());
            resultDataListModel.setLabelTypeName(TagTypeEnum.getByCode(entity.getLabelType()).getText());
            resultDataListModel.setIntention(entity.getIntentionType());
            resultDataListModel.setSentiment(entity.getSentiment());
            resultDataListModel.setOriginalTextScene(entity.getOriginalTextScene());
            resultDataListModel.setOpinion(entity.getTopic());
            JSONObject entries = JSONUtil.parseObj(resultDataListModel);
            if (ObjectUtil.isNotEmpty(entity.getBizExtAttrs())) {
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(entity.getBizExtAttrs());
                    entries.put("channelName", jsonObject.getStr("channel_subclass"));
                    addNewKey(attrsMap, jsonObject, entries, type);
                } catch (Exception e) {
                    log.info("JSON数据错误bizExtAttrs");
                }
            } else {
                addNewKey(attrsMap, new JSONObject(), entries, type);
            }
            resultDataListModelList.add(entries);
        }
        return resultDataListModelList;
    }

    /*@SwitchClientDS
    @Override
    public List<JSONObject> exportResultData(String clientId, ResultDataParamModel paramModel) {
        List<AysPostprocessDataEntity> aysPostprocessDataEntities = this.baseMapper.pagePostprocessDataList(paramModel);
        if (ObjectUtils.isNotEmpty(aysPostprocessDataEntities)) {
            return convertToDataList(aysPostprocessDataEntities, paramModel.getClientId(), 1);
        }
        return null;
    }*/

    /*@SwitchClientDS
    @Override
    public long remove(String clientId, Set<String> ids) throws Exception {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
     //   processPostRulesProducer.deleteData(MessageDTO.builder().source(clientId).data(ids).build());
        return ids.size();
    }

    @SwitchClientDS
    @Override
    public long removeDB(String clientId, Set<String> ids) {
        long count = 0;
        List<List<String>> subList = CollUtil.split(ids, 200);
        for (List<String> subs : subList) {
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.in("new_id", subs);
            wrapper.eq("client_id", clientId);
            count += this.baseMapper.delete(wrapper);
        }

        return count;
    }*/

    @SwitchClientDS
    @Override
    public List<AysPreprocessDataModel> findByIds(String clientId, Set<String> ids) {
        List<AysPostprocessDataEntity> entityList = this.list(new QueryWrapper<AysPostprocessDataEntity>().in("new_id", ids));

        if (CollectionUtil.isEmpty(entityList)) {
            return new ArrayList<>();
        }

        List<AysPreprocessDataModel> list = aysConvertMapperService.converToAysPostprocessDataModelList(entityList);
        return list;
    }

    /*@Override
    @SwitchClientDS
    public PageInfo getCommonDataList(String clientId, ResultDataParamModel resultDataParamModel) {
        log.info("入参:{}", resultDataParamModel);
        PageHelper.startPage(resultDataParamModel.getPageNum(), resultDataParamModel.getPageSize());
        List<AysPostprocessDataEntity> aysPostprocessDataEntities = this.baseMapper.pagePostprocessMetaDataList(resultDataParamModel);
        if (ObjectUtils.isEmpty(aysPostprocessDataEntities)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(aysPostprocessDataEntities);
        List<JSONObject> resultDataListModelList = convertToDataList(aysPostprocessDataEntities, resultDataParamModel.getClientId(), 0);
        pageInfo.setList(resultDataListModelList);
        return pageInfo;
    }*/


    /*@Override
    @SwitchClientDS
    public PageInfo getProjectResultDataList(String clientId, ProjectResultDataParamModel dataParamModel) {
        dataParamModel.setPageNum((dataParamModel.getPageNum() - 1) * dataParamModel.getPageSize());
        dataParamModel.setPageType(1);
        dataParamModel.setLabelTypeLevelFourDisableList(getDisableTagLib(clientId));
        Set<String> allCarSeriesList = new HashSet<>();
        if (CollUtil.isNotEmpty(dataParamModel.getOwnCarSeries())) {
            allCarSeriesList.addAll(dataParamModel.getOwnCarSeries());
        }
        if (CollUtil.isNotEmpty(dataParamModel.getCompetitorsCarSeries())) {
            allCarSeriesList.addAll(dataParamModel.getCompetitorsCarSeries());
        }
        if (CollUtil.isEmpty(dataParamModel.getCarSeries())) {
            dataParamModel.setAllCarSeriesList(allCarSeriesList);
        }
        if (CollUtil.isNotEmpty(dataParamModel.getMentionCarSeriesList())) {
            String join = StringUtils.join(dataParamModel.getMentionCarSeriesList(), ",");
            dataParamModel.setMentionCarSeriesString(join);
        }
        List<AysPostprocessDataEntity> aysPostprocessDataEntities = this.baseMapper.pageProjectPostDataList(dataParamModel);
        if (ObjectUtils.isEmpty(aysPostprocessDataEntities)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(aysPostprocessDataEntities);
        List<JSONObject> resultDataListModelList = convertToProjectDataList(aysPostprocessDataEntities, dataParamModel.getClientId(), 0, dataParamModel);
        pageInfo.setList(resultDataListModelList);
        pageInfo.setPageNum(dataParamModel.getPageNum());
        pageInfo.setPageSize(dataParamModel.getPageSize());
        pageInfo.setTotal(this.baseMapper.pageProjectPostDataCount(dataParamModel));
        return pageInfo;
    }*/


    private List<JSONObject> convertToProjectDataList(List<AysPostprocessDataEntity> aysPostprocessDataEntities, String clientId, Integer type, ProjectResultDataParamModel dataParamModel) {

        List<JSONObject> resultDataListModelList = new ArrayList<>();
        Map<String, String> attrsMap = aysExtAttrsMappingValuesService.getAttrs(clientId);
        for (AysPostprocessDataEntity entity : aysPostprocessDataEntities) {
            ResultDataListModel resultDataListModel = new ResultDataListModel();
            resultDataListModel.setDataStatus("统计数据");
            if (ObjectUtils.isNotEmpty(entity.getAbandon())) {
                resultDataListModel.setDataStatus("1".equals(entity.getAbandon()) ? "过滤数据" : "统计数据");
            }
            resultDataListModel.setNewId(entity.getId());
            resultDataListModel.setBrandName(entity.getBrandCode());
            resultDataListModel.setCarSeriesName(entity.getCarSeriesCode());
            resultDataListModel.setLabelTypeLevelFirst(entity.getLabelTypeLevelFirst());
            resultDataListModel.setLabelTypeLevelSecond(entity.getLabelTypeLevelSecond());
            resultDataListModel.setLabelTypeLevelThree(entity.getLabelTypeLevelThree());
            resultDataListModel.setLabelTypeLevelFour(entity.getLabelTypeLevelFour());
            resultDataListModel.setLabelTypeLevelFive(entity.getLabelTypeLevelFive());
            resultDataListModel.setLabelTypeName(TagTypeEnum.getByCode(entity.getLabelType()).getText());
            resultDataListModel.setIntention(entity.getIntentionType());
            resultDataListModel.setSentiment(entity.getSentiment());
            resultDataListModel.setOriginalTextScene(entity.getOriginalTextScene());
            resultDataListModel.setOpinion(entity.getTopic());
            if (StrUtil.isNotBlank(entity.getCarSeriesCode())) {
                List<String> allCarSerieslist = Arrays.asList(StringUtils.split(entity.getCarSeriesCode(), ","));
                if (CollUtil.isNotEmpty(dataParamModel.getOwnCarSeries())) {
                    List<String> list = allCarSerieslist.stream().filter(a -> dataParamModel.getOwnCarSeries().contains(a)).toList();
                    if (CollUtil.isNotEmpty(list)) {
                        resultDataListModel.setOwnCarSeriesList(String.join(",", list));
                    }
                }
                if (CollUtil.isNotEmpty(dataParamModel.getCompetitorsCarSeries())) {
                    List<String> list = allCarSerieslist.stream().filter(a -> dataParamModel.getCompetitorsCarSeries().contains(a)).toList();
                    if (CollUtil.isNotEmpty(list)) {
                        resultDataListModel.setCompetitorsCarSeriesList(String.join(",", list));
                    }
                }
                if (StringUtils.isNotBlank(entity.getMentionCarSeries()) && !entity.getMentionCarSeries().equals("\"[]\"")) {
                    JSONArray objects = JSONUtil.parseArray(entity.getMentionCarSeries());
                    List<String> list = new ArrayList<>();
                    for (int i = 0; i < objects.size(); i++) {
                        String o1 = objects.getStr(i);
                        if (StringUtils.isNotEmpty(o1) && !o1.equals("null")) {
                            list.add(o1.toString());
                        }
                    }
                    List<String> mentionlist = list.stream().filter(a -> !dataParamModel.getOwnCarSeries().contains(a)).toList();
                    resultDataListModel.setMentionCarSeries(StringUtils.join(mentionlist, ","));
                }
            }
            JSONObject entries = JSONUtil.parseObj(resultDataListModel);
            if (ObjectUtil.isNotEmpty(entity.getBizExtAttrs())) {
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(entity.getBizExtAttrs());
                    addNewKey(attrsMap, jsonObject, entries, type);
                } catch (Exception e) {
                    log.info("JSON数据错误bizExtAttrs");
                }
            } else {
                addNewKey(attrsMap, new JSONObject(), entries, type);
            }
            resultDataListModelList.add(entries);
        }
        return resultDataListModelList;
    }


    @Override
    @SwitchClientDS
    public ResultConditionsModel projectConditions(String clientId, ProjectResultDataParamModel dataParamModel) {
        log.info("项目获取同时提及车系入参：{}", JSONUtil.toJsonStr(dataParamModel));
//        AysPostprocessDataEntity entityList = this.baseMapper.projectPostData(dataParamModel);
        ResultConditionsModel resultConditionsModel = new ResultConditionsModel();
        dataParamModel.setLabelTypeLevelFourDisableList(getDisableTagLib(clientId));
        resultConditionsModel.setMentionCarSeriesList(new ArrayList<>());
        String o = this.baseMapper.projectMentionCarSeries(dataParamModel);
        log.info("项目获取同时提及返回：{}", o);
        JSONArray jsonArray = new JSONArray(o);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            String o1 = jsonArray.getStr(i);
            if (StringUtils.isNotEmpty(o1) && !o1.equals("null")) {
                list.add(o1.toString());
            }
        }
        if (CollUtil.isNotEmpty(list)) {
            if (CollUtil.isNotEmpty(dataParamModel.getOwnCarSeries())) {
                List<String> mentionCarList = list.stream().filter(a -> !dataParamModel.getOwnCarSeries().contains(a)).toList();
                log.info("项目获取同时提及车系过滤完本品：{}", mentionCarList);
                if (CollUtil.isNotEmpty(mentionCarList)) {
                    resultConditionsModel.setMentionCarSeriesList(mentionCarList);
                }
            }
        }
        return resultConditionsModel;
    }

    //获取全部禁用的标签
    private Set<String> getDisableTagLib(String clientId) {
        final Result<InsTagLibVo> allDisableTagLibRS = insTagLibServiceClient.findAllDisableTagLibClient(InsTagLibClientModel.builder().appClient(clientId).build());
        if ("200".equals(allDisableTagLibRS.getCode())) {
            if (ObjUtil.isNotNull(allDisableTagLibRS.getResult()) && CollUtil.isNotEmpty(allDisableTagLibRS.getResult().getFinalTagLib())) {
                InsTagLibVo list = allDisableTagLibRS.getResult();
                if (CollUtil.isNotEmpty(list.getFinalTagLib())) {
                    Set<String> collect = list.getFinalTagLib().stream().map(TagLibClientTreeVo::getTagCode).collect(Collectors.toSet());
                    log.info("获取全部禁用标签:{}", collect);
                    return collect;
                } else {
                    log.info("获取全部禁用标签为空");
                }
            }
        }
        return new HashSet<>();
    }

    @Override
    public Boolean testData() {
        List<AysPostprocessDataEntity> aysPostprocessDataEntities = this.baseMapper.selectErrorData();
        log.info("查询数据条数：{}", aysPostprocessDataEntities.size());
        if (CollUtil.isEmpty(aysPostprocessDataEntities)) {
            return false;
        }
        List<CityCodeModel> cityCodeList = iStaDlrCodeCityMappingMinService.getCityCodeList("764547797eb2e192763f5334028d49c9");
        List<AysPostprocessDataEntity> entities = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(cityCodeList)) {
            Map<String, CityCodeModel> codeModelMap = cityCodeList.stream().collect(Collectors.toMap(CityCodeModel::getDealershipCode, a1 -> a1, (k1, k2) -> k1));
            for (AysPostprocessDataEntity aysPostprocessDataEntity : aysPostprocessDataEntities) {

                Map<String, Object> stringObjectMap = new HashMap<>();
                Map<String, Object> stringObjectMap1 = new HashMap<>();
                Map<String, Object> stringObjectMap2 = new HashMap<>();
                Map<String, Object> stringObjectMap3 = new HashMap<>();
                if (ObjectUtil.isNotNull(aysPostprocessDataEntity.getExtFields())) {
                    JSONObject entries = JSONUtil.parseObj(aysPostprocessDataEntity.getExtFields());
                    for (String key : entries.keySet()) {
                        Object value = entries.get(key);
                        if (ObjectUtils.isNotEmpty(value)) {
                            stringObjectMap.put(key, value);
                        }
                    }

                }
                if (ObjectUtil.isNotNull(aysPostprocessDataEntity.getBizExtAttrs())) {
                    JSONObject entries1 = JSONUtil.parseObj(aysPostprocessDataEntity.getBizExtAttrs());
                    for (String key : entries1.keySet()) {
                        Object value = entries1.get(key);
                        stringObjectMap1.put(key, value);
                    }

                }

                if (ObjectUtil.isNotNull(aysPostprocessDataEntity.getBizExtAttrs2())) {
                    JSONObject entries1 = JSONUtil.parseObj(aysPostprocessDataEntity.getBizExtAttrs2());
                    for (String key : entries1.keySet()) {
                        Object value = entries1.get(key);
                        stringObjectMap2.put(key, value);
                    }

                }

                if (ObjectUtil.isNotNull(aysPostprocessDataEntity.getBizExtAttrs3())) {
                    JSONObject entries1 = JSONUtil.parseObj(aysPostprocessDataEntity.getBizExtAttrs3());
                    for (String key : entries1.keySet()) {
                        Object value = entries1.get(key);
                        stringObjectMap3.put(key, value);
                    }

                }
                JSONObject entries = JSONUtil.parseObj(aysPostprocessDataEntity.getBizExtAttrs3());
                if (ObjectUtil.isNotEmpty(entries)) {
                    String dealershipCodePurchase = entries.getStr("dealership_code_purchase");
                    if (MapUtil.isNotEmpty(codeModelMap) && codeModelMap.containsKey(dealershipCodePurchase)) {
                        stringObjectMap.put("city_code", codeModelMap.get(dealershipCodePurchase).getCityCode());
                        stringObjectMap.put("city_name", codeModelMap.get(dealershipCodePurchase).getCityName());
                        stringObjectMap.put("province_code", codeModelMap.get(dealershipCodePurchase).getProvinceCode());
                        stringObjectMap.put("province_name", codeModelMap.get(dealershipCodePurchase).getProvinceName());
                        stringObjectMap.put("big_area_code", codeModelMap.get(dealershipCodePurchase).getBigAreaCode());
                        stringObjectMap.put("big_area_sale", codeModelMap.get(dealershipCodePurchase).getBigAreaSale());
                        stringObjectMap.put("small_area_code", codeModelMap.get(dealershipCodePurchase).getSmallAreaCode());
                        stringObjectMap.put("samll_area_sale", codeModelMap.get(dealershipCodePurchase).getSamllAreaSale());
                    } else {
                        stringObjectMap.put("city_code", "");
                        stringObjectMap.put("city_name", "");
                        stringObjectMap.put("province_code", "");
                        stringObjectMap.put("province_name", "");
                        stringObjectMap.put("big_area_code", "");
                        stringObjectMap.put("big_area_sale", "");
                        stringObjectMap.put("small_area_code", "");
                        stringObjectMap.put("samll_area_sale", "");
                    }
                }
                aysPostprocessDataEntity.setExtFields(stringObjectMap);
                aysPostprocessDataEntity.setBizExtAttrs(stringObjectMap1);
                aysPostprocessDataEntity.setBizExtAttrs2(stringObjectMap2);
                aysPostprocessDataEntity.setBizExtAttrs3(stringObjectMap3);
                entities.add(aysPostprocessDataEntity);
            }
        }
        log.info("本次解析后需保存{}条数据", entities.size());
        if (CollUtil.isNotEmpty(entities)) {
            try {
                //       processPostRulesProducer.pushData(MessageDTO.builder().source("764547797eb2e192763f5334028d49c9").data(entities).build());
            } catch (Exception e) {
                log.error("异常：", e);
            }
        }
        return true;
    }

    /**
     *
     * @param model
     * @return
     */
    @Override
    public Boolean modifyResultdata(ModifyDataModel model) throws Exception {
        try {
            //1、入参数据验证
            this.vlidateModifyDataModel(model);

            //2、实现入参数据的表入库
            iAysBatchUpdateService.save(model);
            log.info("入库成功");
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private boolean vlidateModifyDataModel(ModifyDataModel model) {
        Assert.isTrue(ObjectUtil.isNotNull(model.getType()), "fileName cannot be empty");
        Assert.isTrue(Arrays.asList(1,2).contains( model.getType()) , "【修改结果表功能】参数错误");
        if(1 == model.getType()){
            Assert.isTrue(CollUtil.isNotEmpty(model.getIds()), "ids cannot be empty");
            Assert.isTrue(model.getIds().size() <= 1000, "ids field count exceeds the limit of 1000");
        }else  if(2 == model.getType()){
            Assert.isTrue(CollUtil.isNotEmpty(model.getIds()) || CollUtil.isNotEmpty(model.getFilters()), "ids or getFilters cannot be empty");
        }
        Assert.isTrue(CollUtil.isNotEmpty(model.getAttrs()), "modifyAttrs cannot be empty");

        List<String> allowedEditableFieldsList = config.getResultdataAllowedEditableFields().stream().map(String::trim).toList();
        if (CollUtil.isEmpty(allowedEditableFieldsList)) {
            log.error("resultdataAllowedEditableFields config cannot be empty");
        }
        if(log.isDebugEnabled()){
            log.debug("allowedEditableFieldsList: {}", "\"".concat(allowedEditableFieldsList.stream().map(String::trim).collect(Collectors.joining("\",\""))).concat("\""));
        }

        for (ModifyDataModel.ModifyAttrs modifyAttr : model.getAttrs()) {
            if (!allowedEditableFieldsList.contains(modifyAttr.getField())) {
                log.error("字段名称不在有效范围内 {}", modifyAttr.getField());
                throw new IllegalArgumentException(String.valueOf("字段名称不在有效范围内 ".concat(modifyAttr.getField())));
            }
            log.debug("字段名称 {} 校验成功", modifyAttr.getField());
        }

        return true;
    }

}