package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.analysis.api.IAysErrorPushService;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.api.IStaticDataServcie;
import com.voc.service.analysis.core.v2.entity.*;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.mapper.*;
import com.voc.service.analysis.core.v2.producers.kafka.CqCaModelResultAnalysisProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.*;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.enums.RuleLogicalOperator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AysMetaDataService
 * @createTime 2024年03月07日 15:54
 * @Copyright cuick
 */
@Service
public class AysModelResltAnalysisServiceImpl extends ServiceImpl<AysModelResltAnalysisMapper, AysModelResultDataAnalysisEntity>
        implements IAysModelResltAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(AysModelResltAnalysisServiceImpl.class);
    @Autowired
    AysConvertMapperService aysConvertMapperService;

    @Autowired
    IStaticDataServcie staticDataServcie;

    @Autowired
    CqCaModelResultAnalysisProducer modelResultAnalysisProducer;
    @Autowired
    AysAnalFlowModelTagsResultDataExtMapper aysAnalFlowModelTagsResultDataExtMapper;
    @Autowired
    IAysErrorPushService errorPushService;
    @Autowired
    AysModelResltAnalysisMapper aysModelResltAnalysisMapper;
    //    @Autowired
//    AysAnalFlowModelTagsResultDataFullExtMapper aysAnalFlowModelTagsResultDataFullExtMapper;
    @Autowired
    AysChannelInfoDataMapper aysChannelInfoDataMapper;
    @Autowired
    AysExtInsBrandMapper aysExtInsBrandMapper;
    @Autowired
    AysExtInsTagSystemMapper aysExtInsTagSystemMapper;
    @Autowired
    AysInsCarSeriesMapper aysInsCarSeriesMapper;
    @CreateCache(area = "VDP", name = ":ext_reslt:", expire = 10, localExpire = 10, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.LOCAL, serialPolicy = "java")
    private Cache<String, Map<String, ?>> cacheEntity;
    @CreateCache(area = "VDP", name = ":ext_reslt:", expire = 2, localExpire = 2, timeUnit = TimeUnit.HOURS, cacheType = CacheType.LOCAL, serialPolicy = "java")
    private Cache<String, Boolean> cacheMiss;


    private AysChannelInfoDataEntity getAysChannelInfoDataMap(String key) {
        if (StrUtil.isBlank(key) || ObjectUtil.isNotNull(cacheMiss.get(key))) {
            return AysChannelInfoDataEntity.builder().build();
        }
        final String cacheKey = "getAysChannelInfoDataMap";
        if (CollUtil.isNotEmpty(cacheEntity.get(cacheKey))) {
            Object object = cacheEntity.get(cacheKey).get(key);
            if (ObjectUtil.isNotNull(object) && object instanceof AysChannelInfoDataEntity) {
                logger.info("使用缓存：cacheEntity getAysChannelInfoDataMap->{}", key);
                return (AysChannelInfoDataEntity) object;
            } else {
                logger.warn("cacheEntity getAysChannelInfoDataMap error");
            }
        }


        try {
            List<AysChannelInfoDataEntity> result = aysChannelInfoDataMapper.selectList(new QueryWrapper<>());

            Map<String, AysChannelInfoDataEntity> map = result.stream()
                    .collect(Collectors.toMap(AysChannelInfoDataEntity::getCode, e -> e, (v1, v2) -> v2));

            cacheEntity.put(cacheKey, map);
            AysChannelInfoDataEntity val = map.get(key);
            if (ObjectUtil.isNotNull(val)) {
                return val;
            }
            cacheMiss.put(key, true);
            logger.warn("未命中缓存到有效数据 ->{}", key);
            return AysChannelInfoDataEntity.builder().build();
        } catch (Exception e) {
            logger.error("Cache load failed, key: {}", key, e);
            return AysChannelInfoDataEntity.builder().build();
        }
    }


    private AysExtInsBrandEntity getAysBrandDataMap(String key) {
        if (StrUtil.isBlank(key) || ObjectUtil.isNotNull(cacheMiss.get(key))) {
            return AysExtInsBrandEntity.builder().build();
        }
        final String cacheKey = "getAysBrandDataMap";
        if (CollUtil.isNotEmpty(cacheEntity.get(cacheKey))) {
            Object object = cacheEntity.get(cacheKey).get(key);
            if (ObjectUtil.isNotNull(object) && object instanceof AysExtInsBrandEntity) {
                logger.info("使用缓存：cacheEntity getAysBrandDataMap->{}", key);
                return (AysExtInsBrandEntity) object;
            } else {
                logger.warn("cacheEntity getAysBrandDataMap error");
            }
        }

        try {
            List<AysExtInsBrandEntity> result = aysExtInsBrandMapper.selectList(new QueryWrapper<>());

            Map<String, AysExtInsBrandEntity> map = result.stream()
                    .collect(Collectors.toMap(AysExtInsBrandEntity::getCode, e -> e, (v1, v2) -> v2));

            cacheEntity.put(cacheKey, map);
            AysExtInsBrandEntity val = map.get(key);
            if (ObjectUtil.isNotNull(val)) {
                return val;
            }

            cacheMiss.put(key, true);
            logger.warn("未命中缓存到有效数据 ->{}", key);
            return AysExtInsBrandEntity.builder().build();
        } catch (Exception e) {
            logger.error("Cache load failed, key: {}", key, e);
            return AysExtInsBrandEntity.builder().build();
        }
    }

    private AysInsCarSeriesEntity getAysInsCarSeriesMap(String key) {
        if (StrUtil.isBlank(key) || ObjectUtil.isNotNull(cacheMiss.get(key))) {
            return AysInsCarSeriesEntity.builder().build();
        }
        final String cacheKey = "getAysInsCarSeriesMap";
        if (CollUtil.isNotEmpty(cacheEntity.get(cacheKey))) {
            Object object = cacheEntity.get(cacheKey).get(key);
            if (ObjectUtil.isNotNull(object) && object instanceof AysInsCarSeriesEntity) {
                logger.info("使用缓存：cacheEntity getAysInsCarSeriesMap->{}", key);
                return (AysInsCarSeriesEntity) object;
            } else {
                logger.warn("cacheEntity getAysInsCarSeriesMap error");
            }
        }

        try {
            List<AysInsCarSeriesEntity> result = aysInsCarSeriesMapper.selectList(new QueryWrapper<>());

            Map<String, AysInsCarSeriesEntity> map = result.stream()
                    .collect(Collectors.toMap(AysInsCarSeriesEntity::getCode, e -> e, (v1, v2) -> v2));

            cacheEntity.put(cacheKey, map);
            AysInsCarSeriesEntity val = map.get(key);
            if (ObjectUtil.isNotNull(val)) {
                return val;
            }

            cacheMiss.put(key, true);
            logger.warn("未命中缓存到有效数据 ->{}", key);
            return AysInsCarSeriesEntity.builder().build();
        } catch (Exception e) {
            logger.error("Cache load failed, key: {}", key, e);
            return AysInsCarSeriesEntity.builder().build();
        }
    }

    private AysExtInsTagSystemEntity getAysTagsDataMap(String key) {
        if (StrUtil.isBlank(key) || ObjectUtil.isNotNull(cacheMiss.get(key))) {
            return AysExtInsTagSystemEntity.builder().build();
        }
        final String cacheKey = "getAysTagsDataMap";
        if (CollUtil.isNotEmpty(cacheEntity.get(cacheKey))) {
            Object object = cacheEntity.get(cacheKey).get(key);
            if (ObjectUtil.isNotNull(object) && object instanceof AysExtInsTagSystemEntity) {
                logger.info("使用缓存：cacheEntity getAysTagsDataMap->{}", key);
                return (AysExtInsTagSystemEntity) object;
            } else {
                logger.warn("cacheEntity getAysTagsDataMap error");
            }
        }

        try {
            QueryWrapper<AysExtInsTagSystemEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("");

            List<AysExtInsTagSystemEntity> result = aysExtInsTagSystemMapper.selectList(new QueryWrapper<>());

            Map<String, AysExtInsTagSystemEntity> map = result.stream()
                    .collect(Collectors.toMap(AysExtInsTagSystemEntity::getTopic, e -> e, (v1, v2) -> v2));

            cacheEntity.put(cacheKey, map);
            AysExtInsTagSystemEntity val = map.get(key);
            if (ObjectUtil.isNotNull(val)) {
                return val;
            }

            cacheMiss.put(key, true);
            logger.warn("未命中缓存到有效数据 ->{}", key);
            return AysExtInsTagSystemEntity.builder().build();
        } catch (Exception e) {
            logger.error("Cache load failed, key: {}", key, e);
            return AysExtInsTagSystemEntity.builder().build();
        }
    }


    @SwitchClientDS
    @Override
    public void saveBatchExtAnalysis(String clientId, List<AysModelResltDataAnalysisModel> modelResltDataAnalysisModels) throws Exception {

        List<AysAnalFlowModelTagsResultDataExtEntity> analysisEntities = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(modelResltDataAnalysisModels)) {
//            Map<String, AysExtInsBrandEntity> brandMap = this.getAysBrandDataMap();
//            Map<String, AysInsCarSeriesEntity> carSeriesMap = this.getAysInsCarSeriesMap();
//            Map<String, AysExtInsTagSystemEntity> tagMap = this.getAysTagsDataMap();


            for (AysModelResltDataAnalysisModel model : modelResltDataAnalysisModels) {
                AysAnalFlowModelTagsResultDataExtEntity entity = new AysAnalFlowModelTagsResultDataExtEntity();
//                BeanUtil.copyProperties(aysModelResltDataAnalysisModel, entity);

                try {
                    Assert.isTrue(StrUtil.isNotBlank(model.getDataId()), "getDataId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(model.getId()), "getId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(model.getChannelId()), "getChannelId cannot be empty");

                    JSONObject rawDataJsonObj = JSONUtil.parseObj(model.getRawData());
                    JSONObject extFieldsJsonObj = JSONUtil.parseObj(model.getExtFields());
                    JSONObject custExtAttrsJsonObj = JSONUtil.parseObj(model.getCustExtAttrs());
                    JSONObject vhlExtAttrsJsonObj = JSONUtil.parseObj(model.getVhlExtAttrs());
                    JSONObject dealerExtAttrsJsonObj = JSONUtil.parseObj(model.getDealerExtAttrs());
                    JSONObject prdExtAttrsJsonObj = JSONUtil.parseObj(model.getPrdExtAttrs());
                    JSONObject bizExtAttrsJsonObj = JSONUtil.parseObj(model.getBizExtAttrs());
                    JSONObject bizExtAttrs2JsonObj = JSONUtil.parseObj(model.getBizExtAttrs2());

                    final String oneId = StrUtil.blankToDefault(model.getOneId(),
                            StrUtil.join("n_",
                                    DigestUtil.md5Hex(StrUtil.join(model.getChannelId()
                                            , rawDataJsonObj.getStr("userName")
                                            , rawDataJsonObj.getStr("userId")
                                    ))
                            )
                    );
                    logger.debug("id={},data_id={},oneId {}", model.getId(), model.getDataId(), oneId);

                    entity.setDataId(model.getDataId());
                    entity.setId(model.getId());
                    entity.setPublishTime(model.getPublishTime());
                    entity.setChannelCatagory(this.getAysChannelInfoDataMap(model.getChannelId()).getChannelCatagoryLevel1());
                    entity.setChannelCode(model.getChannelId());
                    entity.setChannelName(this.getAysChannelInfoDataMap(model.getChannelId()).getName());
                    entity.setBrandCode(model.getBrandCode());  // brandCode
                    entity.setBrandName(this.getAysBrandDataMap(model.getBrandCode()).getName()); //  brandName
                    entity.setCarSeriesCode(model.getCarSeriesCode());
                    entity.setCarSeriesName(this.getAysInsCarSeriesMap(model.getCarSeriesCode()).getName());
                    entity.setModelName(rawDataJsonObj.getStr("model"));
                    entity.setContentType(model.getContentType());
                    entity.setTitle(rawDataJsonObj.getStr("title"));
                    entity.setContent(rawDataJsonObj.getStr("content"));
                    entity.setSentiment(StrUtil.blankToDefault(model.getSentiment(), this.getAysTagsDataMap(model.getTopic()).getEmotion()));   //标签关联属性
                    entity.setIntention(StrUtil.blankToDefault(model.getIntentionType(), this.getAysTagsDataMap(model.getTopic()).getIntention())); //标签关联属性
                    entity.setDataCreateTime(DateUtil.date(model.getPublishTime()));
                    entity.setCreateTime(model.getCreateTime());
                    entity.setUpdateTime(ObjectUtil.defaultIfNull(model.getUpdateTime(), model.getCreateTime()));
                    entity.setIsOuter(rawDataJsonObj.getStr("isOuter"));
                    entity.setHotWord(null);
                    entity.setKeywords(model.getKeywords());
                    entity.setOriginalTextScene(model.getOriginalTextScene());
                    entity.setMarketId(rawDataJsonObj.getStr("marketId"));//
                    entity.setCompetitiveType(this.getAysBrandDataMap(model.getBrandCode()).getCompetitiveType());
                    entity.setIsCore(this.getAysBrandDataMap(model.getBrandCode()).getIsCore());
                    entity.setSeriesFactory(this.getAysInsCarSeriesMap(model.getCarSeriesCode()).getFactory());
                    entity.setAutomark(this.getAysBrandDataMap(model.getBrandCode()).getAutomark());
                    entity.setOneId(oneId);//
                    entity.setUserJourney1(this.getAysTagsDataMap(model.getTopic()).getUserJourney1());     //标签关联属性
                    entity.setUserJourney2(this.getAysTagsDataMap(model.getTopic()).getUserJourney2());     //标签关联属性
                    entity.setUserJourney3(this.getAysTagsDataMap(model.getTopic()).getUserJourney3());     //标签关联属性
                    entity.setUsageScenarioFirst(extFieldsJsonObj.getStr("usage_scenario_first"));
                    entity.setUsageScenarioSecond(extFieldsJsonObj.getStr("usage_scenario_second"));
                    entity.setD2cResponsibleDept(this.getAysTagsDataMap(model.getTopic()).getD2CResponsibleDept());     //标签关联属性
                    entity.setD2cAccountableDept(this.getAysTagsDataMap(model.getTopic()).getD2CAccountableDept());     //标签关联属性
                    entity.setD2cCcDept(this.getAysTagsDataMap(model.getTopic()).getD2CCcDept());           //标签关联属性
                    entity.setCustGlobalId(custExtAttrsJsonObj.getStr("global_id"));
                    entity.setCustClassify(custExtAttrsJsonObj.getStr("cust_classify"));
                    entity.setCustMainPhone(custExtAttrsJsonObj.getStr("mobile"));
                    entity.setIsCarOwner(custExtAttrsJsonObj.getStr("is_car_owner_flg"));
                    entity.setCustAge(custExtAttrsJsonObj.getStr("age"));
                    entity.setCustAgeGroup(custExtAttrsJsonObj.getStr("age_group"));
                    entity.setCustName(rawDataJsonObj.getStr("userName"));
                    entity.setCustGender(custExtAttrsJsonObj.getStr("gender"));
                    entity.setCustHighEducaion(custExtAttrsJsonObj.getStr("high_educaion"));
                    entity.setMarrigeStatue(custExtAttrsJsonObj.getStr("marriage_statue"));
                    entity.setFamilyIncome(custExtAttrsJsonObj.getStr("family_income"));
                    entity.setIsExchangeFlg(custExtAttrsJsonObj.getStr("is_exchange_flg"));
                    entity.setPurchaseCarTimes(custExtAttrsJsonObj.getStr("purchase_car_times"));
                    entity.setIsMemberFlg(custExtAttrsJsonObj.getStr("is_member_flg"));
                    entity.setCustProvinceCode(custExtAttrsJsonObj.getStr("hukou_prov_cd"));
                    entity.setCustProvince(custExtAttrsJsonObj.getStr("hukou_prov_nm"));
                    entity.setCustCityCode(custExtAttrsJsonObj.getStr("hukou_city_cd"));
                    entity.setCustCity(custExtAttrsJsonObj.getStr("hukou_city_nm"));
                    entity.setCustType(custExtAttrsJsonObj.getStr("cust_type"));
                    entity.setCustLivedProv(custExtAttrsJsonObj.getStr("lived_prov_nm"));
                    entity.setCustLivedCity(custExtAttrsJsonObj.getStr("lived_city_nm"));
                    entity.setCustProfession(custExtAttrsJsonObj.getStr("profession"));
                    Object vhlvinObj = ObjectUtil.defaultIfNull(
                            vhlExtAttrsJsonObj.getByPath("vin"),
                            rawDataJsonObj.getByPath("vhlVin")
                    );
                    entity.setVhlVin(ObjectUtil.isNotNull(vhlvinObj) ? String.valueOf(vhlvinObj) : null);
                    entity.setVhlColorName(vhlExtAttrsJsonObj.getStr("col_name"));
                    entity.setVhlProductDate(vhlExtAttrsJsonObj.getStr("product_date"));
                    entity.setVhlOfflineDate(vhlExtAttrsJsonObj.getStr("offline_date"));
                    entity.setVhlIsAbroad(vhlExtAttrsJsonObj.getStr("home_abroad"));
                    entity.setVhlDisCh(vhlExtAttrsJsonObj.getStr("dis_ch"));
                    entity.setVhlDisMt(vhlExtAttrsJsonObj.getStr("dis_mt"));
                    entity.setVhlEngClsf(vhlExtAttrsJsonObj.getStr("eng_clsf"));
                    entity.setVhlEngSeris(vhlExtAttrsJsonObj.getStr("eng_seris"));
                    entity.setVhlVehType(vhlExtAttrsJsonObj.getStr("veh_type"));
                    entity.setVhlCountry(prdExtAttrsJsonObj.getStr("bd_clsf"));
                    entity.setVhlBdClsf(prdExtAttrsJsonObj.getStr("bd_clsf"));
                    entity.setVhlSegMt(prdExtAttrsJsonObj.getStr("seg_mt"));
                    entity.setVhlPowClsf(prdExtAttrsJsonObj.getStr("pow_clsf"));
                    entity.setVhlFuClsf(prdExtAttrsJsonObj.getStr("fu_clsf"));
                    entity.setVhlModlSt(prdExtAttrsJsonObj.getStr("modl_st"));
                    entity.setVhlStdPlntCode(vhlExtAttrsJsonObj.getStr("plnt_code"));
                    entity.setDlrOcId(dealerExtAttrsJsonObj.getStr("sk_id"));
                    entity.setDlrOcCode(dealerExtAttrsJsonObj.getStr("dlr_cd"));
                    entity.setDlrOcName(dealerExtAttrsJsonObj.getStr("dlr_nm"));
                    entity.setDlrOcProvinceCode(dealerExtAttrsJsonObj.getStr("prov_cd"));
                    entity.setDlrOcProvince(dealerExtAttrsJsonObj.getStr("prov_nm"));
                    entity.setDlrOcCityCode(dealerExtAttrsJsonObj.getStr("city_cd"));
                    entity.setDlrOcCity(dealerExtAttrsJsonObj.getStr("city_nm"));
                    entity.setDlrDcId(dealerExtAttrsJsonObj.getStr("sk_id"));
                    entity.setDlrDcCode(dealerExtAttrsJsonObj.getStr("dlr_cd"));
                    entity.setDlrDcName(dealerExtAttrsJsonObj.getStr("dlr_nm"));
                    entity.setDlrDcProvinceCode(dealerExtAttrsJsonObj.getStr("prov_cd"));
                    entity.setDlrDcProvince(dealerExtAttrsJsonObj.getStr("prov_nm"));
                    entity.setDlrDcCityCode(dealerExtAttrsJsonObj.getStr("city_cd"));
                    entity.setDlrDcCity(dealerExtAttrsJsonObj.getStr("city_nm"));
                    entity.setDlrMcId(dealerExtAttrsJsonObj.getStr("sk_id"));
                    entity.setDlrMcCode(dealerExtAttrsJsonObj.getStr("dlr_cd"));
                    entity.setDlrMcName(dealerExtAttrsJsonObj.getStr("dlr_nm"));
                    entity.setDlrMcProvinceCode(dealerExtAttrsJsonObj.getStr("prov_cd"));
                    entity.setDlrMcProvince(dealerExtAttrsJsonObj.getStr("prov_nm"));
                    entity.setDlrMcCityCode(dealerExtAttrsJsonObj.getStr("city_cd"));
                    entity.setDlrMcCity(dealerExtAttrsJsonObj.getStr("city_nm"));
                    entity.setIsWsaterArmy(rawDataJsonObj.getStr("isWsaterArmy"));//
                    entity.setIsManagerFocused(bizExtAttrs2JsonObj.getStr("is_manager_focused"));
                    entity.setIsBigV(bizExtAttrs2JsonObj.getStr("is_big_v"));
                    entity.setAuthorId(bizExtAttrs2JsonObj.getStr("author_id"));
                    entity.setAuthorNick(bizExtAttrs2JsonObj.getStr("author_nick"));
                    entity.setIsMainPost(bizExtAttrs2JsonObj.getStr("is_main_post"));
                    entity.setOriginalLink(bizExtAttrs2JsonObj.getStr("url"));
                    entity.setViewCount(bizExtAttrs2JsonObj.getStr("view_count"));
                    entity.setCommentCount(bizExtAttrs2JsonObj.getStr("comment_count"));
                    entity.setLikeCount(bizExtAttrs2JsonObj.getStr("like_count"));
                    entity.setShareCount(bizExtAttrs2JsonObj.getStr("share_count"));
                    entity.setFavoriteCount(bizExtAttrs2JsonObj.getStr("favorite_count"));
                    entity.setWorkOrderId(bizExtAttrs2JsonObj.getStr("order_id"));
                    entity.setQuestId(bizExtAttrs2JsonObj.getStr("quest_id"));
                    entity.setQuestType(bizExtAttrs2JsonObj.getStr("quest_type"));
                    entity.setQuestAnswerScore(bizExtAttrs2JsonObj.getStr("quest_answer_score"));
                    entity.setQuestBusinessType(bizExtAttrs2JsonObj.getStr("quest_business_type"));
                    entity.setQuestBusinessScenario(bizExtAttrs2JsonObj.getStr("quest_business_scenario"));
                    entity.setTagAccuracy(this.getAysTagsDataMap(model.getTopic()).getTagAccuracy());
                    entity.setTagCustomerIssueClassification(this.getAysTagsDataMap(model.getTopic()).getTagCustomerIssueClassification());
                    entity.setTagIssueSeverity(this.getAysTagsDataMap(model.getTopic()).getTagIssueSeverity());
                    entity.setTagCodeStatus(this.getAysTagsDataMap(model.getTopic()).getTagCodeStatus());
                    entity.setTagBusinessDomain(this.getAysTagsDataMap(model.getTopic()).getTagBusinessDomain());
                    entity.setTagEventClarity(this.getAysTagsDataMap(model.getTopic()).getEventClarity());
                    entity.setTagHighValueFlag(extFieldsJsonObj.getStr("quest_business_type"));
                    entity.setTagComplaintFlagNeedingReply(this.getAysTagsDataMap(model.getTopic()).getTagComplaintFlagNeedingReply());
                    entity.setTagComplaintFlagNeedingPrtvMsg(extFieldsJsonObj.getStr("tag_complaint_flag_needing_prtv_msg"));
                    entity.setTagHighQualityVocFlag(extFieldsJsonObj.getStr("tag_high_quality_voc_flag"));
                    entity.setTagNewEnergyOrFuel(this.getAysTagsDataMap(model.getTopic()).getTagNewEnergyOrFuel());
                    entity.setTagNeedForvclosedLoop(this.getAysTagsDataMap(model.getTopic()).getTagNeedForvclosedLoop());
                    entity.setTagSort(this.getAysTagsDataMap(model.getTopic()).getSort());
                    entity.setTopic(model.getTopic());
                    entity.setTopicText(this.getAysTagsDataMap(model.getTopic()).getTopicText());
                    entity.setOpinion(model.getOpinion());
                    entity.setCptTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getCptTagFirstCode());
                    entity.setCptTagFirst(this.getAysTagsDataMap(model.getTopic()).getCptTagFirst());
                    entity.setUjyTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getUjyTagFirstCode());
                    entity.setCptTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getCptTagFirstCode());
                    entity.setCptTagSecondCode(this.getAysTagsDataMap(model.getTopic()).getCptTagSecondCode());
                    entity.setCptTagThreeCode(this.getAysTagsDataMap(model.getTopic()).getCptTagThreeCode());
                    entity.setCptTagFourCode(this.getAysTagsDataMap(model.getTopic()).getCptTagFourCode());
                    entity.setCptTagFirst(this.getAysTagsDataMap(model.getTopic()).getCptTagFirst());
                    entity.setCptTagSecond(this.getAysTagsDataMap(model.getTopic()).getCptTagSecond());
                    entity.setCptTagThree(this.getAysTagsDataMap(model.getTopic()).getCptTagThree());
                    entity.setCptTagFour(this.getAysTagsDataMap(model.getTopic()).getCptTagFour());
                    entity.setUjyTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getUjyTagFirstCode());
                    entity.setUjyTagSecondCode(this.getAysTagsDataMap(model.getTopic()).getUjyTagSecondCode());
                    entity.setUjyTagThreeCode(this.getAysTagsDataMap(model.getTopic()).getUjyTagThreeCode());
                    entity.setUjyTagFourCode(this.getAysTagsDataMap(model.getTopic()).getUjyTagFourCode());
                    entity.setUjyTagFirst(this.getAysTagsDataMap(model.getTopic()).getUjyTagFirst());
                    entity.setUjyTagSecond(this.getAysTagsDataMap(model.getTopic()).getUjyTagSecond());
                    entity.setUjyTagThree(this.getAysTagsDataMap(model.getTopic()).getUjyTagThree());
                    entity.setUjyTagFour(this.getAysTagsDataMap(model.getTopic()).getUjyTagFour());
                    entity.setCmaTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getCmaTagFirstCode());
                    entity.setCmaTagSecondCode(this.getAysTagsDataMap(model.getTopic()).getCmaTagSecondCode());
                    entity.setCmaTagThreeCode(this.getAysTagsDataMap(model.getTopic()).getCmaTagThreeCode());
                    entity.setCmaTagFourCode(this.getAysTagsDataMap(model.getTopic()).getCmaTagFourCode());
                    entity.setCmaTagFirst(this.getAysTagsDataMap(model.getTopic()).getCmaTagFirst());
                    entity.setCmaTagSecond(this.getAysTagsDataMap(model.getTopic()).getCmaTagSecond());
                    entity.setCmaTagThree(this.getAysTagsDataMap(model.getTopic()).getCmaTagThree());
                    entity.setCmaTagFour(this.getAysTagsDataMap(model.getTopic()).getCmaTagFour());
                    entity.setDomTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getDomTagFirstCode());
                    entity.setDomTagSecondCode(this.getAysTagsDataMap(model.getTopic()).getDomTagSecondCode());
                    entity.setDomTagThreeCode(this.getAysTagsDataMap(model.getTopic()).getDomTagThreeCode());
                    entity.setDomTagFourCode(this.getAysTagsDataMap(model.getTopic()).getDomTagFourCode());
                    entity.setDomTagFirst(this.getAysTagsDataMap(model.getTopic()).getDomTagFirst());
                    entity.setDomTagSecond(this.getAysTagsDataMap(model.getTopic()).getDomTagSecond());
                    entity.setDomTagThree(this.getAysTagsDataMap(model.getTopic()).getDomTagThree());
                    entity.setDomTagFour(this.getAysTagsDataMap(model.getTopic()).getDomTagFour());
                    entity.setNpsTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getNpsTagFirstCode());
                    entity.setNpsTagSecondCode(this.getAysTagsDataMap(model.getTopic()).getNpsTagSecondCode());
                    entity.setNpsTagThreeCode(this.getAysTagsDataMap(model.getTopic()).getNpsTagThreeCode());
                    entity.setNpsTagFourCode(this.getAysTagsDataMap(model.getTopic()).getNpsTagFourCode());
                    entity.setNpsTagFirst(this.getAysTagsDataMap(model.getTopic()).getNpsTagFirst());
                    entity.setNpsTagSecond(this.getAysTagsDataMap(model.getTopic()).getNpsTagSecond());
                    entity.setNpsTagThree(this.getAysTagsDataMap(model.getTopic()).getNpsTagThree());
                    entity.setNpsTagFour(this.getAysTagsDataMap(model.getTopic()).getNpsTagFour());
                    entity.setVtrTagFirstCode(this.getAysTagsDataMap(model.getTopic()).getVtrTagFirstCode());
                    entity.setVtrTagSecondCode(this.getAysTagsDataMap(model.getTopic()).getVtrTagSecondCode());
                    entity.setVtrTagThreeCode(this.getAysTagsDataMap(model.getTopic()).getVtrTagThreeCode());
                    entity.setVtrTagFourCode(this.getAysTagsDataMap(model.getTopic()).getVtrTagFourCode());
                    entity.setVtrTagFirst(this.getAysTagsDataMap(model.getTopic()).getVtrTagFirst());
                    entity.setVtrTagSecond(this.getAysTagsDataMap(model.getTopic()).getVtrTagSecond());
                    entity.setVtrTagThree(this.getAysTagsDataMap(model.getTopic()).getVtrTagThree());
                    entity.setVtrTagFour(this.getAysTagsDataMap(model.getTopic()).getVtrTagFour());
                    entity.setAbandon(StrUtil.blankToDefault(model.getAbandon(), "0"));
                    entity.setSourceDataId(bizExtAttrs2JsonObj.getStr("source_data_id"));

                    Object retweetedUserIdObj = ObjectUtil.defaultIfNull(
                            bizExtAttrsJsonObj.getByPath("retweeted.user.uid_org"),
                            bizExtAttrsJsonObj.getByPath("retweeted.user.uid")
                    );
                    entity.setRetweetedUserId(ObjectUtil.isNotNull(retweetedUserIdObj) ? String.valueOf(retweetedUserIdObj) : null);
                    Object retweetedUserNameObj = bizExtAttrsJsonObj.getByPath("retweeted.user.name");
                    entity.setRetweetedUserName(ObjectUtil.isNotNull(retweetedUserNameObj) ? String.valueOf(retweetedUserNameObj) : null);
                    Object retweetedUrl = bizExtAttrsJsonObj.getByPath("retweeted.url");
                    entity.setRetweetedUrl(ObjectUtil.isNotNull(retweetedUrl) ? String.valueOf(retweetedUrl) : null);
                    Object retweetedContent = bizExtAttrsJsonObj.getByPath("retweeted.content");
                    entity.setRetweetedContent(ObjectUtil.isNotNull(retweetedContent) ? String.valueOf(retweetedContent) : null);
                    Object retweetedTitle = bizExtAttrsJsonObj.getByPath("retweeted.title");
                    entity.setRetweetedTitle(ObjectUtil.isNotNull(retweetedTitle) ? String.valueOf(retweetedTitle) : null);
                    Object retweetedTime = bizExtAttrsJsonObj.getByPath("retweeted.ctime");
                    entity.setRetweetedTime(ObjectUtil.isNotNull(retweetedTime) ? String.valueOf(retweetedTime) : null);
                    Object commentUrl = bizExtAttrsJsonObj.getByPath("user.url");
                    entity.setCommentUrl(ObjectUtil.isNotNull(commentUrl) ? String.valueOf(commentUrl) : null);
                    Object commentUserName = bizExtAttrsJsonObj.getByPath("user.name");
                    entity.setCommentUserName(ObjectUtil.isNotNull(commentUserName) ? String.valueOf(commentUserName) : null);
                    Object commentUserId = ObjectUtil.defaultIfNull(
                            bizExtAttrsJsonObj.getByPath("user.uid_org"),
                            bizExtAttrsJsonObj.getByPath("user.uid")
                    );
                    entity.setCommentUserId(ObjectUtil.isNotNull(commentUserId) ? String.valueOf(commentUserId) : null);

                    Object isMainPost = bizExtAttrs2JsonObj.getByPath("is_main_post");
                    Object oneIdRisk = retweetedUserIdObj;
                    if (ObjectUtil.isNotNull(isMainPost) && "Y".equals(String.valueOf(isMainPost))) {
                        oneIdRisk = commentUserId;
                    }
                    entity.setOneIdRisk(ObjectUtil.isNotNull(oneIdRisk) ? String.valueOf(oneIdRisk) : null);
                    entity.setAdType(model.getAdType());
                    //标记成未完成最终迁移状态
                    entity.setDone(0);

