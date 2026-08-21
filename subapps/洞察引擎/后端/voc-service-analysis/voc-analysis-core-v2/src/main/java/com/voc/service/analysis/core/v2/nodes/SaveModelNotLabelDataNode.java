package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysModelResltAnalysisMissService;
import com.voc.service.analysis.api.IAysModelResltService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.enums.PreDataStatus;
import com.voc.service.analysis.model.AysModelResltDataAnalysisMissModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Title: saveNotModelLabelDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/23 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "saveModelNotLabelDataNode", name = "保存模型未打标数据节点")
public class SaveModelNotLabelDataNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(SaveModelNotLabelDataNode.class);
    @Autowired
    IAysModelResltAnalysisMissService iAysModelResltAnalysisMissService;

    @Override
    public void process() throws Exception {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            List<AysModelResltDataAnalysisMissModel> modelNotLabelDataList = context.getModelNotLabelDataList();
            String clientId = context.getClientId();
            List<AysProcessDataModel> processData = context.getProcessData();
            if (CollectionUtil.isNotEmpty(modelNotLabelDataList)) {
                log.info("模型未打标数据:{},{}", modelNotLabelDataList.size(), processData);
                iAysModelResltAnalysisMissService.saveBatch(clientId, modelNotLabelDataList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(), e);
    }
}
