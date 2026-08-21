package com.voc.service.analysis.core.v2.web;

import com.alibaba.fastjson.JSON;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysPostprocessDataService;
import com.voc.service.analysis.model.ProjectRawDataParamModel;
import com.voc.service.analysis.model.ProjectResultDataParamModel;
import com.voc.service.analysis.model.ResultConditionsModel;
import com.voc.service.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "项目关联数据列表接口")
@RestController
@RequestMapping("/")
public class AnalysisProjectController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisProjectController.class);
    /*@Autowired
    IAysMetaDataAnalysisService iAysMetaDataAnalysisService;

    @Autowired
    IAysPostprocessDataService iAysPostprocessDataService;

    @Operation(summary = "项目关联原文数据列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getProjectRawDataResult")
    Result<?> getProjectRawDataResult(@RequestBody @Validated ProjectRawDataParamModel paramModel) {
        try {
            log.info("项目关联原文数据列表:{}", JSON.toJSONString(paramModel));
            return Result.OK(iAysMetaDataAnalysisService.getProjectRawDataResult(paramModel.getClientId(), paramModel));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("项目关联原文数据结果异常");
        }
    }



    @Operation(summary = "项目关联结果数据列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getProjectResultDataList")
    Result<?> getProjectResultDataList(@RequestBody @Validated ProjectResultDataParamModel dataParamModel) {
        try {
            log.info("项目关联结果数据列表:{}", JSON.toJSONString(dataParamModel));
            return Result.OK(iAysPostprocessDataService.getProjectResultDataList(dataParamModel.getClientId(), dataParamModel));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("项目关联结果数据列表异常");
        }
    }


    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/projectConditions")
    public Result<?> projectConditions(@RequestBody ProjectResultDataParamModel dataParamModel) {
        ResultConditionsModel conditions = iAysPostprocessDataService.projectConditions(dataParamModel.getClientId(), dataParamModel);
        return Result.OK(conditions);
    }*/

}
