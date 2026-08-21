package com.voc.service.data.integration.web;

import cn.hutool.core.util.NumberUtil;
import com.voc.service.common.response.Result;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.IDataBackupStrategyService;
import com.voc.service.data.integration.api.model.DataRequestModel;
import com.voc.service.data.integration.api.vo.DataValidateResultVo;
import com.voc.service.data.integration.mpp.scheduled.xxljob.XxlJobServiceImpl;
import com.voc.service.data.integration.services.CleanHistoryDataServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 09:54
 * @描述:
 **/
@Tag(name = "数据接收服务")
@RestController
@RequestMapping("/")
public class DatIntegrationController {
    @Autowired
    XxlJobServiceImpl service;
    @Autowired
    ChannelExecutionResultService channelExecutionResultService;

    @Autowired
    IDataBackupStrategyService backupStrategyService;
    @Autowired
    CleanHistoryDataServiceImpl cleanHistoryDataService;
    @Autowired
    XxlJobServiceImpl  xxlJobService;

    //    @AutoLog(value = "基础信息-获取能源信息")
    @Operation(summary = "test")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = false, description = "Bearer [token]")
    @PostMapping("/test")
    Result<?> push(@RequestBody String param) throws Exception {
        xxlJobService.inputChannelMeateData(null);
        return Result.OK();
    }

    @Operation(summary = "findVerificationResult")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findVerificationResult")
    Result<DataValidateResultVo> findVerificationResult(@RequestBody DataRequestModel dataRequestModel) throws Exception {
        DataValidateResultVo result = channelExecutionResultService.findVerificationResult(dataRequestModel);
        return Result.OK(result);
    }

    /*@Operation(summary = "findVerificationResultByCondition")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/findVerificationResultByCondition")
    Result<List<DataValidateResultVo>> findVerificationResultByCondition(@RequestBody DataRequestModel dataRequestModel) throws Exception {
        List<DataValidateResultVo> result = channelExecutionResultService.findVerificationResultByCondition(dataRequestModel);
        return Result.OK(result);
    }*/


    @Operation(summary = "removeHistoryData")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/removeHistoryData")
    Result<?> removeHistoryData() throws Exception {
        backupStrategyService.removeHistoryData();
        return Result.OK();
    }
}
