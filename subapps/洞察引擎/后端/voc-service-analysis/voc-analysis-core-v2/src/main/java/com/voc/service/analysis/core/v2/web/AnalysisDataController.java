package com.voc.service.analysis.core.v2.web;

import com.voc.service.analysis.api.IAysBatchUpdateService;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.api.IAysPostprocessDataService;
import com.voc.service.analysis.model.*;
import com.voc.service.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "数据列表接口")
@RestController
@RequestMapping("/")
public class AnalysisDataController {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisDataController.class);

    @Autowired
    IAysMetaDataAnalysisService iAysMetaDataAnalysisService;

    @Autowired
    IAysPostprocessDataService iAysPostprocessDataService;

    @Autowired
    IAysModelResltAnalysisService iAysModelResltAnalysisService;

    @Autowired
    IAysBatchUpdateService iAysBatchUpdateService;

    @Operation(summary = "修改结果表数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/modify/resultdata")
    Result<?> modifyResultdata(@RequestBody ModifyDataModel model) {
        try {
            iAysPostprocessDataService.modifyResultdata(model);
            return Result.OK(model.getRequestId());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    @Operation(summary = "原文数据列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getRawDataResult")
    Result<?> getRawDataResult(@RequestBody @Validated RawDataParamModel rawDataListModel) {

        /*if (CollectionUtils.isNotEmpty(rawDataListModel.getWorkIdList())) {
            rawDataListModel.setShowType(1);
        } else {
            rawDataListModel.setShowType(2);
        }
        try {
            return Result.OK(iAysMetaDataAnalysisService.getRawDataResult(rawDataListModel.getClientId(), rawDataListModel));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("原文数据结果异常");
        }*/
        return Result.OK();
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出原文数据结果")
    @PostMapping("/exportRawDataResult")
    Result<?> exportOriginalTextData(@RequestBody RawDataParamModel rawDataListModel) {

        /*if (CollectionUtils.isNotEmpty(rawDataListModel.getWorkIdList())) {
            rawDataListModel.setShowType(1);
        } else {
            rawDataListModel.setShowType(2);
        }
        try {
            rawDataListModel.setDate(null);
            long start = System.currentTimeMillis();
            List<JSONObject> originDataListModels = iAysMetaDataAnalysisService.exportRawDataResult(rawDataListModel.getClientId(), rawDataListModel);
            logger.info("cost:" + (System.currentTimeMillis() - start));
            return Result.OK(originDataListModels);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.OK(new ArrayList<>());
        }*/
        return Result.OK();
    }


    @Operation(summary = "结果数据列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getResultDataList")
    Result<?> getResultData(@RequestBody @Validated ResultDataParamModel resultDataParamModel) {

        /*if (CollectionUtils.isNotEmpty(resultDataParamModel.getWorkIdList())) {
            resultDataParamModel.setShowType(1);
        } else {
            resultDataParamModel.setShowType(2);
        }
        try {
            return Result.OK(iAysPostprocessDataService.getResultDataList(resultDataParamModel.getClientId(), resultDataParamModel));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("结果数据列表异常");
        }*/
        return Result.OK();
    }

    @Operation(summary = "结果数据列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getCommonDataList")
    Result<?> getCommonDataList(@RequestBody ResultDataParamModel resultDataParamModel) {
        /*try {
            return Result.OK(iAysPostprocessDataService.getCommonDataList(resultDataParamModel.getClientId(), resultDataParamModel));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("结果数据列表异常");
        }*/
        return Result.OK();
    }


    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出结果数据列表")
    @PostMapping("/exportResultData")
    Result<?> exportResultData(@RequestBody @Validated ResultDataParamModel resultDataParamModel) {

        /*if (CollectionUtils.isNotEmpty(resultDataParamModel.getWorkIdList())) {
            resultDataParamModel.setShowType(1);
        } else {
            resultDataParamModel.setShowType(2);
        }
        try {
            List<JSONObject> resultDataListModelList = iAysPostprocessDataService.exportResultData(resultDataParamModel.getClientId(), resultDataParamModel);
            return Result.OK(resultDataListModelList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.OK(new ArrayList<>());
        }*/
        return Result.OK();
    }


    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/conditions")
    public Result<?> conditions(@RequestBody ResultConditionsParamModel paramModel) {

        if (CollectionUtils.isNotEmpty(paramModel.getWorkIdList())) {
            paramModel.setShowType(1);
        } else {
            paramModel.setShowType(2);
        }
        ResultConditionsModel conditions = iAysModelResltAnalysisService.conditions(paramModel.getClientId(), paramModel);
        return Result.OK(conditions);
    }


    @Operation(summary = "获取系统集成数据状态")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getDataResultStatus")
    public Result<?> getDataResultStatus(@RequestBody RawDataParamModel rawDataListModel) {

       /* if (CollectionUtils.isNotEmpty(rawDataListModel.getWorkIdList())) {
            rawDataListModel.setShowType(1);
        } else {
            rawDataListModel.setShowType(2);
        }
        List<DataStatusModel> dataStatusModel = iAysMetaDataAnalysisService.getDataResultStatus(rawDataListModel.getClientId(), rawDataListModel);
        return Result.OK(dataStatusModel);*/

        return Result.OK();
    }


    @Operation(summary = "获取失败数据集合")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getFailDataList")
    public Result<?> getFailDataList(@RequestBody RawDataParamModel rawDataListModel) {

        /*if (CollectionUtils.isNotEmpty(rawDataListModel.getWorkIdList())) {
            rawDataListModel.setShowType(1);
        } else {
            rawDataListModel.setShowType(2);
        }
        List<JSONObject> exportRawDataResult = iAysMetaDataAnalysisService.getFailDataList(rawDataListModel.getClientId(), rawDataListModel);
        return Result.OK(exportRawDataResult);*/
        return Result.OK();
    }


    @Operation(summary = "修改结果表数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/callModifyResultdata")
    Result<?> callModifyResultdata() {
        try {
            iAysBatchUpdateService.modifyResultdata();
            return Result.OK();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

    }

    @Operation(summary = "修改结果表数据(凌晨)")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/callModifyResultdataD")
    Result<?> callModifyResultdataD() {
        try {
            iAysBatchUpdateService.modifyResultdataD();
            return Result.OK();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

    }

    @Operation(summary = "修改结果表数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/moveResultDataToFinalTable")
    Result<?> moveResultDataToFinalTable() {
        try {
            iAysBatchUpdateService.moveResultDataToFinalTable();
            return Result.OK();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }

    }

}
