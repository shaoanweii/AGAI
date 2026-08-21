package com.voc.service.analysis.risk.web;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.ICqCaRiskDataAnalysisService;
import com.voc.service.insights.engine.api.model.WarningTaskRunModel;
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


@Tag(name = "创建job")
@RestController
@RequestMapping("/")
public class CqCaRiskDataAnalysisController {


    private static final Logger log = LoggerFactory.getLogger(CqCaRiskDataAnalysisController.class);

    @Resource
    ICqCaRiskDataAnalysisService iCqCaRiskDataAnalysisService;


    @Operation(summary = "根据规则创建job")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/createJob")
    @XxlJob("createJob")
    Result<?> createJob() {
        try {
            Boolean job = iCqCaRiskDataAnalysisService.createJob();
            return Result.OK(job);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("创建job");
        }
    }

    @Operation(summary = "根据规则停用job")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/delJob")
    Result<?> delJob(@RequestBody List<String> ruleIds) {
        try {
            Boolean job = iCqCaRiskDataAnalysisService.delJob(ruleIds);
            return Result.OK(job);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("停用job");
        }
    }


    @Operation(summary = "触发任务预警")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/warningTaskRun")
    @XxlJob("warningTaskRun")
    Result<?> warningTaskRun(@RequestBody WarningTaskRunModel param) {
        try {
            log.info("初始化任务参数:{}", param);
            String ruleId;
            if (ObjectUtils.isNotEmpty(param)) {
                ruleId = param.getRuleId();
                param.setRuleId(ruleId);
            } else {
                ruleId = XxlJobHelper.getJobParam();
                WarningTaskRunModel model = new WarningTaskRunModel();
                model.setRuleId(ruleId);
                param = model;
                log.info("job任务参数:{}", ruleId);
            }
            log.info("任务参数:{}", param);
            Boolean warningTaskRun = iCqCaRiskDataAnalysisService.warningTaskRun(param);
            return Result.OK(warningTaskRun);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("触发任务预警");
        }
    }

    @Operation(summary = "定时扫描舆情公关数据去重")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/publicOpinionDistinct")
    @XxlJob("publicOpinionDistinct")
    Result<?> publicOpinionDistinct(@RequestBody WarningTaskRunModel param) {
        try {
            log.info("开始定时扫描舆情公关数据去重:{}", param);
            Boolean distinct = iCqCaRiskDataAnalysisService.publicOpinionDistinct();
            return Result.OK(distinct);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("触发任务预警");
        }
    }

}
