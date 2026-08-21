package com.voc.service.analysis.clients;

import com.voc.service.analysis.model.DateSourceModel;
import com.voc.service.common.response.Result;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


@FeignClient(name = "service.data", url = "${service.ins.v1}")
public interface IDataSourceServiceClient {

    @Schema(description = "通知数据源状态")
    @PostMapping("/insDataSource/pushResultData")
    Result notificationStatus(List<DateSourceModel> dateSourceModelList);
}
