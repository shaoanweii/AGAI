package com.voc.service.security.web;

import com.voc.service.common.response.Result;
import com.voc.service.security.api.ILoginHistoryService;
import com.voc.service.security.model.LoginHistroyModel;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Tag(name = "系统登陆日志服务")
public class LoginHistoryController {
    private static final Logger log = LoggerFactory.getLogger(LoginHistoryController.class);
    @Autowired
    ILoginHistoryService loginHistoryService;

    @PostMapping(value = "/saveLoginLogs")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    Result<?> saveLoginLogs(@RequestBody LoginHistroyModel model) {
        try {
            loginHistoryService.add(model);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("LoginHistroyModel {}", model);
        }

        return Result.OK("OK");
    }

    @PostMapping(value = "/abc")
    @Parameter(name="Authorization", in = ParameterIn.HEADER,  required= true, description = "Bearer [token]")
    @ResponseBody
    Result<?> abc() {
        return Result.OK("OK");
    }
}
