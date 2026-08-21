package com.voc.service.analysis.clients;

import com.voc.service.analysis.model.AysValidDataModel;
import com.voc.service.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName IModelServiceClient
 * @createTime 2024年03月08日 14:55
 * @Copyright cuick
 * @Description 算法模型接口
 */

@FeignClient(name = "service.analysis", url = "${service.analysis.v1}")
public interface IAnalysiCoreServiceClient {

    /**
     * 启动验证服务
     *
     * @param param
     * @return
     */
    @PostMapping("/validateFlow")
    Result<?> validateFlow(@RequestBody AysValidDataModel param);

    /**
     * 验证结果服务
     *
     * @param param
     * @return
     */
    @PostMapping("/validResult")
    Result<?> validResult(@RequestBody AysValidDataModel param);

    /**
     * 验证数据时间范围等信息
     *
     * @param param
     * @return
     */
    @PostMapping("/validDataCondition")
//    Result<?> process(@RequestBody List<AnalysisDataModel> data) throws Exception {
    Result<?> validDataCondition(@RequestBody AysValidDataModel param);
}
