package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.enums.PreDataStatus;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Map;
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
@LiteflowComponent(id = "isExistLabelDataNode", name = "判断是否有打标数据处理节点")
public class IsExistLabelDataNode extends AbstractNodeIf {


    private static final Logger log = LoggerFactory.getLogger(IsExistLabelDataNode.class);
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;

    @Override
    public boolean processIf() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        Set<String> ListIds = context.getProcessData().stream().map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
        boolean notEmpty = CollUtil.isNotEmpty(context.getModelLabelDataList());
        Set<String> allIdList = new HashSet<>();
        if (notEmpty) {
            Set<String> ids = context.getModelLabelDataList().stream().map(AysModelResltDataAnalysisModel::getOriginalId).collect(Collectors.toSet());
            allIdList.addAll(ids);
        }
        Map<String, Integer> dataStatusMap = context.getDataStatusMap();
        if (MapUtil.isNotEmpty(dataStatusMap)) {
            Set<String> filteredKeys = new java.util.HashSet<>();
            Set<String> markedDataKeys = new java.util.HashSet<>();
            for (Map.Entry<String, Integer> entry : dataStatusMap.entrySet()) {
                if (entry.getValue().equals(PreDataStatus.MISS_DATA.getCode())) {
                    filteredKeys.add(entry.getKey());
                }
                if (entry.getValue().equals(PreDataStatus.MARKED_DATA.getCode())) {
                    markedDataKeys.add(entry.getKey());
                }
            }
            if (CollUtil.isNotEmpty(markedDataKeys)) {
                log.info("打标数据更新状态:{}", markedDataKeys);
                batchPushRecordV2Service.modifyStatus(context.getClientId(), markedDataKeys, "1", "C");
            }
            allIdList.addAll(filteredKeys);
        }
        Set<String> filterIdList = ListIds.stream().filter(l -> !allIdList.contains(l)).collect(Collectors.toSet());
        log.info("全部数据集合:{}", ListIds);
        log.info("要处理的数据集合:{}", allIdList);
        if (CollUtil.isNotEmpty(filterIdList)) {
            log.info("判断是否有打标数据更新状态:{}", filterIdList);
            batchPushRecordV2Service.modifyStatus(context.getClientId(), filterIdList, "-1", "C");
        }
        return notEmpty;
    }

}
