package com.voc.service.data.integration.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.data.integration.api.ChannelExecutionResultService;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.nodes.context.ChannelDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

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
@LiteflowComponent(id = "saveSuccessedChannelDatasetStatusNode", name = "更新数据状态为成功")
public class SaveSuccessedChannelDatasetStatusNode extends NodeComponent {
    private static final Logger log = LoggerFactory.getLogger(SaveSuccessedChannelDatasetStatusNode.class);
    @Autowired
    ChannelExecutionResultService dataIntegrationRecordService;

    @Override
    public void process() throws Exception {
        ChannelDatasetContext context = this.getRequestData();
        try {
            //记录成功数据标识
//            final Set<String> ids = context.getIds();

            if (CollUtil.isNotEmpty(context.getSuccessfulDataset())) {
//                throw new RuntimeException("【".concat(context.getChannelType()).concat("】未读取到新数据"));
                final List<DataIntegrationRecordModel> list = context.getSuccessfulDataset().stream().map(model -> {

                    ChannelMetaDataModel data = JSONUtil.toBean(String.valueOf(model.getData()), ChannelMetaDataModel.class);
                    // 数据转换
                    final JSONObject attrs2 = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs2()))
                            ? JSONUtil.parseObj(data.getAttrs2()) : new JSONObject();


                    model.setChannelType(context.getChannelType());
                    model.setWorkId(context.getWorkId());
                    model.setStatus(1);
                    model.setData(JSONUtil.createObj().set("md5", attrs2.getStr("md5")));
                    model.setRetryCount(model.getRetryCount() + 1);
                    return model;
                }).collect(Collectors.toList());
                log.info("保存成功数据:",list.size());
                dataIntegrationRecordService.saveList(context.getClientId(), list);
            }


        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }


    @Override
    public boolean isAccess() {
        ChannelDatasetContext context = this.getRequestData();
        return true;
    }

}
