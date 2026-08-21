package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.map.MapUtil;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @Title: modifyToMetaDataAnalysisStatusNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/23 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "modifyToMetaDataAnalysisStatusNode", name = "更新原始数据状态节点")
public class ModifyToMetaDataAnalysisStatusNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(ModifyToMetaDataAnalysisStatusNode.class);
    @Autowired
    IAysMetaDataAnalysisService iAysMetaDataAnalysisService;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        Map<String, Integer> dataStatusMap = context.getDataStatusMap();
        String clientId = context.getClientId();
        log.info("原始数据状态:{}", dataStatusMap.size());
        if (MapUtil.isNotEmpty(dataStatusMap)) {
            iAysMetaDataAnalysisService.modifyToDataStatusMq(clientId, dataStatusMap);
        }
    }
}
