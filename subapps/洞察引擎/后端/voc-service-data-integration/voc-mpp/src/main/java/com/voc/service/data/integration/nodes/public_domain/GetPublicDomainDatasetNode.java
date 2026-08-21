package com.voc.service.data.integration.nodes.public_domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.data.integration.api.IPublicDomainService;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeIteratorComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "getPublicDomainDatasetNode", name = "获取渠道数据集节点")
public class GetPublicDomainDatasetNode extends NodeIteratorComponent {
    private static final Logger log = LoggerFactory.getLogger(GetPublicDomainDatasetNode.class);
    @Autowired
    IPublicDomainService publicDomainService;
    @Autowired
    DataIntegrationConfig config;

    @Override
    public Iterator<List<String>> processIterator() throws Exception {
        PublicDomainDatasetContext context = this.getRequestData();
        try {
            final Set<String> ids = publicDomainService.findAllIds();
            log.info("【{}】获取渠道数据集：{}", context.getWorkId(),ids.size());

            return CollUtil.split(ids, config.getBatchCleanedAppEventsDataSetSize()).iterator();
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }


    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getChannelId cannot be empty");

        return true;
    }

}
