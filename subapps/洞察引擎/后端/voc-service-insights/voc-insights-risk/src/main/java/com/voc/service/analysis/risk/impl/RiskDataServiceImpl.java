package com.voc.service.analysis.risk.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.api.IRiskDataService;
import com.voc.service.analysis.enums.StatisticTypeStatus;
import com.voc.service.analysis.model.*;
import com.voc.service.analysis.risk.entity.AllTypesRiskDataEntity;
import com.voc.service.analysis.risk.mapper.RpAllTypesRiskDataMapper;
import com.voc.service.insights.engine.enums.LabelTypeEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@DS("starrock_dndc")
public class RiskDataServiceImpl extends ServiceImpl<RpAllTypesRiskDataMapper, AllTypesRiskDataEntity> implements IRiskDataService {


    @Override
    public PageInfo getRiskResultList(String clientId, RiskDataParamModel paramModel) {
        PageHelper.startPage(paramModel.getPageNum(), paramModel.getPageSize());
        List<AllTypesRiskDataEntity> allTypesRiskDataEntities = this.baseMapper.pageRiskDataList(paramModel);
        if (ObjectUtils.isEmpty(allTypesRiskDataEntities)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(allTypesRiskDataEntities);
        List<AllTypesRiskDataModel> allTypesRiskDataModels = convertProjectToDataList(allTypesRiskDataEntities);
        pageInfo.setList(allTypesRiskDataModels);
        return pageInfo;
    }

    @Override
    public List<AllTypesRiskDataModel> exportRiskResultList(String clientId, RiskDataParamModel paramModel) {
        List<AllTypesRiskDataEntity> allTypesRiskDataEntities = this.baseMapper.pageRiskDataList(paramModel);
        if (ObjectUtils.isNotEmpty(allTypesRiskDataEntities)) {
            return convertProjectToDataList(allTypesRiskDataEntities);
        }
        return null;
    }

    @Override
    public Boolean saveBatchEmotion(String clientId, List<EmotionRiskDataModel> emotionRiskDataModelList) {
        if (ObjectUtils.isEmpty(emotionRiskDataModelList)) {
            return Boolean.FALSE;
        }
        List<AllTypesRiskDataEntity> allTypesRiskDataEntityList = new ArrayList<>();
        for (EmotionRiskDataModel emotionRiskDataModel : emotionRiskDataModelList) {
            AllTypesRiskDataEntity allTypesRiskDataEntity = new AllTypesRiskDataEntity();
            allTypesRiskDataEntity.setId(SecureUtil.md5(emotionRiskDataModel.getId() + emotionRiskDataModel.getProjectId() + emotionRiskDataModel.getStatisticType()));
            allTypesRiskDataEntity.setRisk(emotionRiskDataModel.getLabelTypeLevelFirst() + "-" +
                    emotionRiskDataModel.getLabelTypeLevelSecond() + "-" + emotionRiskDataModel.getLabelTypeLevelThree() + "-" +
                    emotionRiskDataModel.getLabelTypeLevelFour());
            allTypesRiskDataEntity.setRiskName(allTypesRiskDataEntity.getRisk());
            allTypesRiskDataEntity.setCityCode(emotionRiskDataModel.getCityCode());
            allTypesRiskDataEntity.setChannelId(emotionRiskDataModel.getChannelId());
            allTypesRiskDataEntity.setRiskIndex(emotionRiskDataModel.getRiskIndex());
            allTypesRiskDataEntity.setOpinionWordsJson(getKeywords(emotionRiskDataModel.getKeywords()));
            allTypesRiskDataEntity.setProjectId(emotionRiskDataModel.getProjectId());
            allTypesRiskDataEntity.setBrandCodeName(emotionRiskDataModel.getBrandName());
            allTypesRiskDataEntity.setCarSeriesName(emotionRiskDataModel.getCarSeriesName());
            allTypesRiskDataEntity.setStatisticType(emotionRiskDataModel.getStatisticType());
            allTypesRiskDataEntity.setRiskId(emotionRiskDataModel.getId());
            allTypesRiskDataEntity.setFocusName(emotionRiskDataModel.getLabelTypeLevelFour());
            allTypesRiskDataEntity.setOpinionWords(emotionRiskDataModel.getKeywords());
            allTypesRiskDataEntity.setNegativeNum(emotionRiskDataModel.getNegativeNum());
            allTypesRiskDataEntity.setUserNum(emotionRiskDataModel.getUserNum());
            allTypesRiskDataEntity.setChannelNum(emotionRiskDataModel.getChannelNum());
            allTypesRiskDataEntity.setRiskType("1");
            allTypesRiskDataEntity.setComplainNum(emotionRiskDataModel.getComplainNum());
            allTypesRiskDataEntity.setRiskWordsNum(emotionRiskDataModel.getRiskKeywordsNum());
            allTypesRiskDataEntity.setRiskLevel(emotionRiskDataModel.getRiskLevel());
            allTypesRiskDataEntity.setLabelType(emotionRiskDataModel.getLabelType());
            allTypesRiskDataEntity.setLabelTypeLevelFirst(emotionRiskDataModel.getLabelTypeLevelFirst());
            allTypesRiskDataEntity.setLabelTypeLevelSecond(emotionRiskDataModel.getLabelTypeLevelSecond());
            allTypesRiskDataEntity.setLabelTypeLevelThree(emotionRiskDataModel.getLabelTypeLevelThree());
            allTypesRiskDataEntity.setLabelTypeLevelFour(emotionRiskDataModel.getLabelTypeLevelFour());
            allTypesRiskDataEntity.setLabelTypeLevelFive(emotionRiskDataModel.getLabelTypeLevelFive());
            allTypesRiskDataEntity.setNewIdArray(emotionRiskDataModel.getNewIdArray());
            allTypesRiskDataEntity.setCreateTime(emotionRiskDataModel.getPublishDate().plusDays(1));
            allTypesRiskDataEntityList.add(allTypesRiskDataEntity);
        }
        List<List<AllTypesRiskDataEntity>> resultBatch = ListUtil.split(allTypesRiskDataEntityList, 1000);
        for (List<AllTypesRiskDataEntity> r : resultBatch) {
            this.saveBatch(r);
        }
        return Boolean.TRUE;
    }

    private String getKeywords(String keywords) {

        if (StringUtils.isNotEmpty(keywords)) {
            String[] split = StringUtils.split(keywords, ",");
            List<String> labelTypeLevelFourList = Arrays.asList(split);
            JSONObject jsonObject = new JSONObject();
            for (String label : labelTypeLevelFourList) {
                String replaced = label.replaceAll("\"", "");
                if (jsonObject.containsKey(replaced)) {
                    Integer anInt = jsonObject.getInt(replaced);
                    jsonObject.put(replaced, anInt + 1);
                } else {
                    jsonObject.put(replaced, 1);
                }
            }
            return jsonObject.toString();
        }
        return null;
    }

    @Override
    public Boolean saveBatchQuality(String clientId, List<QualityRiskDataModel> qualityRiskDataModelList) {
        if (ObjectUtils.isEmpty(qualityRiskDataModelList)) {
            return Boolean.FALSE;
        }
        List<AllTypesRiskDataEntity> allTypesRiskDataEntityList = new ArrayList<>();
        for (QualityRiskDataModel qualityRiskDataModel : qualityRiskDataModelList) {
            AllTypesRiskDataEntity allTypesRiskDataEntity = new AllTypesRiskDataEntity();
            allTypesRiskDataEntity.setId(SecureUtil.md5(qualityRiskDataModel.getId() + qualityRiskDataModel.getProjectId() + qualityRiskDataModel.getStatisticType()));
            allTypesRiskDataEntity.setRisk(qualityRiskDataModel.getLabelTypeLevelFirst() + "-" +
                    qualityRiskDataModel.getLabelTypeLevelSecond() + "-" + qualityRiskDataModel.getLabelTypeLevelThree() + "-" +
                    qualityRiskDataModel.getLabelTypeLevelFour());
            allTypesRiskDataEntity.setRiskName(qualityRiskDataModel.getLabelTypeLevelFour() + qualityRiskDataModel.getLabelTypeLevelFive());
            allTypesRiskDataEntity.setChannelId(qualityRiskDataModel.getChannelId());
            allTypesRiskDataEntity.setRiskIndex(qualityRiskDataModel.getRiskIndex());
            allTypesRiskDataEntity.setCityCode(qualityRiskDataModel.getCityCode());
            allTypesRiskDataEntity.setOpinionWordsJson(getKeywords(qualityRiskDataModel.getKeywords()));
            allTypesRiskDataEntity.setProjectId(qualityRiskDataModel.getProjectId());
            allTypesRiskDataEntity.setBrandCodeName(qualityRiskDataModel.getBrandName());
            allTypesRiskDataEntity.setCarSeriesName(qualityRiskDataModel.getCarSeriesName());
            allTypesRiskDataEntity.setStatisticType(qualityRiskDataModel.getStatisticType());
            allTypesRiskDataEntity.setRiskId(qualityRiskDataModel.getId());
            allTypesRiskDataEntity.setFocusName(qualityRiskDataModel.getLabelTypeLevelFour());
            allTypesRiskDataEntity.setOpinionWords(qualityRiskDataModel.getKeywords());
            allTypesRiskDataEntity.setNegativeNum(qualityRiskDataModel.getNegativeNum());
            allTypesRiskDataEntity.setUserNum(qualityRiskDataModel.getUserNum());
            allTypesRiskDataEntity.setChannelNum(qualityRiskDataModel.getChannelNum());
            allTypesRiskDataEntity.setRiskType("2");
            allTypesRiskDataEntity.setRiskWordsNum(qualityRiskDataModel.getRiskKeywordsNum());
            allTypesRiskDataEntity.setRiskLevel(qualityRiskDataModel.getRiskLevel());
            allTypesRiskDataEntity.setLabelType(LabelTypeEnum.QY.getCode());
            allTypesRiskDataEntity.setLabelTypeLevelFirst(qualityRiskDataModel.getLabelTypeLevelFirst());
            allTypesRiskDataEntity.setLabelTypeLevelSecond(qualityRiskDataModel.getLabelTypeLevelSecond());
            allTypesRiskDataEntity.setLabelTypeLevelThree(qualityRiskDataModel.getLabelTypeLevelThree());
            allTypesRiskDataEntity.setLabelTypeLevelFour(qualityRiskDataModel.getLabelTypeLevelFour());
            allTypesRiskDataEntity.setLabelTypeLevelFive(qualityRiskDataModel.getLabelTypeLevelFive());
            allTypesRiskDataEntity.setNewIdArray(qualityRiskDataModel.getNewIdArray());
            allTypesRiskDataEntity.setCreateTime(qualityRiskDataModel.getPublishDate().plusDays(1));
            allTypesRiskDataEntityList.add(allTypesRiskDataEntity);
        }
        List<List<AllTypesRiskDataEntity>> resultBatch = ListUtil.split(allTypesRiskDataEntityList, 1000);
        for (List<AllTypesRiskDataEntity> r : resultBatch) {
            this.saveBatch(r);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean saveBatchUser(String clientId, List<UserRiskDataModel> userRiskDataModelList) {
        if (ObjectUtils.isEmpty(userRiskDataModelList)) {
            return Boolean.FALSE;
        }
        List<AllTypesRiskDataEntity> allTypesRiskDataEntityList = new ArrayList<>();
        for (UserRiskDataModel userRiskDataModel : userRiskDataModelList) {
            AllTypesRiskDataEntity allTypesRiskDataEntity = new AllTypesRiskDataEntity();
            allTypesRiskDataEntity.setId(SecureUtil.md5(userRiskDataModel.getId() + userRiskDataModel.getUserName() + userRiskDataModel.getLabelType() + userRiskDataModel.getCityCode() + userRiskDataModel.getProjectId() + userRiskDataModel.getStatisticType()));
            allTypesRiskDataEntity.setRisk(userRiskDataModel.getUserName());
            allTypesRiskDataEntity.setCityCode(userRiskDataModel.getCityCode());
            allTypesRiskDataEntity.setChannelId(userRiskDataModel.getChannelId());
            allTypesRiskDataEntity.setRiskIndex(userRiskDataModel.getRiskIndex());
            allTypesRiskDataEntity.setOpinionWords(userRiskDataModel.getKeywords());
            allTypesRiskDataEntity.setOpinionWordsJson(getKeywords(userRiskDataModel.getKeywords()));
            allTypesRiskDataEntity.setRiskName(userRiskDataModel.getUserName());
            allTypesRiskDataEntity.setProjectId(userRiskDataModel.getProjectId());
            allTypesRiskDataEntity.setBrandCodeName(userRiskDataModel.getBrandName());
            allTypesRiskDataEntity.setRiskId(userRiskDataModel.getId());
            allTypesRiskDataEntity.setNegativeNum(userRiskDataModel.getNegativeNum());
            allTypesRiskDataEntity.setStatisticType(userRiskDataModel.getStatisticType());
            allTypesRiskDataEntity.setRiskType("3");
            allTypesRiskDataEntity.setCarSeriesName(userRiskDataModel.getCarSeriesName());
            allTypesRiskDataEntity.setComplainNum(userRiskDataModel.getComplainNum());
            allTypesRiskDataEntity.setEmotionNum(userRiskDataModel.getEmotionNum());
            allTypesRiskDataEntity.setUserNum(userRiskDataModel.getVoiceNum());
            allTypesRiskDataEntity.setChannelNum(userRiskDataModel.getChannelNum());
            allTypesRiskDataEntity.setRiskLevel(userRiskDataModel.getRiskLevel());
            allTypesRiskDataEntity.setCreateTime(userRiskDataModel.getPublishDate().plusDays(1));
            allTypesRiskDataEntity.setFocusName(userRiskDataModel.getFocusProblem());
            allTypesRiskDataEntity.setLabelType(userRiskDataModel.getLabelType());
            allTypesRiskDataEntity.setLabelTypeLevelFirst(userRiskDataModel.getLabelTypeLevelFirst());
            allTypesRiskDataEntity.setLabelTypeLevelSecond(userRiskDataModel.getLabelTypeLevelSecond());
            allTypesRiskDataEntity.setLabelTypeLevelThree(userRiskDataModel.getLabelTypeLevelThree());
            allTypesRiskDataEntity.setLabelTypeLevelFour(userRiskDataModel.getLabelTypeLevelFour());
            allTypesRiskDataEntity.setLabelTypeLevelFive(userRiskDataModel.getLabelTypeLevelFive());
            allTypesRiskDataEntity.setNewIdArray(userRiskDataModel.getNewIdArray());
            allTypesRiskDataEntityList.add(allTypesRiskDataEntity);
        }
        List<List<AllTypesRiskDataEntity>> resultBatch = ListUtil.split(allTypesRiskDataEntityList, 1000);
        for (List<AllTypesRiskDataEntity> r : resultBatch) {
            this.saveBatch(r);
        }
        return Boolean.TRUE;
    }


    /**
     * 数据转换
     *
     * @param entityList
     * @return
     */
    private List<AllTypesRiskDataModel> convertProjectToDataList(List<AllTypesRiskDataEntity> entityList) {
        List<AllTypesRiskDataModel> allTypesRiskDataModelList = new ArrayList<>();
        for (AllTypesRiskDataEntity allTypesRiskDataEntity : entityList) {
            AllTypesRiskDataModel riskDataModel = new AllTypesRiskDataModel();
            BeanUtil.copyProperties(allTypesRiskDataEntity, riskDataModel);
            StatisticTypeStatus status = StatisticTypeStatus.getByCode(riskDataModel.getStatisticType());
            riskDataModel.setStatisticType(status.getText());
            riskDataModel.setCreateTime(allTypesRiskDataEntity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            riskDataModel.setRiskLevel(riskDataModel.getRiskLevel().equals("S") ? "一级" : riskDataModel.getRiskLevel().equals("A") ? "二级" : "三级");
            if (allTypesRiskDataEntity.getLabelType().equals(LabelTypeEnum.QY.getCode())) {
                allTypesRiskDataEntity.setRiskName(allTypesRiskDataEntity.getLabelTypeLevelFirst() + "-" +
                        allTypesRiskDataEntity.getLabelTypeLevelSecond() + "-" +
                        allTypesRiskDataEntity.getLabelTypeLevelThree() + "-"
                        + allTypesRiskDataEntity.getLabelTypeLevelFour() + "-" + allTypesRiskDataEntity.getLabelTypeLevelFive());
            }
            allTypesRiskDataModelList.add(riskDataModel);
        }
        return allTypesRiskDataModelList;
    }
}
