package com.voc.service.logs.web;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.logs.api.IBizLogService;
import com.voc.service.logs.model.OpsLogModel;
import com.voc.service.logs.model.UserMenuVisitRecordModel;
import com.voc.service.logs.util.IpUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.kafka.common.protocol.types.Struct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName VocBusinessTagServiceController
 * @Description
 * @createTime 2023年12月22日 19:18
 * @Copyright futong
 */

@Tag(name = "日志处理服务")

@RestController
@RequestMapping("/")
public class VocBizlogsServiceController {
    @Autowired
    IBizLogService iBizLogService;

    @PostMapping(value = "/pushBizLogsMsg")
    Result<?> pushBizLogsMsg(@RequestBody OpsLogModel messageDTO) {
        iBizLogService.pushBizLogsMsg(messageDTO);
        return Result.OK("OK");
    }

    @PostMapping(value = "/findBizLogsMsg")
    Result<PageInfo> findBizLogsMsg(@RequestBody OpsLogModel messageDTO) {
        PageInfo opsLogModel = iBizLogService.findBizLogsMsg(messageDTO);
        return Result.OK(opsLogModel);
    }

    @PostMapping(value = "/findAllBizLogsMsg")
    Result<List<OpsLogModel>> findAllBizLogsMsg(@RequestBody OpsLogModel messageDTO) {
        List<OpsLogModel> opsLogModels = iBizLogService.findAllBizLogsMsg(messageDTO);
        return Result.OK(opsLogModels);
    }

    @PostMapping(value = "/pushMenuVisitRecord")
    Result<?> pushMenuVisitRecord(@RequestBody UserMenuVisitRecordModel model, HttpServletRequest request) {
        model.setIpAddr(IpUtil.getIpAddr(request));
        model.setId(IdWorker.getId());
        // 解析 User-Agent 判断请求来源
        String userAgent = request.getHeader("user-agent");
        String clientType = parseClientType(userAgent);
        model.setCreateTime(LocalDateTime.now());
        model.setVisitTime(LocalDateTime.now());
        model.setVisitDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        // 设置用户id
        model.setUserId(ServiceContextHolder.getUser().getUserId());
        model.setUserCode(ServiceContextHolder.getUser().getUsername());
        model.setUserName(ServiceContextHolder.getUser().getFirstname());

        // 设置访问来源类型
        if (StrUtil.isBlankIfStr(model.getAppType())){
            model.setAppType(clientType);
        }
        iBizLogService.pushMenuVisitRecord(model);
        return Result.OK("OK");
    }
    private String parseClientType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "UNKNOWN";
        }
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone") || userAgent.contains("ipad")) {
            return "APP";
        }
        return "PC";
    }


}
