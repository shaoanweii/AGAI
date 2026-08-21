package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysModelResltService;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.clients.IModelServiceClient;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AiRequestDataModel;
import com.voc.service.analysis.model.AiResultDataModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.enums.RuleContentType;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName StoreSourceDataNode
 * @createTime 2024年03月07日 10:49
 * @Copyright cuick
 * @Description 调用模型后，保存原始数据，成功后标记前置处理数据为已完成状态
 */
@LiteflowComponent(id = "callModelApiServiceNode", name = "调用模型计算服务节点")
public class CallModelApiServiceNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(CallModelApiServiceNode.class);
    @Autowired
    IModelServiceClient modelServiceClient;
    @Autowired
    AysConvertMapperService convertMapperService;
    @Autowired
    IAysModelResltService resltDataService;
    @Autowired
    IAysPreprocessDataService preprocessDataService;
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;
    @Autowired
    AnalysisConfig config;

    @Override
    public void process() throws Exception {
        log.info("{}", this.getClass().getSimpleName());
        AnlysisDefaultContext context = this.getRequestData();
        final List<AysProcessDataModel> parsedData = context.getProcessData();
        //调用模型计算服务
//        AiResultDataModel dataProcess = AiResultDataModel.builder().build();

        //设置固定token
//        ServiceContextHolder.setToken(config.getDefaultToken());
//        List<AiRequestDataModel> aiRequestDateModelList = convertToParams(parsedData);
//        log.info("调用AI数据解析入参:{}", JSON.toJSONString(aiRequestDateModelList));
//        List<ResultData> resultDataList = new ArrayList<>();
//        for (AiRequestDataModel aiRequestDateModel : aiRequestDateModelList) {
//            ResultData resultData = new ResultData();
//            List<AiData> aiData = new ArrayList<>();
//            AiData data = new AiData();
//            data.setBrand("哈佛");
//            data.setId(aiRequestDateModel.getId());
//            data.setCarSeries("哈弗H6");
//            List<FaultsModel> faults = new ArrayList<>();
//            FaultsModel faultsModel = new FaultsModel();
//            faultsModel.setLevelOne("产品体验");
//            faultsModel.setLevelTwo("空间");
//            faultsModel.setLevelThree("整体空间");
//            faultsModel.setLevelFour("整体空间");
//            faultsModel.setSentiment("正面");
//            faultsModel.setIntention("其他");
//            faults.add(faultsModel);
//            data.setFaults(faults);
//            aiData.add(data);
//            resultData.setResult(aiData);
//            resultDataList.add(resultData);
//        }
//
//        dataProcess.setCode("200");
//        dataProcess.setResult(resultDataList);
//        if (ObjectUtil.isNull(dataProcess) || StrUtil.isBlank(dataProcess.getCode()) || !"200".equals(dataProcess.getCode())) {
//            context.setAnalysisStatus("1");
//            throw new RuntimeException("调用模型计算异常");
//        }

        //设置固定token
        ServiceContextHolder.setToken(config.getDefaultToken());
        List<AiRequestDataModel> aiRequestDateModelList = convertToParams(parsedData);
        if (log.isDebugEnabled()) {
            log.debug("调用AI数据入参:{}", JSON.toJSONString(aiRequestDateModelList));
        }
        log.info("调用AI数据入参:{}", aiRequestDateModelList.size());
        AiResultDataModel dataProcess = modelServiceClient.getDataProcess(context.getWorkId(), aiRequestDateModelList);
        log.info("调用AI数据返回结果:{}", JSON.toJSONString(dataProcess));
        if (ObjectUtil.isNull(dataProcess) || StrUtil.isBlank(dataProcess.getCode()) || !"200".equals(dataProcess.getCode()) || CollectionUtil.isEmpty(dataProcess.getResult())) {
            log.error("data:{}", dataProcess);
            throw new RuntimeException("调用模型计算异常：data=".concat(JSON.toJSONString(dataProcess)));
        }
        //保存原始数据
//        resltDataService.saveBatch(context.getClientId(), parsedData);
        //调用模型计算服务后返回的数据
 //       context.setAiResultDateModel(dataProcess);
        // 修改状态为已完成
        final Set<String> ids = parsedData.stream().map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
        preprocessDataService.modifyToDone(context.getClientId(), ids);

        log.info("调用模型服务完成");
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "workId cannot be empty");
        return true;
    }

    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(), e);
        AnlysisDefaultContext context = this.getRequestData();
        final Set<String> ids = context.getProcessData().stream().map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
        log.info("调用模型计算服务异常数据 ids:{}", ids);
        batchPushRecordV2Service.modifyStatus(context.getClientId(), ids, "-1", "G");
    }

    /**
     * 组装请求模型入参
     *
     * @param parsedData
     * @return
     */
    public List<AiRequestDataModel> convertToParams(List<AysProcessDataModel> parsedData) {
        List<AiRequestDataModel> aiRequestDateModelList = new ArrayList<>();
        for (AysProcessDataModel aysProcessDataModel : parsedData) {
            if (ObjectUtil.isNull(aysProcessDataModel.getData())) {
                continue;
            }

            AiRequestDataModel aiRequestDateModel = new AiRequestDataModel();
//            JSONObject jsonObject = JSON.parseObject(aysProcessDataModel.getData());
            String dataStr = String.valueOf(aysProcessDataModel.getData());
            JSONObject jsonObject = JSON.parseObject(dataStr);
            aiRequestDateModel.setContent(jsonObject.getString("content"));
            if (ObjectUtil.isNotEmpty(aysProcessDataModel.getBizExtAttrs())) {
                JSONObject bizExtAttrs = JSON.parseObject(aysProcessDataModel.getBizExtAttrs().toString());
                aiRequestDateModel.setBrand(bizExtAttrs.getString("dlr_brand"));
                aiRequestDateModel.setCarSeries(bizExtAttrs.getString("base_series"));
                aiRequestDateModel.setCategoryTwo(bizExtAttrs.getString("category_2"));
                aiRequestDateModel.setChannelSubclass(bizExtAttrs.getString("channel_subclass"));
                if (StrUtil.isBlank(aiRequestDateModel.getContent())) {
                    log.info("异常情况原文为空导致传给模型为空的时候");
                    aiRequestDateModel.setContent(bizExtAttrs.getString("content"));
                }
            }
            aiRequestDateModel.setId(aysProcessDataModel.getDataId());
            aiRequestDateModel.setSource(RuleContentType.getByCode(aysProcessDataModel.getContentType()).getText());
            aiRequestDateModelList.add(aiRequestDateModel);
        }
        return aiRequestDateModelList;
    }
}
