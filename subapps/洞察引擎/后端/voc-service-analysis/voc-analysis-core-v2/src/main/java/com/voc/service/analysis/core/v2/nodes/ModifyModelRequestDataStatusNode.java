package com.voc.service.analysis.core.v2.nodes;

import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: modifyModelRequestDataStatusNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/23 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "modifyModelRequestDataStatusNode", name = "前置数据状态变更节点")
public class ModifyModelRequestDataStatusNode extends AbstractNode {

    @Autowired
    IAysPreprocessDataService preprocessDataService;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        String clientId = context.getClientId();
        final List<AysProcessDataModel> parsedData = context.getProcessData();
        // 修改状态为已完成
        final Set<String> ids = parsedData.stream().map(AysProcessDataModel::getId).collect(Collectors.toSet());
        preprocessDataService.modifyToDone(clientId, ids);
    }
}
