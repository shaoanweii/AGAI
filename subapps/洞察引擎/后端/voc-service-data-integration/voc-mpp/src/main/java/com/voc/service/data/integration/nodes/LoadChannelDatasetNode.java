package com.voc.service.data.integration.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeIteratorComponent;
import jakarta.annotation.Resource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "loadChannelDatasetNode", name = "根据渠道类型加载不同数据集节点")
public class LoadChannelDatasetNode extends NodeIteratorComponent {
    private static final Logger log = LoggerFactory.getLogger(LoadChannelDatasetNode.class);
    @Autowired
    IMppInputDataService inputDataService;
    @Autowired
    DataIntegrationConfig config;

    @Autowired
    RedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public Iterator<List<String>> processIterator() throws Exception {
        ChannelDatasetContext context = this.getRequestData();
        try {
            final Set<String> result = this.loadData(context.getWorkId(), context.getChannelType(), context.getStartDate(), context.getEndDate());
            log.info("【{}】本次加载数据总量：{}", context.getChannelType(), result.size());
            if (CollUtil.isEmpty(result)) {
//                throw new Exception("【".concat(context.getChannelType()).concat("】未读取到新数据"));
                return Collections.EMPTY_LIST.iterator();
            }

            return CollUtil.split(result, config.getBatchPushApiDataSetSize()).iterator();
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getChannelId cannot be empty");

        return true;
    }

    /**
     * 读取到渠道数据
     *
     * @param channelType
     * @return
     */

    public Set<String> loadData(String workId, String channelType, Date startDate, Date endDate) {
        final Set<String> list = inputDataService.loadDataIds(channelType, startDate, endDate);
        if (CollUtil.isNotEmpty(list)) {
            log.info("【{}】本次加载数据量：{}", channelType, list.size());
            return list;
        }else{
            log.info("本次无加载数据：{}", channelType);
        }
        return Collections.EMPTY_SET;
    }
}
