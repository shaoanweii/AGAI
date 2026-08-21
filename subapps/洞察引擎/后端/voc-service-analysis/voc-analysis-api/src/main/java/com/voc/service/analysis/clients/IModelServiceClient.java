package com.voc.service.analysis.clients;

import com.voc.service.analysis.model.AiRequestDataModel;
import com.voc.service.analysis.model.AiResultDataModel;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName IModelServiceClient
 * @createTime 2024年03月08日 14:55
 * @Copyright cuick
 * @Description 算法模型接口
 */

//@FeignClient(name = "service.model", url = "http://localhost:8088",configuration = ModelServiceClientConfig.class)
@FeignClient(name = "service.model", url = "${service.model.v1}",configuration = ModelServiceClientConfig.class)
public interface IModelServiceClient {

    @Schema(description = "调用模型数据分析")
    @PostMapping("/process")
    AiResultDataModel getDataProcess(@RequestParam("taskId") String taskId,
                                     @RequestBody List<AiRequestDataModel> aiRequestDateModelList);
}
