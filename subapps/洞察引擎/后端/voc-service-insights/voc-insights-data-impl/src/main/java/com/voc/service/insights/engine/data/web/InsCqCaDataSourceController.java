package com.voc.service.insights.engine.data.web;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagLibClientService;
import com.voc.service.insights.engine.api.data.IInsCqCaDataSourceService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.AysCqCaMetaDataAnalysisVo;
import com.voc.service.insights.engine.vo.BaseCarSeriesDataVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag(name = "长安原始结果数据查询", description = "长安原始结果数据查询")
@RequestMapping("/insCqCaDataSource")
public class InsCqCaDataSourceController extends AbstractConditionFilters {
    private static final Logger log = LoggerFactory.getLogger(InsCqCaDataSourceController.class);
    /**
     * 服务对象
     */
    @Resource
    private IInsCqCaDataSourceService iInsCqCaDataSourceService;
    @Autowired
    private IInsTagLibClientService insTagLibClientService;

    @Override
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    @Operation(summary = "查询条件")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, DROPDOWN_FILTER, CLOSED_RULE_LEVEL, CONTENT_TYPE, ORIGINAL_DATA_STATUS, RESULT_DATA_STATUS, EMOTION, INTENTION, IS_HIGH_QUALITY, BRAND_CAR)));
    }


    @AutoLog(value = "数据源-获取原始数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取原始数据")
    @PostMapping("/getRawData")
    public Result<?> getRawData(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        try {
            PageInfo rawData = iInsCqCaDataSourceService.getRawData(InsCqCaDataQueryModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            log.error("获取原始数据异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-获取原始数据明细")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取原始数据明细")
    @PostMapping("/getRawDataDetail")
    public Result<IPage<AysCqCaMetaDataAnalysisVo>> getRawDataDetail(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        try {
            IPage<AysCqCaMetaDataAnalysisVo> rawData = iInsCqCaDataSourceService.getRawDataDetail(InsCqCaDataQueryModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            log.error("获取原始数据明细异常:", e);
            return Result.errors(CommonErrorEnum.UNKNOW_EXECPTION.getMessage());
        }
    }

    @AutoLog(value = "数据源-获取结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取结果数据")
    @PostMapping("/getResultData")
    public Result<?> getResultData(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        try {
            PageInfo rawData = iInsCqCaDataSourceService.getResultData(InsCqCaDataQueryModel);
            return Result.OK(rawData);
        } catch (Exception e) {
            log.error("获取结果数据异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "数据源-获取车系列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取车系列表")
    @PostMapping("/queryCarSeriesList")
    public Result<?> queryCarSeriesList(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        try {
            List<BaseCarSeriesDataVo> baseCarSeriesDataVoList = iInsCqCaDataSourceService.queryCarSeriesList(InsCqCaDataQueryModel);
            return Result.OK(baseCarSeriesDataVoList);
        } catch (Exception e) {
            log.error("获取车系列表:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-获取情感分析结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取情感分析结果数据")
    @PostMapping("/getSentimentResultData")
    @ApiResponse(responseCode = "200", description = "查询成功")
    public Result<?> getSentimentResultData(@RequestBody InsCqCaDataQueryModel insCqCaDataQueryModel) {
        try {
//            PageInfo sentimentResultData = iInsCqCaDataSourceService.getSentimentResultData(insCqCaDataQueryModel);
//            return Result.OK(sentimentResultData);
            return Result.OK();
        } catch (Exception e) {
            log.error("获取情感分析结果数据异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "数据源-获取品牌列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取品牌列表")
    @PostMapping("/queryBrandList")
    public Result<?> queryBrandList(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        try {
            List<BaseCarSeriesDataVo> baseCarSeriesDataVoList = iInsCqCaDataSourceService.queryBrandList(InsCqCaDataQueryModel);
            return Result.OK(baseCarSeriesDataVoList);
        } catch (Exception e) {
            log.error("获取车系列表:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }

    @AutoLog(value = "数据源-获取观点列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "获取观点列表")
    @PostMapping("/findAllFinalTagLibClientVoList")
    public Result<?> findAllFinalTagLibClientVoList(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel) {
        try {
            List<BaseCarSeriesDataVo> baseCarSeriesDataVoList = iInsCqCaDataSourceService.findAllFinalTagLibClientVoList(InsCqCaDataQueryModel);
            return Result.OK(baseCarSeriesDataVoList);
        } catch (Exception e) {
            log.error("获取结果数据异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "数据源-导出原始数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出原始数据")
    @PostMapping("/exportRawData")
    public Result<?> exportRawData(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel, HttpServletResponse response) {
        try {
            Boolean b = iInsCqCaDataSourceService.exportRawData(InsCqCaDataQueryModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @AutoLog(value = "数据源-导出结果数据")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "导出结果数据")
    @PostMapping("/exportRawDataResult")
    public Result<?> exportRawDataResult(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel, HttpServletResponse response) {
        try {
            Boolean b = iInsCqCaDataSourceService.exportRawDataResult(InsCqCaDataQueryModel, response);
            return Result.OK(b);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Result.error(e.getMessage());
        }
    }

}

