package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsBrandInfoModel;
import com.voc.service.insights.engine.vo.InsALlBrandAndCarSeriesVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/27 下午1:41
 * @描述:
 **/
@FeignClient(name = "service.ins.brand", url = "${service.ins.v1}/brandInfo", configuration = InsDataServiceClientConfig.class)
public interface InsBrandServiceClient {

    @GetMapping(value = "/findAllBrandAndCarSeries")
    Result<List<InsALlBrandAndCarSeriesVo> > findAllBrandAndCarSeries();

    @GetMapping(value = "/findAll")
    Result<List<InsBrandInfoModel> > findAll();

    @GetMapping(value = "/getSelfBrandCodes")
    Result<String> getSelfBrandCodes();
}
