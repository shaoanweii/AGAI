package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.largeModel.ModelTopicRequest;
import com.voc.service.analysis.largeModel.vo.Dimension;
import com.voc.service.analysis.largeModel.vo.ModelResponseVo;
import com.voc.service.analysis.largeModel.vo.NlpResult;
import com.voc.service.analysis.largeModel.vo.StandardKeyword;
import com.voc.service.analysis.model.AysModelResltDataAnalysisMissModel;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.common.util.IdWorker;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: analysisModelResultDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/24 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "analysisModelResultDataNode", name = "模型数据解析节点")
public class AnalysisModelResultDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(AnalysisModelResultDataNode.class);

    @Override
    public void process() throws Exception {
        log.info("开始解析模型结果数据");
        AnlysisDefaultContext context = this.getRequestData();
        ModelResponseVo modelResponseVo = context.getModelResponseVo();
        ModelTopicRequest nlpParam = modelResponseVo.getNlpParam();
        AysProcessDataModel aysProcessDataModel = JSONUtil.toBean(JSONUtil.toJsonStr(nlpParam.getSource_data().getExt()), AysProcessDataModel.class);
        List<AysModelResltDataAnalysisModel> aysModelResultDataAnalysisModels = this.analysisAiData(modelResponseVo, aysProcessDataModel);
        context.setModelLabelDataList(aysModelResultDataAnalysisModels);
    }

    /**
     * 调用AI结果数据解析组装
     *
     * @return
     */
    public List<AysModelResltDataAnalysisModel> analysisAiData(ModelResponseVo modelResponseVo, AysProcessDataModel aysProcessDataModel) throws Exception {

        log.info(">>>>>>>开始解析模型数据<<<<<<<");
        AnlysisDefaultContext context = this.getRequestData();

        // 未达标数据集合
        List<AysModelResltDataAnalysisMissModel> aysModelResultDataAnalysisMissModels = new ArrayList<>();
        // 已达标数据集合
        List<AysModelResltDataAnalysisModel> aysModelResltDataAnalysisModelList = new ArrayList<>();
        // 更新数据状态集合
        Map<String, Integer> dataStatusMap = new HashMap<>();

        // 模型返回结果信息
        NlpResult nlpResult = modelResponseVo.getNlpResult();
        // 模型返回入参信息
        ModelTopicRequest nlpParam = modelResponseVo.getNlpParam();
        List<Dimension> dimensionList = nlpResult.getDimensions();

        // 定义状态码常量
        final int EMPTY_DIMENSION_STATUS = 2;
        final int SUCCESS_DATA_STATUS = 3;

        // 模型结果为空
        if (ObjectUtil.isEmpty(nlpResult) || CollectionUtil.isEmpty(dimensionList)) {
            aysModelResultDataAnalysisMissModels.add(this.getMissModelDataList(aysProcessDataModel, null));
            dataStatusMap.put(aysProcessDataModel.getDataId(), EMPTY_DIMENSION_STATUS);
        }
        if (CollectionUtil.isNotEmpty(dimensionList)) {
            for (Dimension dimension : dimensionList) {
                try {
                    StandardKeyword standardKeyword = dimension.getStandard_keyword();
                    if (ObjectUtil.isEmpty(standardKeyword)) {
                        aysModelResultDataAnalysisMissModels.add(this.getMissModelDataList(aysProcessDataModel, dimension));
                        continue;
                    }
                    AysModelResltDataAnalysisModel dataAnalysis = this.getDataAnalysis(dimension, aysProcessDataModel);
                    aysModelResltDataAnalysisModelList.add(dataAnalysis);
                } catch (Exception e) {
                    log.error("模型数据解析异常，topic_id: {}", nlpParam.getTopic_id(), e);
                    if (ObjectUtil.isNotEmpty(aysProcessDataModel)) {
                        dataStatusMap.put(aysProcessDataModel.getDataId(), EMPTY_DIMENSION_STATUS);
                    }
                }
            }
        }
        if (CollectionUtil.isNotEmpty(aysModelResltDataAnalysisModelList)) {
            dataStatusMap.put(aysProcessDataModel.getDataId(), SUCCESS_DATA_STATUS);
        }

        context.setDataStatusMap(dataStatusMap);
        context.setModelNotLabelDataList(aysModelResultDataAnalysisMissModels);

        log.info(">>>>>>>本次解析完未达标加无效数据条数：{}", aysModelResultDataAnalysisMissModels.size());
        Set<String> dataIdCount = aysModelResltDataAnalysisModelList.stream()
                .map(AysModelResltDataAnalysisModel::getInputDataId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        log.info(">>>>>>>本次解析完已达标数据条数：{}", dataIdCount.size());
        log.info(">>>>>>>解析模型数据结束<<<<<<<");
        return aysModelResltDataAnalysisModelList;
    }


    public AysModelResltDataAnalysisMissModel getMissModelDataList(AysProcessDataModel aysProcessDataModel, Dimension dimension) {
        JSONObject jsonObj = JSONUtil.parseObj(String.valueOf(aysProcessDataModel.getData()));
        AysModelResltDataAnalysisMissModel aysModelResultDataAnalysisMissEntity = new AysModelResltDataAnalysisMissModel();
        aysModelResultDataAnalysisMissEntity.setId(IdWorker.getId());
        aysModelResultDataAnalysisMissEntity.setChannelId(aysProcessDataModel.getChannelId());
        aysModelResultDataAnalysisMissEntity.setDataId(aysProcessDataModel.getDataId());
        aysModelResultDataAnalysisMissEntity.setWorkId(aysProcessDataModel.getWorkId());
        aysModelResultDataAnalysisMissEntity.setClientId(aysProcessDataModel.getClientId());
        aysModelResultDataAnalysisMissEntity.setPublishTime(aysProcessDataModel.getPublishTime());
        aysModelResultDataAnalysisMissEntity.setInputDataId(jsonObj.getStr("id"));
        aysModelResultDataAnalysisMissEntity.setContentType(aysProcessDataModel.getContentType());
        aysModelResultDataAnalysisMissEntity.setModelType(aysProcessDataModel.getModelType());
        aysModelResultDataAnalysisMissEntity.setCreateTime(LocalDateTime.now());
        aysModelResultDataAnalysisMissEntity.setRawData(aysProcessDataModel.getData());
        // 设置维度相关字段
        if (ObjectUtil.isNotNull(dimension)) {
            setDimensionFields(aysModelResultDataAnalysisMissEntity, dimension);
        }
        // 设置业务扩展字段
        setBizExtAttrs(aysModelResultDataAnalysisMissEntity, aysProcessDataModel);

        return aysModelResultDataAnalysisMissEntity;
    }

    private AysModelResltDataAnalysisModel getDataAnalysis(Dimension dimension, AysProcessDataModel aysProcessDataModel) {
        AysModelResltDataAnalysisModel modelResultDataAnalysis = new AysModelResltDataAnalysisModel();

        String md5Id = SecureUtil.md5(dimension.getSentiment() + dimension.getStandard_keyword().getId() + dimension.getIntent() + aysProcessDataModel.getDataId() + dimension.getBrand() + dimension.getSeries());
        modelResultDataAnalysis.setDataId(aysProcessDataModel.getDataId());
        modelResultDataAnalysis.setId(md5Id);
        modelResultDataAnalysis.setWorkId(aysProcessDataModel.getWorkId());
        modelResultDataAnalysis.setClientId(aysProcessDataModel.getClientId());
        modelResultDataAnalysis.setOneId(aysProcessDataModel.getOneId());
        modelResultDataAnalysis.setBrandCode(dimension.getBrand());
        modelResultDataAnalysis.setCarSeriesCode(dimension.getSeries());
        modelResultDataAnalysis.setOriginalTextScene(dimension.getSegment());
        modelResultDataAnalysis.setSampleDataType("1");
        modelResultDataAnalysis.setIntentionType(dimension.getIntent());
        modelResultDataAnalysis.setModelType(aysProcessDataModel.getModelType());
        modelResultDataAnalysis.setRawData(aysProcessDataModel.getData());
        modelResultDataAnalysis.setAdType(aysProcessDataModel.getAdType());
        // 设置使用场景字段
        setUsageScenarioFields(modelResultDataAnalysis, dimension);
        if (ObjectUtil.isNotNull(modelResultDataAnalysis.getExtFields())) {
            modelResultDataAnalysis.setExtFields(modelResultDataAnalysis.getExtFields());
        }
        String dataStr = String.valueOf(aysProcessDataModel.getData());
        JSONObject jsonObj = JSONUtil.parseObj(dataStr);
        modelResultDataAnalysis.setInputDataId(jsonObj.getStr("id"));

        if (ObjectUtil.isNotNull(dimension.getStandard_keyword())) {
            modelResultDataAnalysis.setTopic(dimension.getStandard_keyword().getId());
        }

        if (ObjectUtil.isNotNull(dimension.getNormalized_opinion())) {
            modelResultDataAnalysis.setOpinion(dimension.getNormalized_opinion().getText());
        }

        modelResultDataAnalysis.setSentiment(dimension.getSentiment());
        modelResultDataAnalysis.setDescription(dimension.getDescription());
        modelResultDataAnalysis.setPublishTime(aysProcessDataModel.getPublishTime());
        modelResultDataAnalysis.setOriginalId(aysProcessDataModel.getDataId());
        modelResultDataAnalysis.setSubject(dimension.getAspect());
        modelResultDataAnalysis.setScenario(dimension.getUsage_scenario());
        modelResultDataAnalysis.setChannelId(aysProcessDataModel.getChannelId());
        modelResultDataAnalysis.setContentType(aysProcessDataModel.getContentType());

        // 设置业务扩展字段
        setBizExtAttrs(modelResultDataAnalysis, aysProcessDataModel);

        return modelResultDataAnalysis;
    }

    /**
     * 设置使用场景字段
     */
    private void setUsageScenarioFields(AysModelResltDataAnalysisModel model, Dimension dimension) {
        Map<String, Object> map = new HashMap<>();
        map.put("tag_high_quality_voc_flag", 0);
        if (StringUtils.isNotBlank(dimension.getUsage_scenario()) && dimension.getUsage_scenario().contains("%～%")) {
            String[] split = dimension.getUsage_scenario().split("%～%");
            map.put("usage_scenario_first", split[0]);
            map.put("usage_scenario_second", split[1]);
        } else {
            if (StringUtils.isNotBlank(dimension.getUsage_scenario()) && dimension.getUsage_scenario().contains("-")) {
                String[] split = dimension.getUsage_scenario().split("-");
                map.put("usage_scenario_first", split[0]);
                map.put("usage_scenario_second", split[1]);
            }
        }
        if (ObjectUtil.isNotNull(dimension.getNormalized_opinion()) && dimension.getStandard_keyword().getId() != null) {
            map.put("normalized_opinion_id", dimension.getNormalized_opinion());
        }
        if (ObjectUtil.isNotNull(dimension.getStandard_keyword())) {
            map.put("normalized_opinion_id", dimension.getStandard_keyword());
        }
        if (StringUtils.isNotBlank(dimension.getCar_level1())) {
            map.put("car_level1", dimension.getCar_level1());
        }
        if (StringUtils.isNotBlank(dimension.getCar_level2())) {
            map.put("car_level2", dimension.getCar_level2());
        }
        model.setExtFields(JSONUtil.parseObj(map));
    }

    /**
     * 设置维度相关字段
     */
    private void setDimensionFields(AysModelResltDataAnalysisMissModel model, Dimension dimension) {
        if (StringUtils.isNotBlank(dimension.getUsage_scenario()) && dimension.getUsage_scenario().contains("-")) {
            Map<String, String> map = new HashMap<>();
            String[] split = dimension.getUsage_scenario().split("-");
            map.put("usage_scenario_first", split[0]);
            map.put("usage_scenario_second", split[1]);
            model.setExtFields(JSONUtil.parseObj(map));
        }
        model.setBrandCode(dimension.getBrand());
        model.setCarSeriesCode(dimension.getSeries());

        if (ObjectUtil.isNotNull(dimension.getNormalized_opinion())) {
            model.setOpinion(dimension.getNormalized_opinion().getText());
        }
        model.setDescription(dimension.getDescription());
        model.setSubject(dimension.getAspect());
    }

    /**
     * 设置业务扩展字段
     */
    private void setBizExtAttrs(AysModelResltDataAnalysisMissModel model, AysProcessDataModel processData) {
        if (ObjectUtil.isNotNull(processData.getBizExtAttrs())) {
            model.setBizExtAttrs(processData.getBizExtAttrs());
        }
        if (ObjectUtil.isNotNull(processData.getBizExtAttrs2())) {
            model.setBizExtAttrs2(processData.getBizExtAttrs2());
        }
        if (ObjectUtil.isNotNull(processData.getBizExtAttrs3())) {
            model.setBizExtAttrs3(processData.getBizExtAttrs3());
        }
    }

    /**
     * 设置业务扩展字段
     */
    private void setBizExtAttrs(AysModelResltDataAnalysisModel model, AysProcessDataModel processData) {
        if (ObjectUtil.isNotNull(processData.getBizExtAttrs())) {
            model.setBizExtAttrs(processData.getBizExtAttrs());
        }
        if (ObjectUtil.isNotNull(processData.getBizExtAttrs2())) {
            model.setBizExtAttrs2(processData.getBizExtAttrs2());
        }
        if (ObjectUtil.isNotNull(processData.getBizExtAttrs3())) {
            model.setBizExtAttrs3(processData.getBizExtAttrs3());
        }
        if (ObjectUtil.isNotNull(processData.getCustExtAttrs())) {
            model.setCustExtAttrs(processData.getCustExtAttrs());
        }
        if (ObjectUtil.isNotNull(processData.getDealerExtAttrs())) {
            model.setDealerExtAttrs(processData.getDealerExtAttrs());
        }
        if (ObjectUtil.isNotNull(processData.getVhlExtAttrs())) {
            model.setVhlExtAttrs(processData.getVhlExtAttrs());
        }
        if (ObjectUtil.isNotNull(processData.getPrdExtAttrs())) {
            model.setPrdExtAttrs(processData.getPrdExtAttrs());
        }
    }


    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(), e);
    }

}
