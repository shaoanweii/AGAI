package com.voc.service.analysis.risk.web;

import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IEmotionRiskWarningService;
import com.voc.service.analysis.api.IQualityRiskWarningService;
import com.voc.service.analysis.api.IUserRiskWarningService;
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
public class RiskWarningTaskController {


    private static final Logger log = LoggerFactory.getLogger(RiskWarningTaskController.class);
    @Resource
    IEmotionRiskWarningService iEmotionRiskWarningService;

    @Resource
    IQualityRiskWarningService iQualityRiskWarningService;


    @Resource
    IUserRiskWarningService iUserRiskWarningService;

    @Resource
    ExtractTag extractTag;


    @XxlJob("riskWarning")
    Result<?> riskWarning() {
        try {
            String param = XxlJobHelper.getJobParam();
            RiskStatisticModel paramModel = initParam(param);
            log.info("riskStatistics风险预警数据:{}", paramModel);
            iEmotionRiskWarningService.riskEmotionFilter(paramModel, LabelTypeEnum.PROD.getCode());
            iEmotionRiskWarningService.riskEmotionFilter(paramModel, LabelTypeEnum.SERVICE.getCode());
            iQualityRiskWarningService.riskQualityFilter(paramModel, LabelTypeEnum.QY.getCode());
            iUserRiskWarningService.riskUserFilter(paramModel);
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
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


    @Operation(summary = "产品风险预警")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/riskEmotionFilter")
    @XxlJob("riskEmotionFilter")
    Result<?> riskEmotionFilter(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
            }
            iEmotionRiskWarningService.riskEmotionFilter(paramModel,
                    LabelTypeEnum.PROD.getCode());
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
        }
    }

    @Operation(summary = "服务风险预警")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/riskServiceFilter")
    @XxlJob("riskServiceFilter")
    Result<?> riskServiceFilter(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
            }
            iEmotionRiskWarningService.riskEmotionFilter(paramModel, LabelTypeEnum.SERVICE.getCode());
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
        }
    }


    @Operation(summary = "质量风险预警")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/riskQualityFilter")
    @XxlJob("riskQualityFilter")
    Result<?> riskQualityFilter(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
            }
            iQualityRiskWarningService.riskQualityFilter(paramModel, LabelTypeEnum.QY.getCode());
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
        }
    }


    @Operation(summary = "用户风险预警")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/riskUserFilter")
    @XxlJob("riskUserFilter")
    Result<?> riskUserFilter(@RequestBody RiskStatisticModel paramModel) {
        try {
            if (ObjectUtils.isEmpty(paramModel)) {
                String param = XxlJobHelper.getJobParam();
                paramModel = initParam(param);
            }
            iUserRiskWarningService.riskUserFilter(paramModel);
            return Result.OK(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
        }
    }

}
