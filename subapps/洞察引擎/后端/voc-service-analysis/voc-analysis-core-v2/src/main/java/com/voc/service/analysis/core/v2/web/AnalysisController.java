package com.voc.service.analysis.core.v2.web;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisValidService;
import com.voc.service.analysis.api.IDataBackupStrategyService;
import com.voc.service.analysis.api.IRuleDataServcie;
import com.voc.service.analysis.api.IStaticDataServcie;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.schedule.MetaDataStatusBatchUpdateJob;
import com.voc.service.analysis.enums.ModelTypeEnum;
import com.voc.service.analysis.model.AiBatchPushModel;
import com.voc.service.analysis.model.AysValidDataModel;
import com.voc.service.analysis.model.ValidDataModel;
import com.voc.service.analysis.v2.api.IAnalysisCoreService;
import com.voc.service.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 09:54
 * @描述:
 **/
@Tag(name = "数据清洗服务")
@RestController
@RequestMapping("/")
public class AnalysisController {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);
    @Autowired
    IAysModelResltAnalysisValidService resltValidDataService;
    @Autowired
    IDataBackupStrategyService dataBackupStrategyService;

    @Autowired
    IAnalysisCoreService analysisCoreService;
    @Autowired
    AnalysisConfig analysisConfig;
    @Autowired
    IStaticDataServcie staticDataServcie;
    @Autowired
    IRuleDataServcie ruleDataServcie;

    //    @AutoLog(value = "基础信息-获取能源信息")
    @Operation(summary = "数据分析入口")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/push")
//    Result<?> process(@RequestBody List<AnalysisDataModel> data) throws Exception {
//    Result<?> push(@RequestBody PushParamDto param) throws Exception {
    Result<?> push(@RequestBody List<Object> param) throws Exception {
        try {
            Assert.isTrue(ObjectUtil.isNotNull(param), "param cannot be empty");
//            Assert.isTrue(CollUtil.isNotEmpty(param.getData()), "data cannot be empty");
//            Assert.isTrue(StrUtil.isNotEmpty(param.getClientId()), "getClientId cannot be empty");
//            Assert.isTrue(StrUtil.isNotEmpty(param.getChannelId()), "getChannelId cannot be empty");
//            Assert.isTrue(StrUtil.isNotEmpty(param.getContentType()), "getContentType cannot be empty");

            return Result.OK(analysisCoreService.push(param));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return Result.error("数据分析接口异常");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("数据分析接口异常");
        }
    }

    @Operation(summary = "数据分析入口")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/push/{clientId}")
//    Result<?> process(@RequestBody List<AnalysisDataModel> data) throws Exception {
//    Result<?> push(@RequestBody PushParamDto param) throws Exception {
    Result<?> push2(@RequestBody List<Object> param, @PathVariable(name = "clientId") String clientId) throws Exception {
        try {
            Assert.isTrue(ObjectUtil.isNotNull(param), "param cannot be empty");
//            Assert.isTrue(CollUtil.isNotEmpty(param.getData()), "data cannot be empty");
            Assert.isTrue(StrUtil.isNotEmpty(clientId), "getClientId clientId be empty");
//            Assert.isTrue(StrUtil.isNotEmpty(param.getChannelId()), "getChannelId cannot be empty");
//            Assert.isTrue(StrUtil.isNotEmpty(param.getContentType()), "getContentType cannot be empty");

            return Result.OK(analysisCoreService.push(clientId, param));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("数据分析接口异常");
        }
    }


    @Operation(summary = "执行前置规则处理")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/pre_process")
//    Result<?> process(@RequestBody List<AnalysisDataModel> data) throws Exception {
    Result<?> pre_process() throws Exception {
        try {
            return Result.OK(analysisCoreService.preRulesProcess());
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("数据分析接口异常");
        }
    }

    @Operation(summary = "执行后置规则处理")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/post_process")
//    Result<?> process(@RequestBody List<AnalysisDataModel> data) throws Exception {
    Result<?> post_process() throws Exception {
        try {
            return Result.OK(analysisCoreService.postRulesProcess());
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("数据分析接口异常");
        }
    }

    @Operation(summary = "执行后置规则处理")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/call_model")
