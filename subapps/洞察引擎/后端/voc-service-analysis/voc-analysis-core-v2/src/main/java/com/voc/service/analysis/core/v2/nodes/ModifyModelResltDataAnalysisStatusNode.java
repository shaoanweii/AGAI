package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

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
@LiteflowComponent(id = "modifyModelResltDataAnalysisStatusNode", name = "修改后置处理数据状态节点")
public class ModifyModelResltDataAnalysisStatusNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(ModifyModelResltDataAnalysisStatusNode.class);
    @Autowired
    IAysModelResltAnalysisService modelResltAnalysisService;
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;
    @Override
    public void process() throws Exception {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final Set<String> ids = this.getPrivateDeliveryData();
            Assert.isTrue(CollUtil.isNotEmpty(ids), "processData ids cannot be empty");

            modelResltAnalysisService.modifyToDone(context.getClientId(), ids);
            log.info("{}", this.getName());

            //过滤出已被过滤的数据id集合
            final Set<String> idList = context.getProcessData().stream()
                    .map(AysProcessDataModel::getOriginalId)
                    .collect(Collectors.toSet());
            batchPushRecordV2Service.modifyStatus(context.getClientId(), idList, "1", "B");
        }catch (Exception e){
            log.error(e.getMessage(),e);
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
