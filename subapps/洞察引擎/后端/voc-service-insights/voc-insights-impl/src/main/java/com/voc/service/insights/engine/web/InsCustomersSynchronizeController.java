package com.voc.service.insights.engine.web;

import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.IInsCustomersSynchronizeService;
import com.voc.service.insights.engine.model.InsCustomersSynchronizeModel;
import com.voc.service.logs.annotation.AutoLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@Tag(name = "创建客户建好库以后同步客户信息", description = "同步客户信息")
@RequestMapping("/sync")
public class InsCustomersSynchronizeController {


    private static final Logger log = LoggerFactory.getLogger(InsCustomersSynchronizeController.class);
    @Resource
    private IInsCustomersSynchronizeService iInsCustomersSynchronizeService;


    @AutoLog(value = "同步客户信息")
    @Operation(summary = "同步客户信息")
    @PostMapping("/syncCustomersInfo")
    //I6+SR2h5CK0Kq5GHS39c2x6w5oTScGHh091nvQD1jhDD0uD2ID6N1ixuizdQTJ3q/rgu49nCA1CA5Ff6DfNpJQ==
    public Result<?> syncCustomersInfo(@RequestBody @Validated InsCustomersSynchronizeModel model) {
        try {
            Boolean aBoolean = iInsCustomersSynchronizeService.syncCustomersInfo(model);
            return Result.OK(aBoolean);
        } catch (Exception e) {
            log.error("同步客户信息异常:", e);
            return Result.error(CommonErrorEnum.UNKNOW_EXECPTION);
        }
    }


}
