package com.voc.service.data.integration.nodes.public_domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.data.integration.api.IPublicDomainService;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.api.model.PublicDomainInfoDataModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "loadPublicDomainDatasetNode", name = "从数据库加载数据集并划分内容类型")
public class LoadPublicDomainDatasetNode extends NodeComponent {
    private static final Logger log = LoggerFactory.getLogger(LoadPublicDomainDatasetNode.class);
    @Autowired
    IPublicDomainService publicDomainService;
    @Autowired
    DataIntegrationConfig config;


    @Override
    public void process() throws Exception {
        PublicDomainDatasetContext context = this.getRequestData();
        final List<String> result = this.getCurrLoopObj();
        log.info("【{}】getCurrLoopObj： {}", context.getWorkId(),result);
        if (CollUtil.isEmpty(result)) {
            log.warn("【{}】本次数据为空",context.getWorkId());
            //是否结束整个流程
            super.setIsEnd(true);
            return;
        }

        List<DataIntegrationRecordModel> list = this.loadData(context.getChannelType(), context.getWorkId(), result);
        log.info("【{}】 {} 条数据",context.getWorkId(), result.size());
        context.setPublicDomainDataset(list);
    }

    private List<DataIntegrationRecordModel> loadData(String channelType, String workId, List<String> ids) {
        List<PublicDomainInfoDataModel> list = publicDomainService.findByIds(new HashSet<>(ids));

        if (CollUtil.isNotEmpty(list)) {
            log.info("【{}】{} 本次加载数据量：{}", workId,channelType, list.size());
            return list.stream().map(item -> {

                return DataIntegrationRecordModel.builder()
                        .id(item.getId())
                        .data(item)
                        .dataId(item.getId())
//                        .channelType(item.getChannelCode())
//                        .createTime(item.getCreateTime())
                        .workId(workId)
                        .tid(ServiceContextHolder.traceId())
                        .build();
            }).collect(Collectors.toList());
        } else {
            log.error("加载数据异常：channelType：{} , ids:{}", channelType, ids);
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getChannelId cannot be empty");

        return true;
    }

}
