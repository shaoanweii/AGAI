package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.data.InsDataResourceDescModel;
import com.voc.service.insights.engine.model.data.InsDataResourceModel;
import com.voc.service.insights.engine.model.data.InsDataSourceModel;
import com.voc.service.insights.engine.vo.data.ResourceDescDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 资源组
 *
 * @author lww
 */
@FeignClient(name = "service.ins.dataResource", url = "${service.ins.v1}",configuration = InsDataServiceClientConfig.class)
public interface InsDataResourceClient {

    /**
     * 根据资源ID查询资源组信息
     *
     * @param model {@link InsDataResourceDescModel}{resourceId：资源ID，statusFilters：状态过滤< 已启用:Enabled、未启用:NotEnabled、已禁用:Disabled >}
     * @return {@link Result}<{@link List}<{@link ResourceDescDto}>>
     */
    @PostMapping("/insDataResourceDesc/queryByResourceId")
    Result<List<ResourceDescDto>> queryByResourceId(@RequestBody InsDataResourceDescModel model);

    @PostMapping("/insDataResourceDesc/findByConditon")
    Result<List<ResourceDescDto>> findByConditon(@RequestBody InsDataResourceDescModel model);
    @PostMapping("/insDataResourceDesc/findAllDataResourceDesc")
    Result<List<ResourceDescDto>> findAllDataResourceDesc(@RequestBody InsDataResourceDescModel model);
    @PostMapping("/insDataSource/updateSIDataSource")
    void updateSIDataSource(@RequestBody InsDataSourceModel insDataSourceModel);

    @PostMapping("/insDataResource/findAllDataResourceList")
    Result<List<InsDataResourceModel>> findAllDataResourceList(@RequestBody InsDataResourceModel model);
}
