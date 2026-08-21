package com.voc.service.analysis.risk.web;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.model.WarningTaskRunModel;
import com.voc.service.risk.api.IBatchRiskDataAnalysisService;
import com.voc.service.risk.api.model.BatchWarningTaskRunModel;
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


@Tag(name = "批量预警数据")
@RestController
@RequestMapping("/")
public class BatchRiskDataAnalysisController {


    private static final Logger log = LoggerFactory.getLogger(BatchRiskDataAnalysisController.class);

    @Resource
    IBatchRiskDataAnalysisService iBatchRiskDataAnalysisService;


    @Operation(summary = "批量触发任务预警")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/batchWarningTaskRun")
    @XxlJob("batchWarningTaskRun")
    Result<?> batchWarningTaskRun(@RequestBody BatchWarningTaskRunModel param) {
        try {
            log.info("批量闭环初始化任务参数:{}", param);
            String ruleId;
            if (ObjectUtils.isNotEmpty(param)) {
                ruleId = param.getRuleId();
                param.setRuleId(ruleId);
            } else {
                ruleId = XxlJobHelper.getJobParam();
                BatchWarningTaskRunModel model = new BatchWarningTaskRunModel();
                model.setRuleId(ruleId);
                param = model;
                log.info("job任务参数:{}", ruleId);
            }
            log.info("任务参数:{}", param);
            Boolean warningTaskRun = iBatchRiskDataAnalysisService.batchWarningTaskRun(param);
            return Result.OK(warningTaskRun);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("触发任务预警");
        }
    }

    @Operation(summary = "根据规则创建job")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/createJob")
    @XxlJob("createJob")
    Result<?> createJob() {
        try {
            Boolean job = iBatchRiskDataAnalysisService.createJob();
            return Result.OK(job);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("创建job");
        }
    }

}
