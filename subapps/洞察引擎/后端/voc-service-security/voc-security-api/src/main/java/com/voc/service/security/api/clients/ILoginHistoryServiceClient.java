package com.voc.service.security.api.clients;


import com.voc.service.common.response.Result;
import com.voc.service.security.model.LoginHistroyModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "service.auth.logs", url = "${service.auth.v1}/logs")
public interface ILoginHistoryServiceClient {

//    @PostMapping("/saveLoginLogs")
    Result<?> saveLoginLogs(@RequestBody LoginHistroyModel msg);
}
