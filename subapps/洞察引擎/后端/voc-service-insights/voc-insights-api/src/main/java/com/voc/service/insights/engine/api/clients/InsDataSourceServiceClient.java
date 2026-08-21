package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.ProjectInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/27 下午1:41
 * @描述:
 **/
@FeignClient(name = "service.ins.dataSource", url = "${service.ins.v1}/insDataSource", configuration = InsDataServiceClientConfig.class)
public interface InsDataSourceServiceClient {

    @PostMapping("/findAllWorkId")
    Result<Set<String>> findAllWorkId(@RequestBody InsDataSourceModel insDataSourceModel);
}
