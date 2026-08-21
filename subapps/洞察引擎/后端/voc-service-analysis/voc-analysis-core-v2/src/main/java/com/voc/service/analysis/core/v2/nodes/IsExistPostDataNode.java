package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPostRulesProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;

import java.util.Set;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "isExistPostDataNode", name = "判断是否有后置处理数据节点")
public class IsExistPostDataNode extends AbstractNodeIf {

    private static final Logger log = LoggerFactory.getLogger(IsExistPostDataNode.class);
    @Autowired
    IAysModelResltAnalysisService modelResltAnalysisService;
    @Autowired
    ProcessPostRulesProducer processPostRulesProducer;

    @Override
    public boolean processIf() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final Set<String> paramIds = context.getIds();
            if (CollUtil.isEmpty(paramIds)) {
                log.info("没有读取数据到源数据，无法继续执行1");
                return false;
            }
            //数据未完入库
            final Set<String> ids = modelResltAnalysisService.isExitsIds(context.getClientId(), paramIds);
            if (CollUtil.isEmpty(ids)) {
                log.warn("没有读取数据到源数据，无法继续执行2");
  //              processPostRulesProducer.pushEvent(MessageDTO.builder().source(context.getClientId()).type(context.getWorkflowType()).data(paramIds).build());
                Thread.sleep(500);
                return false;
            }
            log.info("后置处理数量,{},{}", ids.size(), context.getModelCount());
            //等待传入ids集合数据全部入库
            if (ids.size() != paramIds.size()) {
     //           processPostRulesProducer.pushEvent(MessageDTO.builder().source(context.getClientId()).type(context.getWorkflowType()).data(paramIds).build());
                return false;
            }
            //数据未完成初始化
            final Set<String> unprocessedIds = modelResltAnalysisService.unprocessedIds(context.getClientId(), ids);
            if (CollUtil.isEmpty(unprocessedIds)) {
                log.warn("没有读取数据到源数据，无法继续执行3");
                return false;
            }

            return true;
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

}
