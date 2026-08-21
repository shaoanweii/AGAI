package com.voc.service.analysis.clients;

import com.voc.service.analysis.model.ChannelInfoModel;
import com.voc.service.analysis.model.ChannelInfoParamModel;
import com.voc.service.common.response.Result;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author liuhb
 * @version 1.0.0
 * @ClassName IModelServiceClient
 * @createTime 2024年03月08日 14:55
 * @Copyright cuick
 * @Description 获取渠道集合
 */

@FeignClient(name = "service.ins", url = "${service.ins.v1}")
public interface IChannelServiceClient {

    @Schema(description = "获取渠道集合")
    @PostMapping("/channel/findAllChannelInfo")
    Result<List<ChannelInfoModel>> findAllChannel(@RequestBody ChannelInfoParamModel channelInfoParamModel);
}
