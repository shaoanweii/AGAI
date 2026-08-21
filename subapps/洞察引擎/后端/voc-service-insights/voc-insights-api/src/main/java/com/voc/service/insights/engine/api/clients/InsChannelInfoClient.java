package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsChannelInfoModel;
import com.voc.service.insights.engine.vo.ChannelInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 资源组
 *
 * @author lww
 */
//@FeignClient(name = "service.ins.channel", url = "http://localhost:8088/channel", configuration = InsDataServiceClientConfig.class)
@FeignClient(name = "service.ins.channel", url = "${service.ins.v1}/channel", configuration = InsDataServiceClientConfig.class)
public interface InsChannelInfoClient {

    /**
     * 根据资源ID查询资源组信息
     */
    @PostMapping("/findAll")
    Result<List<ChannelInfoVo>> findAll(@RequestBody InsChannelInfoModel insChannelInfoModel);
    /**
     * 根据下级渠道id查找上级渠道
     */
    @PostMapping("/findChannelByIds")
    Result<List<ChannelInfoVo>> upwardFindChannelHierarchical(@RequestBody InsChannelInfoModel insChannelInfoModel);

    /**
     * 查询渠道树
     */
    @GetMapping("/getChannelTree")
    Result<List<ChannelInfoVo>> getChannelTree(@RequestParam String clientId,@RequestParam(required = false) Integer level);


}
