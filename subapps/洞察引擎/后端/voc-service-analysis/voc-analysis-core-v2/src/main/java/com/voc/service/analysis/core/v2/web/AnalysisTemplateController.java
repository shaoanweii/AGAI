package com.voc.service.analysis.core.v2.web;

import cn.hutool.core.util.ObjectUtil;
import com.voc.service.analysis.v2.api.IAnalysisCoreService;
import com.voc.service.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @创建者: cuick
 * @创建时间: 2024/5/9 16:54
 * @描述:
 **/
@Tag(name = "数据清洗服务（样板间）")
@RestController
@RequestMapping("/tpl")
public class AnalysisTemplateController {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisTemplateController.class);
    @Autowired
    IAnalysisCoreService analysisCoreService;

    /*@Operation(summary = "数据分析入口（样板间）")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @PostMapping("/process")
    Result<?> process(@RequestBody List<Object> param) throws Exception {
        try {
            Assert.isTrue(ObjectUtil.isNotNull(param), "param cannot be empty");
            return Result.OK(analysisCoreService.templateProcess(null,param));
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.error("数据分析接口异常");
        }
    }*/

}
