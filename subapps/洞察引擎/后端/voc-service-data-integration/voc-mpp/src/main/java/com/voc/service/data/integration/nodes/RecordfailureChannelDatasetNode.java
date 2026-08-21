package com.voc.service.data.integration.nodes;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
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
@LiteflowComponent(id = "recordfailureChannelDatasetNode", name = "记录失败数据节点")
public class RecordfailureChannelDatasetNode extends NodeComponent {
    private static final Logger log = LoggerFactory.getLogger(RecordfailureChannelDatasetNode.class);
    @Autowired
    ChannelExecutionResultService errorDataMsgService;

    @Override
    public void process() throws Exception {
        ChannelDatasetContext context = this.getRequestData();

        try {
            if (CollUtil.isNotEmpty(context.getFailedDataset())) {
                log.info("【".concat(context.getChannelType()).concat("】数据验证失败，失败数据：".concat(String.valueOf(context.getFailedDataset().size()))));
                //保存记录异常数据集
                this.saveFailedData(context.getClientId(), context.getWorkId(), context.getChannelType(), context.getFailedDataset());
            } else {
                log.info("【".concat(context.getChannelType()).concat("】数据验证完成，本次无异常数据"));
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getChannelId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        return true;
    }

    /**
     * 保存记录异常数据集
     *
     * @param failedDataset
     */
    private void saveFailedData(String clientId, String workId, String channelType, List<DataIntegrationRecordModel> failedDataset) {
        if (CollUtil.isEmpty(failedDataset)) {
            return;
        }
        //读取数据重试数据
        final List<DataIntegrationRecordModel> list = failedDataset.stream().map(item -> {
            DataIntegrationRecordModel model = DataIntegrationRecordModel.builder().build();
            BeanUtil.copyProperties(item, model);

            model.setChannelType(channelType);
            model.setStatus(-1);
            model.setData(model.getData());
//            model.setRetryCount(retryCountMap.containsKey(item.getId()) ? retryCountMap.get(item.getId()) +1 : 1);
            model.setRetryCount(item.getRetryCount() + 1);
            model.setLastExecTime(model.getRetryCount() > 0 ? LocalDateTime.now() : null);
            model.setTid(ServiceContextHolder.traceId());

            return model;
        }).collect(Collectors.toList());

        errorDataMsgService.saveList(clientId, list);
    }

}
