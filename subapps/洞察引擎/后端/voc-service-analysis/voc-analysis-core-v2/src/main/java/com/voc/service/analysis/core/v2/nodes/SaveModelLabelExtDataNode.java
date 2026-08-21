package com.voc.service.analysis.core.v2.nodes;

import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: saveModelLabelDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/23 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "saveModelLabelExtDataNode", name = "保存模型已打标数据节点")
public class SaveModelLabelExtDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(SaveModelLabelExtDataNode.class);
    @Autowired
    IAysModelResltAnalysisService iAysModelResltAnalysisService;

    @Value("${analysis.flow.nodes.saveModelLabelExtDataNode.enable:true}")
    boolean enable;

    @Override
    public void process() throws Exception {
        try {
            if(!enable){
                return ;
            }
            AnlysisDefaultContext context = this.getRequestData();
            String clientId = context.getClientId();
            List<AysModelResltDataAnalysisModel> modelLabelDataList = context.getModelLabelDataList();
            iAysModelResltAnalysisService.saveBatchExtAnalysis(clientId, modelLabelDataList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(), e);
        AnlysisDefaultContext context = this.getRequestData();
        final Set<String> ids = context.getModelLabelDataList().stream().map(AysModelResltDataAnalysisModel::getDataId).collect(Collectors.toSet());
        log.info("模型已打标异常数据 ids:{}", ids);
    }
}
