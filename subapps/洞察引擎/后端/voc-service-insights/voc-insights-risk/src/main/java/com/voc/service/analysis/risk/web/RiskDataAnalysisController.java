package com.voc.service.analysis.risk.web;

import com.voc.service.analysis.api.IRiskDataService;
import com.voc.service.analysis.model.AllTypesRiskDataModel;
import com.voc.service.analysis.model.RiskDataParamModel;
import com.voc.service.common.response.Result;
import com.voc.service.logs.annotation.AutoLog;
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

import java.util.ArrayList;
import java.util.List;


@Tag(name = "风险预警相关接口")
@RestController
@RequestMapping("/")
public class RiskDataAnalysisController {


    private static final Logger log = LoggerFactory.getLogger(RiskDataAnalysisController.class);
    @Autowired
    IRiskDataService iRiskDataService;

    @Operation(summary = "风险列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/getRiskResultList")
    Result<?> getProjectRawDataResult(@RequestBody @Validated RiskDataParamModel paramModel) {
        try {
            return Result.OK(iRiskDataService.getRiskResultList(paramModel.getClientId(), paramModel));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("风险列表数据异常");
        }
    }


    @AutoLog(value = "风险列表导出")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "风险列表导出")
    @PostMapping("/exportRiskResultList")
    Result<?> exportRiskResultList(@RequestBody @Validated RiskDataParamModel paramModel) {
        try {
            List<AllTypesRiskDataModel> allTypesRiskDataModels = iRiskDataService.exportRiskResultList(paramModel.getClientId(), paramModel);
            return Result.OK(allTypesRiskDataModels);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.OK(new ArrayList<>());
        }
    }

}
