package com.voc.service.data.integration.nodes.public_domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.data.integration.api.IPublicDomainService;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "sendSuccessedPublicDomainDataSaveNode", name = "推送数据到公域表息队列")
public class SendSuccessedPublicDomainDataSaveNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(SendSuccessedPublicDomainDataSaveNode.class);

    @Autowired
    IPublicDomainService publicDomainService;

    @Override
    public void process() throws Exception {
        PublicDomainDatasetContext context = this.getRequestData();
        try {
            if (CollUtil.isEmpty(context.getSuccessfulDataset())) {
                throw new Exception("【".concat(context.getChannelType()).concat("】数据验证后无成功数据："));
            } else {
                log.info("【{}】数据验证成功，成功数据：{}", context.getWorkId(), context.getSuccessfulDataset().size());
                //保存记录成功数据集
                final List<DataIntegrationRecordModel> list = context.getSuccessfulDataset().stream()
                        .filter(item -> {
                            JSONObject jsonObj = JSONUtil.parseObj(item.getData());
                            ChannelMetaDataModel data = JSONUtil.toBean(jsonObj, ChannelMetaDataModel.class);
                            if (StrUtil.isBlank(data.getId()) || StrUtil.isBlank(data.getChannelCode()) || ObjUtil.isNull(data.getDataCreateTime())) {
                                log.error("【{}】数据验证失败，无数据标识：{}", context.getWorkId(), data);
                                publicDomainService.saveErrorList(context.getClientId(), CollUtil.newArrayList(item));
                                return false;
                            }
                            return true;
                        }).toList();

                final List<ChannelMetaDataModel> list2 = list.stream()
                        .map(model -> JSONUtil.toBean(JSONUtil.parseObj(model.getData()), ChannelMetaDataModel.class)
                        ).toList();
                log.info("【{}】保存成功数据: {}", context.getWorkId(), list2.size());
                publicDomainService.saveList(context.getClientId(), list2);

            }
        } catch (Exception e) {
            log.info("【{}】推送数据错误信息：", context.getWorkId(), e);
            //出现服务异常时，将所有数据归为异常处理数据集
            final List<DataIntegrationRecordModel> errorList = context.getSuccessfulDataset().stream()
                    .map(data -> {
                        data.setErrorCode(ErrorDataMsgEnums.PushServiceHasFailed.getCode());
                        data.setErrorMsg(ErrorDataMsgEnums.PushServiceHasFailed.getText());
                        return data;
                    }).toList();
            context.getFailedDataset().addAll(errorList);
            context.setSuccessfulDataset(null);
//            throw new Exception(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }
}
