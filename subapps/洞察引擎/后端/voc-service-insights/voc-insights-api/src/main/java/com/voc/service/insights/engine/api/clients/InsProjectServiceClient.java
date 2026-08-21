package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsProjectInfoModel;
import com.voc.service.insights.engine.vo.BrandVo;
import com.voc.service.insights.engine.vo.NewCarSeriesConditionVo;
import com.voc.service.insights.engine.vo.ProjectInfoVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/27 下午1:41
 * @描述:
 **/
@FeignClient(name = "service.ins.project", url = "${service.ins.v1}/insProjectInfo", configuration = InsDataServiceClientConfig.class)
public interface InsProjectServiceClient {

    @PostMapping("/findRiskWarningInfo")
    Result<List<ProjectInfoVo>> findRiskWarningInfo(@RequestBody InsProjectInfoModel projectInfoModel);

    @PostMapping("/findBrandTabLabelByProjectId")
    Result<List<BrandVo>> findBrandTabLabel(@RequestBody InsProjectInfoModel projectInfoModel);

    @PostMapping("/findBrandInfo")
    Result<List<BrandVo>> findBrandInfo(@RequestBody InsProjectInfoModel projectInfoModel);


    @PostMapping("/allLibClientCategoryTree")
    Result<List<TagLibCategoryVo>> allLibClientCategoryTree(@RequestParam(value = "clientId") String clientId);

    @GetMapping("/getNewCarSeriesCondition")
    Result<NewCarSeriesConditionVo> getNewCarSeriesCondition();
}
