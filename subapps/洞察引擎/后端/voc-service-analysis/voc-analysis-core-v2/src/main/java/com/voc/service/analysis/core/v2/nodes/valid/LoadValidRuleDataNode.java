package com.voc.service.analysis.core.v2.nodes.valid;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IRuleDataServcie;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.rule.ComputLogicModel;
import com.voc.service.insights.engine.enums.RuleStage;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "loadValidRuleDataNode", name = "[校验]加载规则数据节点")
public class LoadValidRuleDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadValidRuleDataNode.class);
    @Autowired
    IRuleDataServcie ruleDataServcie;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        final Set<String> ruleIds;

        //单条校验
        if (CollUtil.isEmpty(context.getValidDataParam().getEnabledRuleIds())) {
            //单条验证规则
            ruleIds = Collections.synchronizedSet(context.getValidDataParam().getValidRuleIds());
        //整体测试 ：  单条+已启用的所有规则
        } else {
            //单条验证规则 + 已启用的所有规则
            ruleIds = Collections.synchronizedSet(CollUtil.newHashSet(CollUtil.union(
                    context.getValidDataParam().getValidRuleIds(), context.getValidDataParam().getEnabledRuleIds())));
        }

        //获取规则数据
        final List<ComputLogicModel> ruleList = ruleDataServcie.getRuleData(context.getClientId(), ruleIds);

        //正常判断规则范围 前置+后置的已【启用+未启用】
        List<ComputLogicModel> list = null;
        //前置规则
        if (RuleStage.PreRule.getCode().equals(this.getTag())) {
            list = ruleList.stream().filter(e -> RuleStage.PreRule.getCode().equalsIgnoreCase(e.getStage()))
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        //后置规则
        } else if (RuleStage.PostRule.getCode().equals(this.getTag())) {
            list = ruleList.stream().filter(e -> RuleStage.PostRule.getCode().equalsIgnoreCase(e.getStage()))
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        }

        if (CollUtil.isEmpty(list)) {
            log.warn("当前客户无可执行规则[{}]，请先配置规则 {}", this.getTag(), context.getClientId());
            return;
        }

        context.setRuleList(list);
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }
}
