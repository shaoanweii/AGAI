package com.voc.service.insights.engine.web;

import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsRiskKeywordsService;
import com.voc.service.insights.engine.common.filters.AbstractConditionFilters;
import com.voc.service.insights.engine.model.AddRiskKeywordsModel;
import com.voc.service.insights.engine.model.InsRiskKeywordsModel;
import com.voc.service.insights.engine.model.InsRiskKeywordsQueryModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liuhb
 * @version 1.0.0
 * @ClassName InsRiskKeywordsController
 * @Description
 * @createTime 2024年5月16日 10:18
 */
@RestController
@Tag(name = "风险关键词", description = "风险关键词")
@RequestMapping("/keywords")
public class InsRiskKeywordsController extends AbstractConditionFilters {

    private static final Logger log = LoggerFactory.getLogger(InsRiskKeywordsController.class);
    @Resource
    private IInsRiskKeywordsService iInsRiskKeywordsService;

    @AutoLog(value = "风险关键词-分页查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询")
    @PostMapping("/queryRisKeywordsList")
    public Result<?> queryRisKeywordsList(@RequestBody InsRiskKeywordsQueryModel insRiskKeywordsQueryModel) {
        try {
            PageInfo wordsList = iInsRiskKeywordsService.queryRisKeywordsList(insRiskKeywordsQueryModel);
            return Result.OK(wordsList);
        } catch (Exception e) {
            log.error("风险关键词-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "风险关键词-新增信息")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "新增信息")
    @PostMapping("/addRisKeywords")
    public Result<?> addRisKeywords(@RequestBody @Validated AddRiskKeywordsModel riskKeywordsModel) {
        try {
            Boolean aBoolean = iInsRiskKeywordsService.addRisKeywords(riskKeywordsModel);
            return Result.OK(aBoolean);
        } catch (Exception e) {
            log.error("风险关键词-新增信息:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @AutoLog(value = "风险关键词-预警配置查询")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "预警配置查询")
    @PostMapping("/queryRiskList")
    public Result<?> queryRiskList(@RequestBody InsRiskKeywordsQueryModel insRiskKeywordsQueryModel) {
        try {
            List<InsRiskKeywordsModel> insRiskKeywordsModels = iInsRiskKeywordsService.queryRiskList(insRiskKeywordsQueryModel);
            return Result.OK(insRiskKeywordsModels);
        } catch (Exception e) {
            log.error("风险关键词-分页查询异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


    @Override
    @Operation(summary = "过滤条件")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @GetMapping("/conditions")
    public Result<?> conditions() {
        return Result.OK(async(CollUtil.set(false, CATEGORY_TYPE, SERIOUSNESS, INCREASE_TYPE, ENABLE_STATUS, STOP_OR_ENABLE)));
    }
}
