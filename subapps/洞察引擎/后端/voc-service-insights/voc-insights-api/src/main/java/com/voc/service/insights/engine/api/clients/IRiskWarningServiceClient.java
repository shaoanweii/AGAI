package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @创建人 fanrong
 * @创建时间 2024/10/9 15:18
 * @描述：
 **/
@FeignClient(name = "service.risk", url = "${service.risk.v1}")
//@FeignClient(name = "service.risk", url = "http://192.168.7.37:8080")
public interface IRiskWarningServiceClient {

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/26 上午10:00
     * @描述   获取风险预警数据
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/getRiskResultList")
    Result<?> findRiskWarningData(InsDataSourceModel insDataSourceModel);

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/26 上午10:29
     * @描述   获取需导出的风险预警数据
     * @param insDataSourceModel
     * @return com.voc.service.common.response.Result<?>
     **/
    @PostMapping("/exportRiskResultList")
    Result<?> exportRiskWarningData(InsDataSourceModel insDataSourceModel);
}
