package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IStaticDataServcie;
import com.voc.service.analysis.core.v2.events.context.AnlysisEventContext;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "runRuleNode", name = "运行规则计算节点")
public class RunRuleNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(RunRuleNode.class);
    @Resource
    private FlowExecutor flowExecutor;
    @Autowired
    IAysMetaDataAnalysisService aysMetaItemsDataService;

    @Autowired
    AysConvertMapperService convertMapperService;
    @Autowired
    public IStaticDataServcie staticDataServcie;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();

            final String eventChainId = this.getPrivateDeliveryData();
            Assert.isTrue(StrUtil.isNotBlank(eventChainId), "eventChainId cannot be empty");
            log.info("get chainId= {}", eventChainId);

            final List<AysProcessDataModel> processData = context.getProcessData();
            log.info("processData size:{}, eventContext.getClientId:{}", processData.size() ,context.getClientId());
            //放入即将执行规则计算的数据集
            for (AysProcessDataModel data : processData) {
                AnlysisEventContext eventContext = AnlysisEventContext.builder()
                        .workId(data.getWorkId())
                        .clientId(context.getClientId())
                        .finshData(data)
//                    .resourcesGroupData(resourceMap)
                        .build();

                //执行链路
                LiteflowResponse res = flowExecutor.execute2Resp(eventChainId, eventContext,context.getWorkId());
                if (!res.isSuccess()) {
                    log.error("id:{}  {}", data.getId(), res.getMessage());
                    context.getErrorIds().add(data.getDataId());
                }
            }

            log.info("执行条件集完成 {}", this.getTag());
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        ;
        Assert.isTrue(CollUtil.isNotEmpty(context.getRuleList()), "getRuleList cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(context.getChannelIds()), "getChannelIds cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getContentType()), "getContentType cannot be empty");


        return true;
    }


}
