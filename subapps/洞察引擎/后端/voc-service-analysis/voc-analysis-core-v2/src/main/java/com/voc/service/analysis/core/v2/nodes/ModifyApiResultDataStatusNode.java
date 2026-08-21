package com.voc.service.analysis.core.v2.nodes;

import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysModelResltService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: modifyToMetaDataAnalysisStatusNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/23 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "modifyApiResultDataStatusNode", name = "更新原始数据状态节点")
public class ModifyApiResultDataStatusNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(ModifyApiResultDataStatusNode.class);
    @Autowired
    IAysMetaDataAnalysisService iAysMetaDataAnalysisService;

    @Autowired
    IAysModelResltService resltDataService;

    @Override
    public void process() throws Exception {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            String clientId = context.getClientId();
            Set<String> ids = context.getModelLabelDataList().stream().map(AysModelResltDataAnalysisModel::getOriginalId).collect(Collectors.toSet());
            resltDataService.modifyToDone(clientId, ids);
        } catch (Exception e) {
            log.error("更改模型原始数据状态错误:", e);
        }
    }
}
