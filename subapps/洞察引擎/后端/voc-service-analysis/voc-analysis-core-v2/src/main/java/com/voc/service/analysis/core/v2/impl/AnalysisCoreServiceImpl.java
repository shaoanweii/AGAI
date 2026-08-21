package com.voc.service.analysis.core.v2.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.api.*;
import com.voc.service.analysis.clients.IChannelServiceClient;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ModelTestProducer;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPreRulesProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.*;
import com.voc.service.analysis.v2.api.IAnalysisCoreService;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.common.util.StopWatch;
import com.voc.service.components.kafka.config.TopicConfig;
import com.voc.service.components.mybatis.annotation.SwitchClientDS;
import com.voc.service.insights.engine.enums.RuleContentType;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Title: AnalysisCoreServiceImpl
 * @Package: com.voc.service.analysis.v2.core.v2.impl
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:46
 * @Version:1.0
 */
@Service
public class AnalysisCoreServiceImpl implements IAnalysisCoreService {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisCoreServiceImpl.class);
    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    IAysMetaDataService metaDataService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    IAysPreprocessDataService preprocessDataService;
    @Autowired
    IAysModelResltService modelResltService;
    @Autowired
    IAysModelResltAnalysisService modelResltAnalysisService;
    @Autowired
    IAysModelResltAnalysisValidService modelResltAnalysisValidService;
    @Autowired
    IAysPostprocessValidDataService postprocessValidDataService;
    @Autowired
    IAysPostprocessDataService aysPostprocessDataService;
    @Autowired
    IRuleDataServcie ruleDataServcie;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    IChannelServiceClient iChannelServiceClient;
    @Autowired
    AnalysisConfig config;
    @Autowired
    AysConvertMapperService convertMapperService;

    @Autowired
    ProcessPreRulesProducer processPreRulesProducer;
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;

    @Autowired
    ModelTestProducer modelTestProducer;
    @Autowired
    private KafkaListenerEndpointRegistry registry;
    @Autowired
    TopicConfig topicConfig;

    @Value("${analysis.matedata_saveBatchMq.enable:false}")
    boolean saveBatchMqEnable;
    @Value("${analysis.matedata_saveBatchExtMq.enable:true}")
    boolean saveBatchExTMqEnable;

    @Autowired
    IRmtInsTagInfoViewService iRmtInsTagInfoViewService;

    //    @Transactional
    @Override
    public String push(List<Object> param) throws Exception {
        return this.push(config.clientId, param);
    }

    //    @Transactional
    @Override
    public String push(String clientId, List<Object> param) throws Exception {
        return this.push(clientId, null, null, null, null, param, null, null);
    }

    //    @Transactional
    @Override
    public String push(String clientId, String workId_, String reqeustId, String type, String dataSource, List<Object> param, Integer modelType, Integer showType) throws Exception {
        Assert.isTrue(StrUtil.isNotEmpty(clientId), "getClientId clientId be empty");
        Assert.isTrue(StrUtil.isNotEmpty(reqeustId), "getClientId reqeustId be empty");

        final String workId = StrUtil.isBlank(workId_) ? reqeustId : workId_;
        StringBuilder sb = new StringBuilder();
        sb.append("workId=").append(workId);

        StopWatch stop = new StopWatch();
        stop.start("原始数据保存");
        stop.start("原始数据解析保存");
        //保存解析后的数据
        Set<String> newIds = Set.of();
        if(saveBatchExTMqEnable){
            newIds = metaDataAnalysisService.saveBatchExtMq(clientId, workId, reqeustId, type, dataSource, param, modelType, showType);
        }
        if(saveBatchMqEnable){
            newIds = metaDataAnalysisService.saveBatchMq(clientId, workId, reqeustId, type, dataSource, param, modelType, showType);
        }
//        final Set<String> metaDataAnalysisService.saveBatchExtMq(clientId, workId, reqeustId, type, dataSource, param, modelType, showType);

        //开启前置任务执行
        processPreRulesProducer.pushEvent(MessageDTO.builder().token("初始化：前置任务执行收到条数" + newIds.size() + "推送条数" + newIds.size())
                .source(clientId).type(type).data(newIds).build());

        stop.stop();
        ;
        stop.start("原始数据解析状态数据保存");
//        保存解析后的数据， 再过程中异常数据将被遗弃
        batchPushRecordV2Service.save(clientId, workId, reqeustId, newIds, modelType);
        stop.stop();
        ;
        stop.start("数据字段校验测试");
        //校验必填字段
        final Set<String> valdMsg = this.validatePushData(param);
        if (CollUtil.isNotEmpty(valdMsg)) {
            logger.error("必填项字段校验失败：{}", valdMsg);
            sb.append(" -》 异常数据：").append(valdMsg);
        }
        logger.info(stop.prettyPrint());
        logger.info("保存原始数据完成 {}", workId);
        return workId;
    }


    private Set<String> validatePushData(List<Object> list) {
        try {
            final Set<String> requiredAttrs = config.getPushDataRequired();

            return list.stream()
                    .map(JSONUtil::parseObj)
//                    .filter(ObjectUtil::isNotNull)
//                    .map(data -> data.keySet())
                    .map(data -> {
                        final Set<String> dataAttrs = data.keySet();
                        final Collection<String> intersectionList = CollUtil.intersection(dataAttrs, requiredAttrs);
                        HashSet<String> missingFields = CollUtil.newHashSet(requiredAttrs);
                        missingFields.removeAll(intersectionList);
                        if (CollUtil.isNotEmpty(missingFields)) {
                            return StrUtil.format("{} 字段不能为空！数据={}", missingFields, JSONUtil.toJsonStr(data));
                        }
                        return null;
                    })
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

    //    @Scheduled(fixedDelay = 1 * 500)
    @Override
    public String preRulesProcess() throws Exception {
        /*if (!config.isRunPreRulesScheduled()) {
            logger.warn("配置中未开启次状态 runPreRulesScheduled=true");
            return null;
        }*/
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();

        RLock rlock = config.getProRulesLock();
        try {
            if (!rlock.isLocked()) {
                rlock.lock();
                logger.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< ");

                LiteflowResponse response = flowExecutor.execute2Resp("pre_process_flow", context, context.getWorkId());
                if (!response.isSuccess()) {
                    logger.error("workId:{}  {}", context.getWorkId(), response.getCause());
                    throw new Exception(response.getCause().getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();

            if (rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }

        return "OK";
    }

    //    @Scheduled(fixedDelay = 500)
    @Override
    public String postRulesProcess() throws Exception {
        /*if (!config.isRunPostRulesScheduled()) {
            logger.warn("配置中未开启次状态 runPostRulesScheduled=true");
            return null;
        }*/
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();

        RLock rlock = config.getPostRulesLock();
        try {
            if (!rlock.isLocked()) {
                rlock.lock();
                logger.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< ");

                LiteflowResponse response = flowExecutor.execute2Resp("post_process_flow", context, context.getWorkId());
                if (!response.isSuccess()) {
                    logger.error("workId:{}  {}", context.getWorkId(), response.getCause());
                    throw new Exception(response.getCause().getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            logger.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();

            if (rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }

        return "OK";

    }

    //    @Scheduled(fixedDelay = 2 * 1000)
    @Override
    public String callModel() throws Exception {
        /*if (!config.isRunCallModelScheduled()) {
            logger.warn("配置中未开启次状态 runCallModelScheduled=true");
            return null;
        }*/
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();

        RLock rlock = config.getPostRulesLock();
        String workId = preprocessDataService.findWorkId(context.getClientId());
        if (StringUtil.isBlank(workId)) {
            logger.info("callModel没有要处理的数据");
            return "OK";
        }
        Boolean flag = redisTemplate.opsForValue().setIfAbsent(workId, "1", 1, TimeUnit.HOURS);
        // 加锁失败，已有消费端在此时对此消息进行处理，这里不再做处理
        if (!flag) {
            logger.info("callModel数据重复处理");
            return "OK";
        }

        try {
            if (!rlock.isLocked()) {
                rlock.lock();
                logger.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< ");
                LiteflowResponse response = flowExecutor.execute2Resp("call_model_flow", context, context.getWorkId());
                if (!response.isSuccess()) {
                    logger.error("workId:{}  {}", context.getWorkId(), response.getCause());
                    throw new Exception(response.getCause().getMessage());
                }
            }
        } catch (Exception e) {
            if (StringUtil.isNotBlank(workId)) {
                redisTemplate.delete(workId);
            }
            throw new RuntimeException(e);
        } finally {
            logger.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();
            if (rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }
        return "OK";
    }


    /*@Override
    @SwitchClientDS
    public Object templateProcess(String clientId, List<Object> param) {
        final String workId = DigestUtil.md5Hex(IdWorker.getId());
        StringBuilder sb = new StringBuilder();
        AnlysisDefaultContext context = AnlysisDefaultContext.builder().build();
        RLock rlock = config.getTemplateLock();

        try {
            //保存解析后的数据， 再过程中异常数据将被遗弃
            final List<AysMetaDataAnalysisModel> analysisDataList = metaDataAnalysisService.saveBatch(clientId, workId, param, false);

            //校验必填字段
            final Set<String> valdMsg = this.validatePushData(param);
            if (CollUtil.isNotEmpty(valdMsg)) {
                logger.error("必填项字段校验失败：{}", valdMsg);
                sb.append(" -》 异常数据：").append(valdMsg);
                throw new IllegalArgumentException(valdMsg.toString());
            }

            logger.info("保存原始数据完成 {}", workId);

            if (!rlock.isLocked()) {
                rlock.tryLock();
                logger.info(">>>>>>> 开始执行任务 <<<<<<<<<<<<<< ");
                String contentType = RuleContentType.PreRule.getCode();
                List<AysProcessDataModel> rs = new CopyOnWriteArrayList<>();
                for (AysMetaDataAnalysisModel data : analysisDataList) {
                    AysProcessDataModel cData = convertMapperService.converToAysProcessDataModel(data);
                    cData.setClientId(cData.getClientId());
                    cData.setChannelId(cData.getChannelId());
                    cData.setContentType(cData.getContentType());
                    cData.setWorkId(cData.getWorkId());
                    cData.setData(data.getData());
                    cData.setDataMd5(JSONUtil.toJsonStr(metaDataAnalysisService.getMD5Values(data)));
                    if (ObjectUtil.isNotNull(data.getExtFields())) {
                        cData.setExtFields(JSONUtil.parseObj(data.getExtFields()));
                    }
                    contentType = cData.getContentType();
                    rs.add(cData);
                }
                context.setProcessData(rs);
                context.setWorkId(workId);
                context.setClientId(ServiceContextHolder.getClientId());
                context.setChannelIds(CollUtil.newLinkedHashSet("template"));
                context.setContentType(contentType);
                LiteflowResponse response = flowExecutor.execute2Resp("template_process_flow", context, context.getWorkId());
                if (!response.isSuccess()) {
                    logger.error("workId:{}  {}", context.getWorkId(), response.getCause());
                    throw new Exception(response.getCause().getMessage());
                }
            }
            return this.templateResult(context.getProcessData(), context.getAnalysisStatus());
        } catch (Exception e) {
            logger.error("样本间处理规则错误:", e);
            return this.templateResult(context.getProcessData(), context.getAnalysisStatus());
        } finally {
            logger.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();

            if (rlock != null && rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }
    }*/


    /**
     * 分批接收原始数据服务
     *
     * @param param
     * @return workId
     */
//    @Transactional
    @SwitchClientDS
    @Override
    public String batchPushData(String clientId, String reqeustId, String type, AiBatchPushModel param) throws Exception {
        logger.debug("batchPushData.type:{}", type);
        Assert.isTrue(StrUtil.isNotBlank(reqeustId), "getRequestId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(clientId), "getClientId cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(param.getData()), "getData cannot be empty");
        Object o = redisTemplate.opsForValue().get(reqeustId);
        String workId = null;
        if (!ObjectUtils.isEmpty(o)) {
            workId = o.toString();
        }
        if (ObjectUtil.isEmpty(param.getShowType())) {
            param.setShowType(2);
        }
        String rsWorkId = this.push(clientId, workId, reqeustId, type, param.getDataSource(), param.getData(), Integer.valueOf(param.getModelType()), param.getShowType());
        if (ObjectUtils.isEmpty(workId)) {
            redisTemplate.opsForValue().set(reqeustId, rsWorkId, 2, TimeUnit.HOURS);
        }
        logger.debug("rsWorkId {}", rsWorkId);
        return rsWorkId;

    }

    @Override
    public String testModelMq() throws Exception {

        Map<String, Object> tagList = iRmtInsTagInfoViewService.getTagList();
        try {
            ResultData resultData = new ResultData();
            List<AiData> aiData = new ArrayList<>();
            AiData data = new AiData();
            data.setBrand("哈佛");
            data.setId("1e0fd831ee7f1774a17c98e44ad90dd5");
            data.setCarSeries("哈弗H9");
            List<FaultsModel> faults = new ArrayList<>();
            FaultsModel faultsModel = new FaultsModel();
            faultsModel.setLevelOne("产品体验mq");
            faultsModel.setLevelTwo("空间mq");
            faultsModel.setLevelThree("整体空间mq");
            faultsModel.setLevelFour("整体空间mq");
            faultsModel.setSentiment("正面mq");
            faultsModel.setIntention("其他mq");
            faults.add(faultsModel);
            data.setFaults(faults);
            aiData.add(data);
            resultData.setResult(aiData);
            resultData.setClientId("e11ab369ea4d56a7a64ab0a3c491a2cc");
            modelTestProducer.pushData(MessageDTO.builder().source("e11ab369ea4d56a7a64ab0a3c491a2cc").data(resultData).build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "成功";
    }

    /**
     * 设置clientId值到对象集合数据内
     *
     * @param clientId
     * @param data
     */
//    private List<Object> setClientIdAttr(String clientId, List<Object> data) {
//        return data.stream().map(d -> {
//            cn.hutool.json.JSONObject obj = JSONUtil.parseObj(d);
//            obj.putOnce("clientId", clientId);
//            return JSONUtil.toBean(obj, Object.class);
//        }).collect(Collectors.toList());
//    }

    /**
     * 将规则处理结果数据转换成样板间接口返回数据类型
     *
     * @param data
     * @return
     */
    private List<AysModelResltDataAnalysisModel> templateResult(List<AysProcessDataModel> data, String analysisStatus) {
        List<AysModelResltDataAnalysisModel> aysModelResltDataAnalysisModels = new ArrayList<>();
        if ("1".equals(analysisStatus)) {
            return aysModelResltDataAnalysisModels;
        }
        for (AysProcessDataModel dataModel : data) {
            String dataStr = String.valueOf(dataModel.getData());
            AysModelResltDataAnalysisModel aysModelResltDataAnalysisModel = JSON.parseObject(dataStr, AysModelResltDataAnalysisModel.class);
            logger.debug("样本间解析后数据:{}", JSON.toJSONString(aysModelResltDataAnalysisModel));
            aysModelResltDataAnalysisModels.add(aysModelResltDataAnalysisModel);
        }
        return aysModelResltDataAnalysisModels;
    }

    @Override
    public AysValidResltDataModel valid(AysValidDataModel param) throws Exception {
        logger.info("验证数据入参:{}", JSON.toJSONString(param));
        Assert.isTrue(ObjectUtil.isNotNull(param), "param cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getStartTime()), "getStartTime cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getEndTime()), "getEndTime cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getClientId()), "getClientId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getContentType()), "getContentType cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(param.getValidRuleIds()), "getValidRuleIds cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(param.getChannel()), "getChannel cannot be empty");

        final String workId = DigestUtil.md5Hex(IdWorker.getId());
        param.setWorkId(workId);
        String clientId = param.getClientId();

        try {
            modelResltAnalysisService.moveBatch(param.getClientId(), workId, param);
            logger.info("[校验]保存原始数据完成 {}", workId);

            final String str = JSONUtil.toJsonStr(param);
            logger.debug("validTaskAdapter.param：{}", str);
            redisTemplate.convertAndSend("validTaskAdapter.v2", str);
        } catch (Exception e) {
            ruleDataServcie.setRuleStatusErr(workId, clientId);
            logger.error("验证异常:", e);
        }

        return AysValidResltDataModel.builder().workId(workId).build();

    }


    @Override
    public String validateFlow(AysValidDataModel param) throws Exception {

        AnlysisDefaultContext context = AnlysisDefaultContext.builder()
                .workId(param.getWorkId())
                .clientId(param.getClientId())
                .contentType(param.getContentType())
                .validDataParam(param)
                .build();
        context.getChannelIds().addAll(param.getChannel());

        final String workId = param.getWorkId();
        String clientId = param.getClientId();
        try {
            LiteflowResponse response = flowExecutor.execute2Resp("valid_post_process_flow", context, context.getWorkId());
            if (!response.isSuccess()) {
                logger.error("workId:{}  {}", context.getWorkId(), response.getCause());
                throw new Exception(response.getCause().getMessage());
            }
        } catch (Exception e) {
            ruleDataServcie.setRuleStatusErr(workId, clientId);
            logger.error("validateFlow验证异常:", e);
        } finally {
            logger.info("workId {} 完成 ", context.getWorkId());
            context.getStopWatch().prettyPrint();
        }

        return null;
    }


    @Override
    @SwitchClientDS(objectAttribute = "validDataModel.clientId")
    public PageInfo getValidateList(ValidDataModel validDataModel) {
        List<AysProcessValidDataModel> processValidDataList = postprocessValidDataService.getProcessValidData(validDataModel.getClientId(), validDataModel.getWorkId(),
                validDataModel.getChannelId());
        logger.info("查询验证结果列表:{}", processValidDataList.size());
        if (ObjectUtils.isEmpty(processValidDataList)) {
            return new PageInfo();
        }
        PageInfo pageInfo = new PageInfo(processValidDataList);
        pageInfo.setPageNum(validDataModel.getPageNum());
        pageInfo.setPageSize(validDataModel.getPageSize());
        //转换
        List<ValidateListModel> validateListModels = this.convertToValidateList(processValidDataList, validDataModel.getRulesId(), validDataModel.getClientId());
        if (StrUtil.isNotBlank(validDataModel.getHitState())) {
            validateListModels = validateListModels.stream().filter(v -> v.getHitState().equals(validDataModel.getHitState())).collect(Collectors.toList());
        }
        if (StrUtil.isNotBlank(validDataModel.getDataCompare())) {
            validateListModels = validateListModels.stream().filter(v -> v.getDataCompare().equals(validDataModel.getDataCompare())).collect(Collectors.toList());
        }
        if (CollectionUtil.isEmpty(validateListModels)) {
            return new PageInfo();
        }
        List<ValidateListModel> validateListModelArrayList = new ArrayList<>();
        int total = validateListModels.size();
        List<List<ValidateListModel>> lists = Lists.partition(validateListModels, validDataModel.getPageSize());
        if (validDataModel.getPageNum() <= lists.size() && validDataModel.getPageNum() >= 1) {
            validateListModelArrayList = lists.get(validDataModel.getPageNum() - 1);
        }
        pageInfo.setTotal(total);
        pageInfo.setList(validateListModelArrayList);
        return pageInfo;
    }


    /**
     * 数据转换组装
     *
     * @param processValidDataList
     * @return
     */
    private List<ValidateListModel> convertToValidateList(List<AysProcessValidDataModel> processValidDataList, String rulesId, String clientId) {
        List<ValidateListModel> validateListModelList = new ArrayList<>();
        Map<String, ChannelInfoModel> channelInfoVoMap = null;
        try {
            ChannelInfoParamModel channelInfoParamModel = new ChannelInfoParamModel();
            channelInfoParamModel.setClientId(clientId);
            Result<List<ChannelInfoModel>> allChannel = iChannelServiceClient.findAllChannel(channelInfoParamModel);
            channelInfoVoMap = allChannel.getResult().stream().collect(Collectors.toMap(ChannelInfoModel::getCode, Function.identity()));
        } catch (Exception e) {
            logger.error("获取渠道集合异常:", e);
        }
        for (AysProcessValidDataModel aysProcessValidDataModel : processValidDataList) {
            ValidateListModel validateListModel = new ValidateListModel();
            String dataStr = aysProcessValidDataModel.getDataStr();
            if (StrUtil.isNotBlank(dataStr)) {
                JSONObject jsonObject = JSON.parseObject(dataStr);
                validateListModel.setOriginalText(jsonObject.getString("content"));
            }
            validateListModel.setCarSeriesName(aysProcessValidDataModel.getOldCarSeriesName());
            validateListModel.setOriginalTextScene(aysProcessValidDataModel.getOldOriginalTextScene());
            String newHitRules = aysProcessValidDataModel.getOldHitRules();
            if (MapUtil.isNotEmpty(channelInfoVoMap) && channelInfoVoMap.containsKey(aysProcessValidDataModel.getChannelId())) {
                ChannelInfoModel channelInfoVo = channelInfoVoMap.get(aysProcessValidDataModel.getChannelId());
                validateListModel.setChannelName(channelInfoVo.getName());
            }
            if (StrUtil.isNotBlank(newHitRules) && !"".equals(newHitRules)) {
                try {
                    List<String> ruleIds = JSONUtil.parseObj(newHitRules).getBeanList("rule_ids", String.class);
                    validateListModel.setHitState(ruleIds.contains(rulesId) ? "1" : "0");
                } catch (Exception e) {
                    validateListModel.setHitState("0");
                }
            } else {
                validateListModel.setHitState("0");
            }
            validateListModel.setHitStateStr("0".equals(validateListModel.getHitState()) ? "未命中" : "命中");
            String newProcessingResult = this.getJsonString(aysProcessValidDataModel, 1);
            String originalProcessingResult = "";
            if (aysProcessValidDataModel.getAbandon() != 1) {
                originalProcessingResult = this.getJsonString(aysProcessValidDataModel, 2);
            }
            validateListModel.setNewProcessingResult(originalProcessingResult);
            validateListModel.setOriginalProcessingResult(newProcessingResult);
            String newProcessingResultMd5 = DigestUtil.md5Hex(newProcessingResult);
            String originalProcessingResultMd5 = DigestUtil.md5Hex(originalProcessingResult);
            logger.info("MD5对比加密串:{},{}", newProcessingResultMd5, originalProcessingResultMd5);
            if (newProcessingResultMd5.equals(originalProcessingResultMd5)) {
                validateListModel.setDataCompare("0");
            } else {
                validateListModel.setDataCompare("1");
            }
            validateListModel.setDataCompareStr("0".equals(validateListModel.getDataCompare()) ? "一致" : "不同");
            validateListModelList.add(validateListModel);
        }
        return validateListModelList;
    }


    /**
     * 解析结果转换JSON字符串
     *
     * @param aysProcessValidDataModel
     * @param type
     * @return
     */
    private String getJsonString(AysProcessValidDataModel aysProcessValidDataModel, Integer type) {
        JsonListModel jsonListModel = new JsonListModel();
        jsonListModel.setBrandCodeName(type == 1 ? aysProcessValidDataModel.getNewBrandCodeName() : aysProcessValidDataModel.getOldBrandCodeName());
        jsonListModel.setCarSeriesName(type == 1 ? aysProcessValidDataModel.getNewCarSeriesName() : aysProcessValidDataModel.getOldCarSeriesName());
        jsonListModel.setBusinessLabelTypeLevelFirst(type == 1 ? aysProcessValidDataModel.getNewLabelTypeLevelFirst() : aysProcessValidDataModel.getOldLabelTypeLevelFirst());
        jsonListModel.setBusinessLabelTypeLevelSecond(type == 1 ? aysProcessValidDataModel.getNewLabelTypeLevelSecond() : aysProcessValidDataModel.getOldLabelTypeLevelSecond());
        jsonListModel.setBusinessLabelTypeLevelThree(type == 1 ? aysProcessValidDataModel.getNewLabelTypeLevelThree() : aysProcessValidDataModel.getOldLabelTypeLevelThree());
        jsonListModel.setBusinessLabelTypeLevelFour(type == 1 ? aysProcessValidDataModel.getNewLabelTypeLevelFour() : aysProcessValidDataModel.getOldLabelTypeLevelFour());
        jsonListModel.setScenario(type == 1 ? aysProcessValidDataModel.getNewScenario() : aysProcessValidDataModel.getOldScenario());
        jsonListModel.setSentiment(type == 1 ? aysProcessValidDataModel.getNewSentiment() : aysProcessValidDataModel.getOldSentiment());
        jsonListModel.setIntentionType(type == 1 ? aysProcessValidDataModel.getNewIntentionType() : aysProcessValidDataModel.getOldIntentionType());
        jsonListModel.setTopic(type == 1 ? aysProcessValidDataModel.getNewTopicProportion() : aysProcessValidDataModel.getOldTopicProportion());
        jsonListModel.setSubject(type == 1 ? aysProcessValidDataModel.getNewSubject() : aysProcessValidDataModel.getOldSubject());
        jsonListModel.setFaultLevel(type == 1 ? aysProcessValidDataModel.getNewFaultLevel() : aysProcessValidDataModel.getOldFaultLevel());
        jsonListModel.setDescription(type == 1 ? aysProcessValidDataModel.getNewDescription() : aysProcessValidDataModel.getOldDescription());
        jsonListModel.setSentimentScore(type == 1 ? aysProcessValidDataModel.getNewSentimentScore() : aysProcessValidDataModel.getOldSentimentScore());
        jsonListModel.setKeywords(type == 1 ? aysProcessValidDataModel.getNewKeywords() : aysProcessValidDataModel.getOldKeywords());
        return JSON.toJSONString(jsonListModel, SerializerFeature.WriteNullStringAsEmpty);
    }

    @Override
    public Set<String> resumeAction(final Set<String> topics) {
        Set<String> topicsList = new HashSet<>();
        if (CollUtil.isEmpty(topics)) {
            //获取配置的全部
            topicsList = topicConfig.getTopicList();
        } else {
            topicsList = topics;
        }

        Set<String> finshList = new HashSet<>();
        logger.info("本次执行范围:{}", topicsList);
        //恢复topic消费
        topicsList.forEach(topic -> {
            MessageListenerContainer messageListenerContainer = registry
                    .getListenerContainer(topic);
            if (Objects.nonNull(messageListenerContainer) && messageListenerContainer.isContainerPaused()) {
                logger.info("TOPIC {} 已恢复", topic);
                messageListenerContainer.resume();
                finshList.add(topic);
            }
        });

        return finshList;
    }

    @Override
    public Set<String> pauseAction(final Set<String> topics) {
        Set<String> topicsList = new HashSet<>();
        if (CollUtil.isEmpty(topics)) {
            //获取配置的全部
            topicsList = topicConfig.getTopicList();
        } else {
            topicsList = topics;
        }
        logger.info("本次执行范围:{}", topicsList);
        Set<String> finshList = new HashSet<>();
        //暂停topic消费
        topicsList.forEach(topic -> {
            MessageListenerContainer messageListenerContainer = registry
                    .getListenerContainer(topic);
            if (Objects.nonNull(messageListenerContainer) && !messageListenerContainer.isContainerPaused()) {
                logger.info("TOPIC {} 已暂停", topic);
                messageListenerContainer.pause();
                finshList.add(topic);
            }
        });

        return finshList;
    }


}
