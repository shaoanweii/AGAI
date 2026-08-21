package com.voc.service.analysis.core.v2.nodes;

import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysModelResltService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
@LiteflowComponent(id = "exceptionModelDataNode", name = "模型异常数据处理节点")
public class ExceptionModelDataNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(ExceptionModelDataNode.class);
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;

    @Autowired
    IAysModelResltService resltDataService;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        final List<AysProcessDataModel> parsedData = context.getProcessData();
        Set<String> list = parsedData.stream().map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
        resltDataService.modifyToException(context.getClientId(), list);
        final Set<String> ids = context.getProcessData().stream().map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
        log.info("调用模型计算服务异常数据 ids:{}", ids);
        batchPushRecordV2Service.modifyStatus(context.getClientId(), ids, "-1", "G");
    }
}
