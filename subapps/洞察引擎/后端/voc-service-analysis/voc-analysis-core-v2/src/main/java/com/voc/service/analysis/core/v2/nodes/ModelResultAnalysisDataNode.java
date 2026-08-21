package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.api.IAysModelResltService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.enums.PreDataStatus;
import com.voc.service.analysis.model.*;
import com.voc.service.insights.engine.model.AddHighFrequencyWordsModel;
import com.voc.service.insights.engine.model.AddWordsInfoModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName StoreSourceDataNode
 * @createTime 2024年03月07日 10:49
 * @Copyright cuick
 */
@LiteflowComponent(id = "modelResultAnalysisDataNode", name = "保存模型返回数据解析后的数据节点")
@SuppressWarnings("all")
public class ModelResultAnalysisDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(ModelResultAnalysisDataNode.class);
    @Autowired
    IAysModelResltAnalysisService modelResltAnalysisService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;

    @Autowired
    AysConvertMapperService aysConvertMapperService;
    @Autowired
    IAysModelResltService resltDataService;
    @Autowired
    AysConvertMapperService convertMapperService;
    @Autowired
    AnalysisConfig analysisConfig;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();

        //保存解析后的数据至 ays_api_reslt_data_analysis
        AiResultDataModel aiResultDateModel = new AiResultDataModel();
        List<AysProcessDataModel> aysProcessDataModels = context.getProcessData();
        List<AysModelResltDataAnalysisModel> analysisList = this.getAnalysisList(aiResultDateModel.getResult(), aysProcessDataModels);
        if (CollUtil.isEmpty(analysisList)) {
            context.setAnalysisStatus("1");
            throw new RuntimeException("调用模型计算解析数据为空");
        }
        AysModelResltDataAnalysisModel model = analysisList.stream().findFirst().get();
        context.setWorkId(model.getWorkId());
        context.getChannelIds().add(model.getChannelId());
        context.setClientId(model.getClientId());
        context.setContentType(model.getContentType());

        List<AysProcessDataModel> rs = new CopyOnWriteArrayList<>();
        for (AysModelResltDataAnalysisModel data : analysisList) {
            AysProcessDataModel cData = convertMapperService.converToAysProcessDataModel3(data);
            cData.setClientId(cData.getClientId());
            cData.setChannelId(cData.getChannelId());
            cData.setContentType(cData.getContentType());
            cData.setWorkId(cData.getWorkId());
            cData.setData(JSONUtil.toJsonStr(data));
            if(ObjectUtil.isNotNull(data.getExtFields())) {
                cData.setExtFields(JSONUtil.parseObj(data.getExtFields()));
            }
            rs.add(cData);
        }
        context.setProcessData(rs);
    }

    /**
     * 调用AI结果数据解析组装
     *
     * @param resultDataList
     * @return
     */
    public List<AysModelResltDataAnalysisModel> getAnalysisList(List<ResultData> resultDataList, List<AysProcessDataModel> aysProcessDataModels) {
        AnlysisDefaultContext context = this.getRequestData();
        Map<String, AysModelResltDataAnalysisModel> modelResltDataAnalysisEntityMap = new HashMap<>();
        Map<String, AysProcessDataModel> processDataModelMap = aysProcessDataModels.stream().collect(Collectors.toMap(AysProcessDataModel::getId, m -> m, (key1, key2) -> key2));
        List<AddWordsInfoModel> addWordsInfoModelList = new ArrayList<>();
        List<AysModelResltDataAnalysisMissModel> aysModelResltDataAnalysisMissModels = new ArrayList<>();
        String clientId = null;
        Map<String, Integer> dataStatusMap = new HashMap<>();
        List<String> newIdList = new ArrayList<>();
        for (ResultData resultData : resultDataList) {
            List<AiData> aiDataList = resultData.getResult();
            Map<String, List<AiData>> listMap = aiDataList.stream().collect(Collectors.groupingBy(AiData::getId));
            for (Map.Entry<String, List<AiData>> entry : listMap.entrySet()) {
                List<AiData> dataList = entry.getValue();
                if (CollectionUtil.isEmpty(dataList)) {
                    continue;
                }
                if (CollectionUtil.isNotEmpty(dataList)) {
                    int missData = 0;
                    for (AiData aiData : dataList) {
                        //业务标签
                        List<FaultsModel> faultList = aiData.getFaults();
                        //质量标签
                        List<DimensionsModel> dimensionsList = aiData.getDimensions();
                        AysProcessDataModel aysProcessDataModel = null;
                        if (MapUtil.isNotEmpty(processDataModelMap) && processDataModelMap.containsKey(aiData.getId())) {
                            aysProcessDataModel = processDataModelMap.get(aiData.getId());
                            clientId = aysProcessDataModel.getClientId();
                        }
                        //处理未打标签的数据
                        List<OpinionsModel> opinions = aiData.getUnmarkedOpinions();
                        if (CollectionUtil.isNotEmpty(opinions)) {
                            aysModelResltDataAnalysisMissModels.addAll(this.getMissModelDataList(aiData.getBrandCode(), aiData.getCarSeriesCode(), aysProcessDataModel, opinions));
                        }
                        if (CollectionUtil.isEmpty(faultList) && CollectionUtil.isEmpty(dimensionsList)) {
                            if (ObjectUtil.isNotEmpty(aysProcessDataModel)) {
                                dataStatusMap.put(aysProcessDataModel.getOriginalId(), PreDataStatus.MISS_DATA.getCode());
                            }
                            continue;
                        } else {
                            if (ObjectUtil.isNotEmpty(aysProcessDataModel)) {
                                dataStatusMap.put(aysProcessDataModel.getOriginalId(), PreDataStatus.MARKED_DATA.getCode());
                            }
                        }
                        missData++;
                        Map<String, AysModelResltDataAnalysisModel> analysisEntityMap = new HashMap<>();
                        List<String> systemSuggestedBusiness = new ArrayList<>();
                        List<String> systemSuggestedQuality = new ArrayList<>();
                        if (CollectionUtil.isNotEmpty(faultList)) {
                            for (FaultsModel faults : faultList) {
                                if (MapUtil.isNotEmpty(analysisEntityMap) && analysisEntityMap.containsKey(aiData.getSentence())) {
                                    AysModelResltDataAnalysisModel aysModelResltDataAnalysisEntity = analysisEntityMap.get(aiData.getSentence());
                                    if (ObjectUtil.isNotEmpty(aysModelResltDataAnalysisEntity.getIntentionType()) && aysModelResltDataAnalysisEntity.getIntentionType().equals(faults.getIntention()) &&
                                            ObjectUtil.isNotEmpty(aysModelResltDataAnalysisEntity.getSentiment()) && aysModelResltDataAnalysisEntity.getSentiment().equals(faults.getSentiment()) &&
                                            ObjectUtil.isNotEmpty(aysModelResltDataAnalysisEntity.getTopic()) && aysModelResltDataAnalysisEntity.getTopic().equals(faults.getTopic()) && analysisConfig.isModelMergeDataSwitch()) {
                                        AysModelResltDataAnalysisModel resultDataAnalysisEntity = modelResltDataAnalysisEntityMap.get(aysModelResltDataAnalysisEntity.getDataId());
                                    } else {
                                        AysModelResltDataAnalysisModel dataAnalysis = getDataAnalysis(faults, aiData, 1, aysProcessDataModel);
                                        modelResltDataAnalysisEntityMap.put(dataAnalysis.getDataId(), dataAnalysis);
                                    }
                                } else {
                                    AysModelResltDataAnalysisModel dataAnalysis = getDataAnalysis(faults, aiData, 1, aysProcessDataModel);
                                    modelResltDataAnalysisEntityMap.put(dataAnalysis.getDataId(), dataAnalysis);
                                    analysisEntityMap.put(aiData.getSentence(), dataAnalysis);
                                }
                            }
                        }
                        if (CollectionUtil.isNotEmpty(dimensionsList)) {
                            for (DimensionsModel dimensions : dimensionsList) {
                                FaultsModel faults = new FaultsModel();
                                BeanUtil.copyProperties(dimensions, faults);
                                //如果存在质量业务数据需要做聚合
                                if (MapUtil.isNotEmpty(analysisEntityMap) && analysisEntityMap.containsKey(aiData.getSentence())) {
                                    AysModelResltDataAnalysisModel aysModelResltDataAnalysisEntity = analysisEntityMap.get(aiData.getSentence());
                                    if (ObjectUtil.isNotEmpty(aysModelResltDataAnalysisEntity.getIntentionType()) && aysModelResltDataAnalysisEntity.getIntentionType().equals(faults.getIntention()) &&
                                            ObjectUtil.isNotEmpty(aysModelResltDataAnalysisEntity.getSentiment()) && aysModelResltDataAnalysisEntity.getSentiment().equals(faults.getSentiment()) &&
                                            ObjectUtil.isNotEmpty(aysModelResltDataAnalysisEntity.getTopic()) && aysModelResltDataAnalysisEntity.getTopic().equals(faults.getTopic()) && analysisConfig.isModelMergeDataSwitch()) {
                                        AysModelResltDataAnalysisModel resultDataAnalysisEntity = modelResltDataAnalysisEntityMap.get(aysModelResltDataAnalysisEntity.getDataId());
                                    } else {
                                        AysModelResltDataAnalysisModel dataAnalysis = getDataAnalysis(faults, aiData, 2, aysProcessDataModel);
                                        modelResltDataAnalysisEntityMap.put(dataAnalysis.getDataId(), dataAnalysis);
                                    }
                                } else {
                                    AysModelResltDataAnalysisModel dataAnalysis = getDataAnalysis(faults, aiData, 2, aysProcessDataModel);
                                    modelResltDataAnalysisEntityMap.put(dataAnalysis.getDataId(), dataAnalysis);
                                    analysisEntityMap.put(aiData.getSentence(), dataAnalysis);
                                }
                            }
                        }
                        if (CollectionUtil.isNotEmpty(aiData.getKeywords())) {
                            for (String keyword : aiData.getKeywords()) {
                                AddWordsInfoModel AddWordsInfoModel = new AddWordsInfoModel();
                                AddWordsInfoModel.setWordName(keyword);
                                AddWordsInfoModel.setChannelSource(aysProcessDataModel.getChannelId());
                                AddWordsInfoModel.setSystemSuggestedBusiness(systemSuggestedBusiness);
                                AddWordsInfoModel.setSystemSuggestedQuality(systemSuggestedQuality);
                                addWordsInfoModelList.add(AddWordsInfoModel);
                            }
                        }
                    }
                    if (missData == 0) {
                        newIdList.add(entry.getKey());
                    }
                }
            }
        }
        this.addHighFrequencyWords(addWordsInfoModelList, clientId);
        context.setDataStatusMap(dataStatusMap);
        context.setNewIdList(newIdList);
        context.setModelNotLabelDataList(aysModelResltDataAnalysisMissModels);
        return new ArrayList<>(modelResltDataAnalysisEntityMap.values());
    }

    private void addHighFrequencyWords(List<AddWordsInfoModel> addWordsInfoModelList, String clientId) {
        AnlysisDefaultContext context = this.getRequestData();
        if (CollectionUtil.isNotEmpty(addWordsInfoModelList)) {
            AddHighFrequencyWordsModel addHighFrequencyWordsModel = new AddHighFrequencyWordsModel();
            addHighFrequencyWordsModel.setClientId(clientId);
            addHighFrequencyWordsModel.setAddWordsInfoModelList(addWordsInfoModelList);
            context.setAddHighFrequencyWordsModel(addHighFrequencyWordsModel);
        }
    }


    public List<AysModelResltDataAnalysisMissModel> getMissModelDataList(String brandCode, String carSeriesCode, AysProcessDataModel aysProcessDataModel, List<OpinionsModel> opinions) {
        List<AysModelResltDataAnalysisMissModel> aysModelResltDataAnalysisMissEntities = new ArrayList<>();
        String data = String.valueOf(aysProcessDataModel.getData());
        JSONObject jsonObj = JSONUtil.parseObj(data);
        for (OpinionsModel opinionsModel : opinions) {
            AysModelResltDataAnalysisMissModel aysModelResltDataAnalysisMissEntity = new AysModelResltDataAnalysisMissModel();
            aysModelResltDataAnalysisMissEntity.setDataId(aysProcessDataModel.getDataId());
            aysModelResltDataAnalysisMissEntity.setChannelId(aysProcessDataModel.getChannelId());
            aysModelResltDataAnalysisMissEntity.setId(aysProcessDataModel.getId());
            aysModelResltDataAnalysisMissEntity.setWorkId(aysProcessDataModel.getWorkId());
            aysModelResltDataAnalysisMissEntity.setClientId(aysProcessDataModel.getClientId());
            aysModelResltDataAnalysisMissEntity.setBrandCode(brandCode);
            aysModelResltDataAnalysisMissEntity.setCarSeriesCode(carSeriesCode);
            aysModelResltDataAnalysisMissEntity.setInputDataId(jsonObj.getStr("id"));
            aysModelResltDataAnalysisMissEntity.setOpinion(opinionsModel.getOpinion());
            aysModelResltDataAnalysisMissEntity.setSubject(opinionsModel.getSubject());
            aysModelResltDataAnalysisMissEntity.setOpinionSentiment(opinionsModel.getOpinionSentiment());
            aysModelResltDataAnalysisMissEntity.setDescription(opinionsModel.getDesc());
            aysModelResltDataAnalysisMissEntity.setViewLabel(opinionsModel.getViewLabel());
            aysModelResltDataAnalysisMissEntity.setCarBodyLabel(opinionsModel.getCarBodyLabel());
            aysModelResltDataAnalysisMissEntity.setContentType(aysProcessDataModel.getContentType());
            aysModelResltDataAnalysisMissEntities.add(aysModelResltDataAnalysisMissEntity);
        }
        log.info("未打标签数据:{}", aysModelResltDataAnalysisMissEntities.size());
        return aysModelResltDataAnalysisMissEntities;
    }


    /**
     * 拆分标签
     *
     * @param faults
     * @return
     */
    private String getTagJoint(FaultsModel faults) {
        StringBuilder stringBuilder = new StringBuilder(faults.getLevelOne());
        if (StrUtil.isNotBlank(faults.getLevelTwo())) {
            stringBuilder.append("-").append(faults.getLevelTwo());
        }
        if (StrUtil.isNotBlank(faults.getLevelThree())) {
            stringBuilder.append("-").append(faults.getLevelThree());
        }
        if (StrUtil.isNotBlank(faults.getLevelFour())) {
            stringBuilder.append("-").append(faults.getLevelFour());
        }
        return stringBuilder.toString();
    }

    private AysModelResltDataAnalysisModel getDataAnalysis(FaultsModel faults, AiData aiData,
                                                           Integer tagType, AysProcessDataModel aysProcessDataModel) {
        AysModelResltDataAnalysisModel modelResultDataAnalysis = new AysModelResltDataAnalysisModel();
        modelResultDataAnalysis.setDataId(aysProcessDataModel.getDataId());
        modelResultDataAnalysis.setId(aysProcessDataModel.getId());
        modelResultDataAnalysis.setWorkId(aysProcessDataModel.getWorkId());
        modelResultDataAnalysis.setClientId(aysProcessDataModel.getClientId());
        modelResultDataAnalysis.setBrandCode(aiData.getBrandCode());
        modelResultDataAnalysis.setCarSeriesCode(aiData.getCarSeriesCode());
        modelResultDataAnalysis.setOriginalTextScene(aiData.getSentence());
        modelResultDataAnalysis.setSampleDataType("1");
        modelResultDataAnalysis.setIntentionType(faults.getIntention());
//        String data = aysProcessDataModel.getData();
//        String data = Base64.decodeStr(aysProcessDataModel.getData().replaceAll("\"", ""), CharsetUtil.CHARSET_UTF_8);
        JSONObject jsonObj = JSONUtil.parseObj(String.valueOf(aysProcessDataModel.getData()));
        modelResultDataAnalysis.setInputDataId(jsonObj.getStr("id"));
        modelResultDataAnalysis.setTopic(faults.getTopic());
        modelResultDataAnalysis.setSentiment(faults.getSentiment());
        modelResultDataAnalysis.setDescription(faults.getDesc());
        modelResultDataAnalysis.setFaultLevel(faults.getLevel());
        modelResultDataAnalysis.setOpinion(faults.getOpinion());
        modelResultDataAnalysis.setPublishTime(aysProcessDataModel.getPublishTime());
        modelResultDataAnalysis.setOriginalId(aysProcessDataModel.getOriginalId());
        modelResultDataAnalysis.setSentimentScore(faults.getSentimentScore());
        modelResultDataAnalysis.setSubject(faults.getSubject());
        modelResultDataAnalysis.setScenario(faults.getScenario());
        if (CollectionUtil.isNotEmpty(aiData.getKeywords())) {
            modelResultDataAnalysis.setKeywords(String.join(",", aiData.getKeywords()));
        }
        modelResultDataAnalysis.setChannelId(aysProcessDataModel.getChannelId());
        modelResultDataAnalysis.setContentType(aysProcessDataModel.getContentType());
        return modelResultDataAnalysis;
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(CollUtil.isNotEmpty(context.getProcessData()), "getProcessData cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "workId cannot be empty");

        return true;
    }


}
