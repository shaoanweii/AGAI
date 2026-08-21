package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.MetaDataAnalysisProducer;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "modifyMetaDataAnalysisStatusNode", name = "修改前置处理数据状态节点")
public class ModifyMetaDataAnalysisStatusNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(ModifyMetaDataAnalysisStatusNode.class);
    @Autowired
    IAysPreprocessDataService preprocessDataService;
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;
    @Autowired
    IAysMetaDataAnalysisService iAysMetaDataAnalysisService;

    @Override
    public void process() throws Exception {
        try {
//            final Set<String> ids = this.getPrivateDeliveryData();
//            Assert.isTrue(CollUtil.isNotEmpty(ids), "processData ids cannot be empty");
            AnlysisDefaultContext context = this.getRequestData();

            //保存数据
//          metaDataAnalysisService.modifyToDone(context.getClientId(), ids);
            final List<AysProcessDataModel> dataModels = context.getProcessData();

            //过滤出已被过滤的数据id集合
            final Set<String> abandonIds = dataModels.stream()
                    .filter(model -> "1".equalsIgnoreCase(model.getAbandon()))
                    .map(AysProcessDataModel::getDataId)
                    .collect(Collectors.toSet());
            log.debug("abandonIds:{}", abandonIds);

            if (CollUtil.isNotEmpty(abandonIds)) {
                try {
                    //优化  --  voc_anal_flow_mate_data_status
//                metaDataAnalysisService.modifyToDataStatus(context.getClientId(), abandonIds,"1");
                    Map<String, Integer> dataStatusMap = abandonIds.stream().collect(Collectors.toMap(id -> id, id -> 2, (oldVal, newVal) -> newVal));
                    String clientId = context.getClientId();
                    if (MapUtil.isNotEmpty(dataStatusMap)) {
                        log.debug("原始数据状态:{}", dataStatusMap);
                        iAysMetaDataAnalysisService.modifyToDataStatusMq(clientId, dataStatusMap);
                    }
                }catch (Exception e){
                    log.error("修改数据状态失败：{}", abandonIds, e);
                }
            }
            //模型不接收已被过滤的数据
            final List<AysProcessDataModel> validDataset = dataModels.stream()
                    .filter(model -> !abandonIds.contains(model.getDataId())).toList();
            context.setProcessData(validDataset);


            final Set<String> ids = context.getProcessData().stream()
                    .filter(model -> "0".equalsIgnoreCase(model.getAbandon()))
                    .map(AysProcessDataModel::getDataId)
                    .collect(Collectors.toSet());
            log.debug("processData ids:{}", ids);
            this.sendPrivateDeliveryData("notificationNode" ,ids);
//            final Collection<String> intersectionList = CollUtil.intersection(ids_, ids);
//            batchPushRecordV2Service.modifyStatus(context.getClientId(), new HashSet<>(intersectionList), "1", "A");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }
}
