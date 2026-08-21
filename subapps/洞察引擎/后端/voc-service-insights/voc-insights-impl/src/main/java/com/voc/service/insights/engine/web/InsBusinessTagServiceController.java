package com.voc.service.insights.engine.web;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsTagInfoService;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName InsBusinessTagServiceController
 * @Description
 * @createTime 2023年12月22日 19:18
 * @Copyright futong
 */

@Tag(name = "业务标签数据服务")

@RestController
@RequestMapping("/businessTag")
public class InsBusinessTagServiceController {
    //    @Autowired
    @Autowired
    IInsTagInfoService tagInfoService;

    @PostMapping(value = "/findAll")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    Result<?> findAll() {
        return Result.OK(tagInfoService.findTageInfoByType(InsightsConstants.BUSINESS_TAG_TYPE));
    }
}
