package com.voc.service.analysis.risk.web;

import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IEmotionRiskStatisticsService;
import com.voc.service.analysis.api.IQualityRiskStatisticsService;
import com.voc.service.analysis.api.IUserRiskStatisticsService;
import com.voc.service.analysis.model.RiskStatisticModel;
import com.voc.service.analysis.risk.component.ExtractTag;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.enums.LabelTypeEnum;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Tag(name = "风险预警相关接口")
@RestController
@RequestMapping("/")
public class RiskStatisticsTaskController {


    private static final Logger log = LoggerFactory.getLogger(RiskStatisticsTaskController.class);
    @Resource
    IEmotionRiskStatisticsService iEmotionRiskStatisticsService;

    @Resource
    IQualityRiskStatisticsService iQualityRiskStatisticsService;

    @Resource
    IUserRiskStatisticsService iUserRiskStatisticsService;

    @Resource
    ExtractTag extractTag;


    @XxlJob("riskStatistics")
    Result<?> riskStatistics() {
        try {
            String param = XxlJobHelper.getJobParam();
            RiskStatisticModel paramModel = initParam(param);
            extractTag.completeTime(paramModel);
            log.info("riskStatistics风险统计数据:{}", paramModel);
            iEmotionRiskStatisticsService.emotionRiskStatistics(paramModel.getClientId(), paramModel, LabelTypeEnum.PROD.getCode());
            iEmotionRiskStatisticsService.emotionRiskStatistics(paramModel.getClientId(), paramModel,LabelTypeEnum.SERVICE.getCode());
            iQualityRiskStatisticsService.qualityRiskStatistics(paramModel.getClientId(), paramModel,LabelTypeEnum.QY.getCode());
            iUserRiskStatisticsService.userRiskStatistics(paramModel.getClientId(), paramModel);
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("业务风险统计数据异常");
        }
    }


    private RiskStatisticModel initParam(String param) {
        List<String> paramsList = StrUtil.split(param, StrUtil.COMMA);
        Map<String, String> argsMap = paramsList.stream().collect(Collectors.toMap(r -> String.valueOf(StrUtil.split(r, StrUtil.AT).get(0)),
                r -> String.valueOf(StrUtil.split(r, StrUtil.AT).get(1))));

        RiskStatisticModel riskStatisticModel = new RiskStatisticModel();
        argsMap.forEach((k, v) -> extractTag.initParam(riskStatisticModel, k, v));

        return riskStatisticModel;
    }

    @Operation(summary = "产品风险统计")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/prodRiskStatistics")
    @XxlJob("prodRiskStatistics")
    Result<?> prodRiskStatistics(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
                extractTag.completeTime(paramModel);
            }
            iEmotionRiskStatisticsService.emotionRiskStatistics(paramModel.getClientId(), paramModel,LabelTypeEnum.PROD.getCode());
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("服务风险统计数据异常");
        }
    }


    @Operation(summary = "服务风险统计")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/serviceRiskStatistics")
    @XxlJob("serviceRiskStatistics")
    Result<?> serviceRiskStatistics(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
                extractTag.completeTime(paramModel);
            }
            iEmotionRiskStatisticsService.emotionRiskStatistics(paramModel.getClientId(), paramModel,LabelTypeEnum.SERVICE.getCode());
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("服务风险统计数据异常");
        }
    }


    @Operation(summary = "质量风险统计")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/qualityRiskStatistics")
    @XxlJob("qualityRiskStatistics")
    Result<?> qualityRiskStatistics(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
                extractTag.completeTime(paramModel);
            }
            iQualityRiskStatisticsService.qualityRiskStatistics(paramModel.getClientId(), paramModel,LabelTypeEnum.QY.getCode());
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
        }
    }


    @Operation(summary = "用户风险统计")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/userRiskStatistics")
    @XxlJob("userRiskStatistics")
    Result<?> userRiskStatistics(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
                extractTag.completeTime(paramModel);
            }
            iUserRiskStatisticsService.userRiskStatistics(paramModel.getClientId(), paramModel);
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
        }
    }

}
