package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IRuleDataServcie;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.rule.ComputLogicModel;
import com.voc.service.insights.engine.enums.RuleStage;
import com.voc.service.insights.engine.enums.RuleStatusType;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;
import org.springframework.util.Assert;

import java.util.List;
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
@LiteflowComponent(id = "loadRuleDataNode", name = "加载规则数据节点")
public class LoadRuleDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadRuleDataNode.class);
    @Autowired
    IRuleDataServcie ruleDataServcie;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final List<ComputLogicModel> ruleList = ruleDataServcie.getRuleData(context.getClientId(), null);

            if (CollUtil.isEmpty(ruleList)) {
                log.warn("当前客户无可执行规则[{}]，请先配置规则 {}", this.getTag(), context.getClientId());
                return;
            }
            //正常判断规则范围 前置+后置的已【启用】
            List<ComputLogicModel> list = new CopyOnWriteArrayList();
            if (RuleStage.PreRule.getCode().equals(this.getTag())) {
                list.addAll(ruleList.stream()
                        .filter(e -> RuleStatusType.Enabled.getCode().equalsIgnoreCase(e.getStatus()))
                        .filter(e -> RuleStage.PreRule.getCode().equalsIgnoreCase(e.getStage())).collect(Collectors.toList()));
            } else if (RuleStage.PostRule.getCode().equals(this.getTag())) {
                list.addAll(ruleList.stream()
                        .filter(e -> RuleStatusType.Enabled.getCode().equalsIgnoreCase(e.getStatus()))
                        .filter(e -> RuleStage.PostRule.getCode().equalsIgnoreCase(e.getStage())).collect(Collectors.toList()));
            }
            log.info("加载规则数据节点：{}",list.size());
            context.getRuleList().addAll(list);
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }

    @Override
    public void onError(Exception e) throws Exception {
       super.onError(e);
       log.error(e.getMessage(),e);
       throw new RetryException(e.getMessage(),e);
    }
}
