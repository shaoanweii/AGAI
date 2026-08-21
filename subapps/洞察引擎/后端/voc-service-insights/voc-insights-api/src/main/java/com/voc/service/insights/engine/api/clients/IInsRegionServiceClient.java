package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsRegionConfigModel;
import com.voc.service.insights.engine.vo.RegionConfigVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/10/17 下午3:49
 * @描述:
 **/
@FeignClient(name = "service.ins.region", url = "${service.ins.v1}/region", configuration = InsDataServiceClientConfig.class)
//@FeignClient(name = "service.ins.region", url = "http://localhost:8088/region", configuration = InsDataServiceClientConfig.class)
public interface IInsRegionServiceClient {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/10/17 下午3:49
     * @描述   根据区域分类id获取区域树
     * @param regionConfigModel
     * @return com.voc.service.common.response.Result<java.util.List<com.voc.service.insights.engine.vo.RegionConfigVo>>
     **/
    @PostMapping("/findRegionTreeByIds")
    Result<List<RegionConfigVo>> findRegionTreeByIds(@RequestBody InsRegionConfigModel regionConfigModel);


    @PostMapping("/findRegionTreeByIds1")
    Result<List<RegionConfigVo>> findRegionTreeByIds1(@RequestBody InsRegionConfigModel regionConfigModel);
}
