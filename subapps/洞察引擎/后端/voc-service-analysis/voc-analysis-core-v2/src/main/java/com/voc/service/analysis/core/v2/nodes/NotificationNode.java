package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessCallModelProducer;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPostRulesProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "notificationNode", name = "发送消息通知节点")
public class NotificationNode extends AbstractNode {

    static final String TAG_CALL_MODEL = "callModel";
    private static final Logger log = LoggerFactory.getLogger(NotificationNode.class);
    @Autowired
    ProcessCallModelProducer processCallModelProducer;
    @Autowired
    AnalysisConfig config;

    @Override
    public void process() throws Exception {
        log.info("通知消息类型 tag: {}", this.getTag());
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final Set<String> ids = this.getPrivateDeliveryData();
            log.debug("通知消息数据量: {}", ids);
//            final Set<String> ids = context.getProcessData().stream().filter(model -> "0".equals(model.getAbandon()))
//                    .map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
            if (TAG_CALL_MODEL.equalsIgnoreCase(this.getTag()) && CollUtil.isNotEmpty(ids)) {
                log.info("调用模型分配数据量: {}", config.getModelParamsSize());
                List<List<String>> sub = CollUtil.split(ids, config.getModelParamsSize());
                for (List<String> idList : sub) {
                    processCallModelProducer.pushEvent(MessageDTO.builder().source(context.getClientId())
                            .type(context.getWorkflowType())
                            .data(idList).requestId(context.getWorkId()).build());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(ArrayUtil.isNotEmpty(context.getProcessData()), "getProcessData cannot be empty");
        Assert.isTrue(ArrayUtil.isNotEmpty(context.getClientId()), "getClientId cannot be empty");
        return true;
    }
}
