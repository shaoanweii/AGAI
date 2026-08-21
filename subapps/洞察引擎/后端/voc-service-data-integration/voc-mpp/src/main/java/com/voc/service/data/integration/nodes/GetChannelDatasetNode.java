package com.voc.service.data.integration.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "getChannelDatasetNode", name = "获取渠道数据集节点")
public class GetChannelDatasetNode extends NodeComponent {
    private static final Logger log = LoggerFactory.getLogger(GetChannelDatasetNode.class);
    @Autowired
    IMppInputDataService inputDataService;
    @Autowired
    ChannelExecutionResultService dataIntegrationRecordService;

    @Override
    public void process() throws Exception {
        ChannelDatasetContext context = this.getRequestData();
//        final List<DataIntegrationRecordModel> result = this.getCurrLoopObj();
        final List<DataIntegrationRecordModel> result = context.getChannelDataset();
        if (CollUtil.isEmpty(result)) {
            //是否结束整个流程
            super.setIsEnd(true);
            return;
        }

        log.info("【{}】本次执行数据集：{}", context.getChannelType(), result.size());
        context.setChannelDataset(result);
    }

    private String getId(JSONObject jsonObj, String field) {
        return jsonObj.getStr(field);
    }


    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getChannelId cannot be empty");

        return true;
    }

}
