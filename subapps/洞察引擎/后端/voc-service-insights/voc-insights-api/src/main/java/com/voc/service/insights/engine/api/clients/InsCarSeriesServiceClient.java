package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsCarSeriesInfoModel;
import com.voc.service.insights.engine.vo.CarInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/17 下午4:21
 * @描述:
 **/
//@FeignClient(name = "service.ins.carSeriesInfo", url = "http://localhost:8088/carSeriesInfo", configuration = InsDataServiceClientConfig.class)
@FeignClient(name = "service.ins.carSeriesInfo", url = "${service.ins.v1}/carSeriesInfo", configuration = InsDataServiceClientConfig.class)
public interface InsCarSeriesServiceClient {

    @PostMapping(value = "/findCarSeriesByIds")
    Result<List<CarInfoVo>> findCarSeriesByIds(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel);


    @PostMapping(value = "/findByParam")
    Result<List<InsCarSeriesInfoModel>> findByParam(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel);


    @PostMapping(value = "/findAll")
    Result<List<InsCarSeriesInfoModel>> findAll(@RequestBody InsCarSeriesInfoModel insCarSeriesInfoModel);
}
