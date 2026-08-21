package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPreRulesProducer;
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
@LiteflowComponent(id = "isExistPreDataNode", name = "判断是否有前置处理数据节点")
public class IsExistPreDataNode extends AbstractNodeIf {

    private static final Logger log = LoggerFactory.getLogger(IsExistPreDataNode.class);
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
//    @Autowired
//    ProcessPreRulesProducer processPreRulesProducer;

    @Override
    public boolean processIf() throws RetryException {
        try {
            log.info("开始执行前置任务");
            AnlysisDefaultContext context = this.getRequestData();
            final Set<String> paramIds = context.getIds();
            if (CollUtil.isEmpty(paramIds)) {
                log.info("没有读取数据到源数据，无法继续执行1");
                return false;
            }

            //数据未完入库
//            final Set<String> ids = metaDataAnalysisService.isExitsIds(context.getClientId(), paramIds);
            /*if (CollUtil.isEmpty(ids)) {
                log.warn("没有读取数据到源数据，无法继续执行2");
                processPreRulesProducer.pushEvent(MessageDTO.builder()
                                .token("重新写入：数据量与查询数据量未对齐")
                        .source(context.getClientId()).type(context.getWorkflowType()).data(paramIds).build());
                Thread.sleep(200);
                return false;
            }*/
            //等待传入ids集合数据全部入库
//            if(ids.size() != paramIds.size()){
            /*if(ids.size() < paramIds.size()){
                log.info("数据量与查询数据量未对齐 {},{}",ids.size(),paramIds.size());
                processPreRulesProducer.pushEvent(MessageDTO.builder()
                        .token("重新写入：数据量与查询数据量未对齐")
                        .source(context.getClientId()).type(context.getWorkflowType()).data(paramIds).build());
                return false;
            }*/

            //数据未完成初始化
            final Set<String> unprocessedIds = metaDataAnalysisService.unprocessedIds(context.getClientId(), paramIds);
            if (CollUtil.isEmpty(unprocessedIds)) {
                log.warn("没有读取数据到源数据，无法继续执行3");
                return false;
            }
            if(CollUtil.isNotEmpty(unprocessedIds)) {
                log.info("sendPrivateDeliveryData：{}",unprocessedIds);
                //未执行的数据集
                this.sendPrivateDeliveryData("loadPreDataNode", unprocessedIds);
            }
            return true;
        }catch (Exception e){
            throw new RetryException(e.getMessage(),e);
        }
    }

}
