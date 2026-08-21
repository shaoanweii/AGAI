package com.voc.service.data.integration.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeIteratorComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "loadRetryChannelDatasetNode", name = "（重试）加载未处理完成数据集节点")
public class LoadRetryChannelDatasetNode extends NodeIteratorComponent {
    @Autowired
    IMppInputDataService inputDataService;
    @Autowired
    DataIntegrationConfig config;
    @Autowired
    ChannelExecutionResultService dataIntegrationRecordService;
    @Override
    public Iterator<List<String>> processIterator() throws Exception {
        ChannelDatasetContext context = this.getRequestData();
        try {
            final List<DataIntegrationRecordModel> result = this.loadRetryData(context.getWorkId(), context.getChannelType(), new Date(), new Date());

            if (CollUtil.isEmpty(result)) {
//                throw new Exception("【".concat(context.getChannelType()).concat("】未读取到新数据"));
                return Collections.EMPTY_LIST.iterator();
            }
            context.setChannelType(result.get(0).getChannelType());
            final Set<String> ids = result.stream().map(DataIntegrationRecordModel::getId).collect(Collectors.toSet());
            return CollUtil.split(ids, config.getBatchPushApiDataSetSize()).iterator();
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
//        Assert.isTrue(StrUtil.isNotBlank(context.getChannelType()), "getChannelType cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getChannelId cannot be empty");

        return true;
    }

    /**
     * 读取到渠道数据
     *
     * @param channelType
     * @return
     */
    private List<DataIntegrationRecordModel> loadRetryData(String workId, String channelType, Date startDate, Date endDate) {
        final List<DataIntegrationRecordModel> list = dataIntegrationRecordService.loadRetryData(channelType, startDate, endDate);
        return list;
        /*if (CollUtil.isNotEmpty(list)) {
            log.info("【{}】本次加载数据量：{}", channelType, list.size());
            return list.stream().map(item -> {
                return DataIntegrationRecordModel.builder()
                        .id(item.getId())
                        .data(item)
                        .dataId(item.getDataId())
                        .channelType(item.getChannelBiz())
                        .createTime(item.getCreateTime())
                        .retryCount()
                        .workId(workId)
                        .tid(ServiceContextHolder.traceId())
                        .build();
            }).collect(Collectors.toList());
        }
        return Collections.EMPTY_LIST;*/
    }

}
