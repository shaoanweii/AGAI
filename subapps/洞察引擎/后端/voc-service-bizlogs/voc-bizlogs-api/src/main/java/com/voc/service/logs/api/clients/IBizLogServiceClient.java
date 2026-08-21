package com.voc.service.logs.api.clients;

import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.logs.model.OpsLogModel;
import com.voc.service.logs.model.UserMenuVisitRecordModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


//@FeignClient(name = "service.logs", url = "http://localhost:8080")
@FeignClient(name = "service.logs", url = "${service.logs.v1}")
public interface IBizLogServiceClient {


    @PostMapping("/pushBizLogsMsg")
    Result<?> pushBizLogsMsg(@RequestBody OpsLogModel model);

    @PostMapping(value = "/findBizLogsMsg")
    Result<PageInfo> findBizLogsMsg(@RequestBody OpsLogModel messageDTO);

    @PostMapping(value = "/findAllBizLogsMsg")
    Result<List<OpsLogModel>> findAllBizLogsMsg(@RequestBody OpsLogModel messageDTO);
    @PostMapping("/pushMenuVisitRecord")
    Result<?> pushMenuVisitRecord(@RequestBody UserMenuVisitRecordModel model);

    /*
//    @PostMapping("/pushLoginLogsMsg")
//    Result<?> pushLoginLogsMsg(@RequestBody MessageDTO messageDTO);
    @PostMapping("/pushLoginLogsMsg")
    Result<?> pushLoginLogsMsg(@RequestBody List<MessageDTO> messageDTO);*/
}
