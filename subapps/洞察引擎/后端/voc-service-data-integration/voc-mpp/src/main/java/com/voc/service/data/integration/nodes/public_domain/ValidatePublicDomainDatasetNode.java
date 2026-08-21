package com.voc.service.data.integration.nodes.public_domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "validatePublicDomainDatasetNode", name = "校验数据有效性，分出有效数据和无效数据")
public class ValidatePublicDomainDatasetNode extends NodeComponent {
    private static final Logger log = LoggerFactory.getLogger(ValidatePublicDomainDatasetNode.class);
    @Autowired
    DataIntegrationConfig config;

    @Override
    public void process() throws Exception {
        PublicDomainDatasetContext context = this.getRequestData();
        try {
            List<DataIntegrationRecordModel> successfulDataset = new CopyOnWriteArrayList();
            List<DataIntegrationRecordModel> failedDataset = new CopyOnWriteArrayList<>();
            context.getPublicDomainDataset().stream().forEach(model -> {
                final DataIntegrationRecordModel validateResult
                        = this.meetRequirements(context.getChannelType(), model);
                if (StrUtil.isBlank(validateResult.getErrorCode())) {
                    //记录成功数据
                    successfulDataset.add(validateResult);
                } else {
                    log.error("【{}】{} 数据未通过校验：{}", context.getWorkId(),context.getChannelType(), Optional.ofNullable(model));
                    //记录失败数据
                    failedDataset.add(validateResult);
                }
            });

            context.setSuccessfulDataset(successfulDataset);
            context.setFailedDataset(failedDataset);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
//        Assert.isTrue(CollUtil.isNotEmpty(context.getPublicDomainDataset()), "getIds cannot be empty");
        if(CollUtil.isEmpty(context.getPublicDomainDataset())){
            log.warn("【{}】{} 数据为空", context.getWorkId(),context.getChannelType());
            return false;
        }
        return true;
    }

    /**
     * 判断是否满足必填项校验要求
     *
     * @param model
     * @return
     */
    private DataIntegrationRecordModel meetRequirements(String channelType, DataIntegrationRecordModel model) {

        return model;
    }
}
