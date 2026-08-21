package com.voc.service.analysis.core.v2.nodes.valid;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisValidService;
import com.voc.service.analysis.api.IRuleDataServcie;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "modifyModelResltDataAnalysisValidStatusNode", name = "[校验]修改后置处理数据状态节点")
public class ModifyModelResltDataAnalysisValidStatusNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(ModifyModelResltDataAnalysisValidStatusNode.class);
    @Autowired
    IAysModelResltAnalysisValidService modelResltAnalysisService;

    @Autowired
    IRuleDataServcie ruleDataServcie;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        final Set<String> ids = this.getPrivateDeliveryData();
        Assert.isTrue(CollUtil.isNotEmpty(ids), "getPrivateDeliveryData  cannot be empty");
        modelResltAnalysisService.modifyToDone(context.getClientId(), ids);
        log.info("[校验]修改后置处理数据状态节点{}", ids);
        ruleDataServcie.setRuleStatusOk(context.getWorkId(), context.getClientId());
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }
}
