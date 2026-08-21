package com.voc.service.insights.engine.web;

import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsCustomerInfoService;
import com.voc.service.insights.engine.vo.CustomerInfoVo;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/21 17:57
 * @描述:
 **/
@Tag(name = "客户管理服务")
@RestController
@RequestMapping("/model")
public class InsCustomerModelApiController {

    private static final Logger log = LoggerFactory.getLogger(InsCustomerModelApiController.class);
    @Autowired
    IInsCustomerInfoService customerInfoService;


    @AutoLog(value = "客户管理服务-分页查询客户列表")
    @Parameter(name = "Authorization", in = ParameterIn.HEADER, required = true, description = "Bearer [token]")
    @Operation(summary = "分页查询客户列表")
    @PostMapping("/findModelCustomerList")
    Result<?> findCustomerList() {
        try {
            List<CustomerInfoVo> customerListModel = customerInfoService.findCustomerListModel();
            return Result.OK(customerListModel);
        } catch (Exception e) {
            log.error("提供model查询客户服务-分页查询客户列表异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


}