//                    entity.setHighQuality(value);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    //异常入库数据纪录
                    errorPushService.push(ErrorPushModel
                            .builder()
                            .table("voc_anal_flow_model_tags_result_data_full_ext")
                            .clientId(clientId)
                            .action(IAysErrorPushService.ACTION_ADD)
                            .data(model)
                            .workId(model.getWorkId())
                            .tid(ServiceContextHolder.traceId())
                            .build());
                    continue;
                }

                // 将 entity 中属性值为空字符串的设置为 null（Hutool 实现）
                Field[] fields = ReflectUtil.getFields(entity.getClass());
                for (Field field : fields) {
                    try {
                        Object value = ReflectUtil.getFieldValue(entity, field);
                        if (value instanceof String && StrUtil.isBlank((String) value)) {
                            ReflectUtil.setFieldValue(entity, field, null);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process field: {}", field.getName(), e);
                    }
                }

                try {
                    Assert.isTrue(ObjectUtil.isNotNull(entity.getPublishTime()), "getPublishTime cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getContentType()), "getContentType cannot be empty");
                    Assert.isTrue(ObjectUtil.isNotNull(entity.getDataCreateTime()), "getDataCreateTime cannot be empty");
                    Assert.isTrue(ObjectUtil.isNotNull(entity.getCreateTime()), "getCreateTime cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getIsOuter()), "getIsOuter cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getOneId()), "getOneId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getTopic()), "getTopic cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getAbandon()), "getAbandon cannot be empty");
                    Assert.isTrue(ObjectUtil.isNotNull(entity.getInsertDt()), "getInsertDt cannot be empty");

                    analysisEntities.add(entity);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        } else {
            logger.debug("moveBatch: no data");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("pushExtData: analysisEntities -> {}", analysisEntities.size());
            logger.debug("analysisEntities -> {}", JSONUtil.toJsonPrettyStr(analysisEntities));
        }
        modelResultAnalysisProducer.pushExtData(MessageDTO.builder().data(analysisEntities).build());

    }

    @SwitchClientDS
    @Override
    public Set<String> findUnmigratedDataScop(String clientId) {
        Set<String> ids = new HashSet<>();

        List<AysAnalFlowModelTagsResultDataExtEntity> list = aysAnalFlowModelTagsResultDataExtMapper.selectList(
                new QueryWrapper<AysAnalFlowModelTagsResultDataExtEntity>()
                        .select("id")
                        .in("done", 0)
        );
        if (CollUtil.isEmpty(list)) {
            logger.debug("findResultDataIds: no data");
            return ids;
        }

        ids = list.stream().map(AysAnalFlowModelTagsResultDataExtEntity::getId).collect(Collectors.toSet());
        logger.debug("findResultDataIds: size: {}, ids -> {}", ids.size(), ids);
        return ids;
    }

    public List<AysAnalFlowModelTagsResultDataExtModel> finchResultData(String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            logger.debug("fincResultData: no data");
            return null;
        }

        List<AysAnalFlowModelTagsResultDataExtEntity> list = aysAnalFlowModelTagsResultDataExtMapper.selectList(
                new QueryWrapper<AysAnalFlowModelTagsResultDataExtEntity>()
                        .in("id", ids)
        );

        List<AysAnalFlowModelTagsResultDataExtModel> modelList = list.stream().map(entity -> {
            AysAnalFlowModelTagsResultDataExtModel model = new AysAnalFlowModelTagsResultDataExtModel();
            BeanUtil.copyProperties(entity, model);
            return model;
        }).collect(Collectors.toList());

        return modelList;
    }

    @SwitchClientDS
    @Override
    public void modifyUnmigratedDataScopToDone(String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            logger.debug("modifyUnmigratedDataScopToDone: no data");
            return;
        }

        List<List<String>> subList = CollUtil.split(ids, 500);
        for (List<String> subs : subList) {
            aysAnalFlowModelTagsResultDataExtMapper.update(
                    AysAnalFlowModelTagsResultDataExtEntity.builder().done(1).build(),
                    new UpdateWrapper<AysAnalFlowModelTagsResultDataExtEntity>()
                            .in("id", subs)
            );
            logger.debug("modifyUnmigratedDataScopToDone: size: {}, ids -> {}", subs.size(), subs);
        }
    }


    @SwitchClientDS
    @Override
    public Long moveBatch(String clientId, final String workId, AysValidDataModel validResltDataParam) throws Exception {
        try {
            Assert.isTrue(ObjectUtil.isNotNull(validResltDataParam), "getValidResltDataParam cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(validResltDataParam.getStartTime()), "getStartTime cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(validResltDataParam.getEndTime()), "getEndTime cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(validResltDataParam.getClientId()), "getClientId cannot be empty");
//            Assert.isTrue(StrUtil.isNotBlank(validResltDataParam.getChannelId()), "getChannelId cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(validResltDataParam.getContentType()), "getContentType cannot be empty");
            Assert.isTrue(CollUtil.isNotEmpty(validResltDataParam.getChannel()), "getChannel cannot be empty");
            Assert.isTrue(StrUtil.isNotBlank(workId), "workId cannot be empty");
            logger.debug("validResltDataParam {}", validResltDataParam);

            /*final Long count = baseMapper.selectCount_(validResltDataParam);
            if (count > 0) {
                validResltDataParam.setWorkId(workId);
                final Long count = this.baseMapper.moveBatch(validResltDataParam);
                logger.info("moveBatch success {}", count);
            }*/
            List<AysValidAttributeModel> attrs = validResltDataParam.getAttrs();
            List<AysValidAttributeModel> resourceGroupList = attrs.stream().filter(a -> "resourceGroup".equals(a.getConditionType())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(resourceGroupList)) {
                Map<String, Set<String>> allEnabledResourceGroup = staticDataServcie.getAllEnabledResourceGroup(validResltDataParam.getClientId());
                for (AysValidAttributeModel aysValidAttributeModel : resourceGroupList) {
                    Set<String> nameList = allEnabledResourceGroup.get(aysValidAttributeModel.getConditionDetail());
                    aysValidAttributeModel.setConditionDetailList(new ArrayList<>(nameList));
                }
            }
            final Long count = this.baseMapper.moveBatch(validResltDataParam);
            logger.info("moveBatch success {}", count);

            return count;
        } catch (Exception e) {
            throw e;
        }
    }

    @SwitchClientDS
    @Override
    public void saveBatchAnalysis(String clientId, List<AysModelResltDataAnalysisModel> modelResltDataAnalysisModels) throws Exception {

        List<AysModelResultDataAnalysisEntity> analysisEntities = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(modelResltDataAnalysisModels)) {
            for (AysModelResltDataAnalysisModel aysModelResltDataAnalysisModel : modelResltDataAnalysisModels) {
                AysModelResultDataAnalysisEntity entity = new AysModelResultDataAnalysisEntity();
                BeanUtil.copyProperties(aysModelResltDataAnalysisModel, entity);
                if (ObjectUtil.isNotNull(aysModelResltDataAnalysisModel.getExtFields())) {
                    entity.setExtFields(JSONUtil.parseObj(aysModelResltDataAnalysisModel.getExtFields()));
                }

                try {
                    Assert.isTrue(StrUtil.isNotBlank(entity.getDataId()), "getDataId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getId()), "getId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getWorkId()), "getWorkId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getClientId()), "getClientId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getChannelId()), "getChannelId cannot be empty");
                    Assert.isTrue(StrUtil.isNotBlank(entity.getOriginalId()), "getOriginalId cannot be empty");
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    //异常入库数据纪录
                    errorPushService.push(ErrorPushModel
                            .builder()
                            .table("voc_anal_flow_model_tags_result_data_full")
                            .clientId(clientId)
                            .action(IAysErrorPushService.ACTION_ADD)
                            .data(entity)
                            .workId(entity.getWorkId())
                            .tid(ServiceContextHolder.traceId())
                            .build());
                    continue;
                }
                analysisEntities.add(entity);
            }
        }
        modelResultAnalysisProducer.pushData(MessageDTO.builder().data(analysisEntities).build());

    }

    @SwitchClientDS
    @Override
    public int modifyToDone(String clientId, Set<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        UpdateWrapper<AysModelResultDataAnalysisEntity> wrapper = new UpdateWrapper<>();
        wrapper.in("id", ids);
        wrapper.set("done", "1");
        return this.baseMapper.update(null, wrapper);
    }

    @Override
    public void moveModelResultDataToFinalTable(String clientId, Set<String> ids) {
        aysAnalFlowModelTagsResultDataExtMapper.insertBatch(ids);
    }

    @SwitchClientDS
    @Override
    public List<AysModelResltDataAnalysisModel> findByWorkId(String clientId, String workId) {
        List<AysModelResultDataAnalysisEntity> entityList = this.list(new QueryWrapper<AysModelResultDataAnalysisEntity>()
                .eq("work_id", workId)
                .eq("done", "0")
        );

        return aysConvertMapperService.converToAysModelResltDataAnalysisModel(entityList);
    }

    @SwitchClientDS(objectAttribute = "param.clientId")
    @Override
    public long dataCount(AysValidDataModel param) {
        //校验 param.conditionType 合法性
        if (CollUtil.isNotEmpty(param.getAttrs())) {
            for (AysValidAttributeModel attr : param.getAttrs()) {
                Optional.of(RuleLogicalOperator.getByCode(attr.getLogicalOperator()))
                        .orElseThrow(() -> new IllegalArgumentException("逻辑运算符不合法".concat(attr.getLogicalOperator())));
            }
        }
        List<AysValidAttributeModel> attrs = param.getAttrs();
        List<AysValidAttributeModel> resourceGroupList = attrs.stream().filter(a -> "resourceGroup".equals(a.getConditionType())).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(resourceGroupList)) {
            Map<String, Set<String>> allEnabledResourceGroup = staticDataServcie.getAllEnabledResourceGroup(param.getClientId());
            logger.info("");
            if (CollUtil.isEmpty(allEnabledResourceGroup)) {
                return 0;
            }
            for (AysValidAttributeModel aysValidAttributeModel : resourceGroupList) {
                Set<String> nameList = allEnabledResourceGroup.get(aysValidAttributeModel.getConditionDetail());
                if (CollUtil.isEmpty(nameList)) {
                    aysValidAttributeModel.setConditionDetailList(new ArrayList<>());
                } else {
                    aysValidAttributeModel.setConditionDetailList(new ArrayList<>(nameList));
                }
            }
        }
        logger.info("param: {}", JSONUtil.toJsonStr(param));
        return this.baseMapper.dataCount(param);
    }

    @SwitchClientDS
    @Override
    public long removeHistoryData(String clientId, int days) {
        return this.baseMapper.removeHistoryData(days);
    }

    /*   @Override
       public Set<String> findIincompleteData() {
           return this.baseMapper.findIincompleteData();
       }*/
    @SwitchClientDS
    @Override
    public List<AysModelResltDataAnalysisModel> findByIds(String clientId, Set<String> ids) {
        List<AysModelResultDataAnalysisEntity> entityList = this.list(
                new QueryWrapper<AysModelResultDataAnalysisEntity>()
                        .in("id", ids)
        );
        List<AysModelResltDataAnalysisModel> aysModelResltDataAnalysisModelList = new ArrayList<>();
        if (CollectionUtil.isEmpty(entityList)) {
            return aysModelResltDataAnalysisModelList;
        }
        for (AysModelResultDataAnalysisEntity entity : entityList) {
            AysModelResltDataAnalysisModel aysModelResltDataAnalysisModel = new AysModelResltDataAnalysisModel();
            BeanUtil.copyProperties(entity, aysModelResltDataAnalysisModel);
            aysModelResltDataAnalysisModelList.add(aysModelResltDataAnalysisModel);
        }
        return aysModelResltDataAnalysisModelList;
    }

    @SwitchClientDS
    @Override
    public ResultConditionsModel conditions(String clientId, ResultConditionsParamModel paramModel) {
        List<AysModelResultDataAnalysisEntity> entityList = aysModelResltAnalysisMapper.findByWorkId(paramModel);
        if (ObjectUtils.isEmpty(entityList)) {
            return null;
        }
        ResultConditionsModel resultConditionsModel = new ResultConditionsModel();
        List<BrandCarModel> brandCarModelList = new ArrayList<>();
        Map<String, List<AysModelResultDataAnalysisEntity>> analysisMap = entityList.stream().filter(e -> StringUtils.isNotBlank(e.getBrandCode())).collect(Collectors.groupingBy(AysModelResultDataAnalysisEntity::getBrandCode));
        if (MapUtil.isEmpty(analysisMap)) {
            return resultConditionsModel;
        }
        for (Map.Entry<String, List<AysModelResultDataAnalysisEntity>> entry : analysisMap.entrySet()) {
            BrandCarModel brandCarModel = new BrandCarModel();
            brandCarModel.setBrandName(entry.getKey());
            Set<String> carNameList = entry.getValue().stream().map(AysModelResultDataAnalysisEntity::getCarSeriesCode).collect(Collectors.toSet());
            brandCarModel.setCarName(carNameList);
            brandCarModelList.add(brandCarModel);
        }
        resultConditionsModel.setBrandCarModelList(brandCarModelList);
        return resultConditionsModel;
    }

    /**
     * 未处理数据集合
     *
     * @param paramIds
     * @return
     */
    @SwitchClientDS
    @Override
    public Set<String> unprocessedIds(String clientId, Set<String> paramIds) {
        if (ObjectUtils.isNotEmpty(paramIds)) {
            List<AysModelResultDataAnalysisEntity> aysMetaDataAnalysisEntities = this.baseMapper.selectList(
                    new QueryWrapper<AysModelResultDataAnalysisEntity>()
                            .select("id", "data_id")
                            .in("id", paramIds)
                            .eq("done", "0"));
            if (ObjectUtils.isNotEmpty(aysMetaDataAnalysisEntities)) {
                //已处理完成的ids
                final Set<String> processedIds = aysMetaDataAnalysisEntities.stream().map(AysModelResultDataAnalysisEntity::getId).collect(Collectors.toSet());
                Collection<String> rs = CollUtil.intersection(paramIds, processedIds);
                if (CollUtil.isNotEmpty(rs)) {
                    return new HashSet<>(rs);
                }
            }
        }
        return null;
    }

    /**
     * 返回未处理的数据
     * }
     *
     * @param paramIds
     * @return
     */
    @SwitchClientDS
    @Override
    public Set<String> isExitsIds(String clientId, Set<String> paramIds) {
        if (ObjectUtils.isNotEmpty(paramIds)) {
            List<AysModelResultDataAnalysisEntity> aysMetaDataAnalysisEntities = baseMapper.selectList(
                    new QueryWrapper<AysModelResultDataAnalysisEntity>()
                            .select("id")
                            .in("id", paramIds));
            if (ObjectUtils.isNotEmpty(aysMetaDataAnalysisEntities)) {
                //已处理完成的ids
                /*Set<String> processedIds = aysMetaDataAnalysisEntities.stream().map(AysModelResltDataAnalysisEntity::getNewId).collect(Collectors.toSet());
                return new HashSet<>(CollUtil.union(paramIds, processedIds));*/
                return aysMetaDataAnalysisEntities.stream().map(AysModelResultDataAnalysisEntity::getId).collect(Collectors.toSet());
            }
        }
        return new HashSet<>();
    }
}
