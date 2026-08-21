package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.vo.InsTagLibVo;
import com.voc.service.insights.engine.vo.TagClientVo;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import com.voc.service.insights.engine.vo.TagLibClientTreeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/27 下午1:41
 * @描述:
 **/
//@FeignClient(name = "service.ins.tagLib", url = "http://localhost:8088/insTagLibClient", configuration = InsDataServiceClientConfig.class)
@FeignClient(name = "service.ins.tagLib", url = "${service.ins.v1}/insTagLibClient", configuration = InsDataServiceClientConfig.class)
public interface InsTagLibServiceClient {

    @PostMapping("/findAllTagLibClientIds")
    Result<List<String>> findAllTagLibClientIds(@RequestBody InsTagLibClientModel tagLibClientModel);

    @PostMapping("/findUpTagLibHierarchical")
    Result<List<TagLibCategoryVo>> findUpTagLibHierarchical(@RequestBody InsTagLibClientModel tagLibClientModel);

    /**
     * 获取全部禁用的标签
     * @param tagLibClientModel
     * @return
     */
    @PostMapping("/findAllDisableTagLibClient")
    Result<InsTagLibVo> findAllDisableTagLibClient(@RequestBody InsTagLibClientModel tagLibClientModel);


    @PostMapping("/findTagTree")
    Result<List<TagLibCategoryVo>> findTagTree(@RequestBody InsTagLibClientModel tagLibClientModel);

    @PostMapping("/findAllFinalTagLibClientVoList")
    Result<List<TagLibClientTreeVo>> findTagLibTree(@RequestBody InsTagLibClientModel tagLibClientModel);


    @PostMapping("/findAllUpTagLibHierarchicalByTagId")
    Result<List<TagClientVo>> findAllUpTagLibHierarchicalByTagId(@RequestBody InsTagLibClientModel tagLibClientModel);
}
