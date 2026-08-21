package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "isExistRuleDataNode", name = "判断是否有规则数据节点")
public class IsExistRuleDataNode extends AbstractNodeIf {
    @Autowired
    IAysModelResltAnalysisService modelResltAnalysisService;

    @Override
    public boolean processIf() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        return CollUtil.isNotEmpty(context.getRuleList());
    }
}