//    Result<?> process(@RequestBody List<AnalysisDataModel> data) throws Exception {
    Result<?> call_model() throws Exception {
        try {
            return Result.OK(analysisCoreService.callModel());
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("数据分析接口异常");
        }
    }

    @Operation(summary = "开启验证数据流程")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/validateFlow")
    Result<?> validateFlow(@RequestBody AysValidDataModel param) throws Exception {
        try {
            return Result.OK(analysisCoreService.valid(param));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("开启验证数据流程异常");
        }
    }


    @Operation(summary = "验证数据时间范围等信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/validDataCondition")
//    Result<?> process(@RequestBody List<AnalysisDataModel> data) throws Exception {
    Result<?> validDataCondition(@RequestBody AysValidDataModel param) throws Exception {
        try {
            return Result.OK(resltValidDataService.validDataCondition(param));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("验证数据时间范围等信息异常");
        }
    }

    @Operation(summary = "规则验证数据结果")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/validResult")
    Result<?> validResult(@RequestBody @Validated ValidDataModel validDataModel) throws Exception {
        Assert.isTrue(ObjectUtil.isNotNull(validDataModel), "param cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(validDataModel.getWorkId()), "getWorkId cannot be empty");
        try {
            return Result.OK(analysisCoreService.getValidateList(validDataModel));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("规则验证数据结果异常");
        }
    }

    @Operation(summary = "除非清理历史数据操作")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/removeHistoryData")
    Result<?> removeHistoryData() throws Exception {
        try {
            return Result.OK(dataBackupStrategyService.removeHistoryData());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("规则验证数据结果异常");
        }
    }

    @Operation(summary = "原始数据分批导入功能")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/batchPushData")
    Result<?> batchPushData(@RequestBody AiBatchPushModel param) throws Exception {
        try {
            Assert.isTrue(StrUtil.isNotBlank(param.getClientId()), "getClientId cannot be empty");
            Assert.isTrue(ObjectUtil.isNotNull(param.getModelType()), "getModelType cannot be empty");
            String workId = "";
            Integer modelType = Integer.valueOf(param.getModelType());
            if (modelType.equals(ModelTypeEnum.AI_ONLINE.getType()) || modelType.equals(ModelTypeEnum.AI_OFFLINE.getType())) {
                workId = analysisCoreService.batchPushData(param.getClientId(), param.getRequestId(), "", param);
            }
            if (modelType.equals(ModelTypeEnum.CLUSTERING_LLM.getType())) {
                workId = analysisCoreService.batchPushData(param.getClientId(), param.getRequestId(), "api_v1", param);
            }
            final String requestId = param.getRequestId();
            final String clientId = param.getClientId();

            JSONObject obj = JSONUtil.createObj();
            obj.putOnce("workId", workId);
            obj.putOnce("requestId", requestId);
            obj.putOnce("clientId", clientId);
            logger.info("result: {}", JSONUtil.toJsonStr(obj));
            return Result.OK(obj);
        } catch (Exception e) {
            logger.error("洞察引擎导入数据异常", e);
            return Result.error("规则验证数据结果异常");
        }
    }

    @Autowired
    MetaDataStatusBatchUpdateJob job;

    @Operation(summary = "除非清理历史数据操作")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/testMq")
    Result<?> testModelMq() {
        try {
            job.execute();
            return Result.OK();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("规则验证数据结果异常");
        }
    }


    @Operation(summary = "开启所有MQ处理")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/start")
    Result<?> start(@RequestBody Set<String> topics) {
        try {
            return Result.OK(analysisCoreService.resumeAction(topics));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("规则验证数据结果异常");
        }
    }

    @Operation(summary = "暂停所有MQ处理")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/stop")
    Result<?> stop(@RequestBody Set<String> topics) {
        try {
            return Result.OK(analysisCoreService.pauseAction(topics));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("规则验证数据结果异常");
        }
    }


    @Operation(summary = "removeCache")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/removeCache")
    Result<?> removeCache(@RequestParam(required = false) String cacheName) {
        try {
            /*if(ObjectUtil.isNotEmpty(cacheName)){
                if("rule".equals(cacheName)){
                    ruleDataServcie.removeCache();
                }else if("resourceGroup".equals(cacheName)){
                    staticDataServcie.removeCache();
                }

            }else{
                staticDataServcie.removeCache();
                ruleDataServcie.removeCache();
            }*/

            return Result.OK();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("删除缓存异常");
        }
    }

}
