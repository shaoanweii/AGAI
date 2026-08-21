package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.core.v2.events.abstracts.AbstractEventNode;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.rule.ComputLogicModel;
import com.voc.service.common.util.IdWorker;
import com.voc.service.common.util.ServiceContextHolder;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.builder.LiteFlowNodeBuilder;
import com.yomahub.liteflow.builder.el.ELBus;
import com.yomahub.liteflow.builder.el.LiteFlowChainELBuilder;
import com.yomahub.liteflow.builder.el.ThenELWrapper;
import com.yomahub.liteflow.flow.FlowBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryException;
import org.springframework.util.Assert;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@LiteflowComponent(id = "generateRuleEventsNode", name = "生成规则节点")
public class GenerateRuleEventsNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(GenerateRuleEventsNode.class);

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();

            //初始化事件nodes
            final String chainId = this.initEventsEL(context.getRuleList(), context.getWorkId());
            if (StrUtil.isBlank(chainId)) {
                throw new Exception("chainId 初始化失败，无法执行事件流程");
            }
            log.info("send chainId:{}", chainId);
            //设置事件流程id，后续执行事件流程时使用
            this.sendPrivateDeliveryData("runRuleNode", chainId);
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(CollUtil.isNotEmpty(context.getRuleList()), "getRuleList cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(context.getChannelIds()), "getChannelIds cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getContentType()), "getContentType cannot be empty");

        return true;
    }

    /**
     * 初始化事件nodes
     */
    private synchronized String initEventsEL(List<ComputLogicModel> rules, String workId) throws Exception {
        //获取所有可执行事件node集合
        Map<String, AbstractEventNode> maps = this.getEvents();
        //根据规则动态创建流程事件节点对象
        List<AbstractEventNode> nodeList = new CopyOnWriteArrayList();
        for (ComputLogicModel rule : rules) {
            Optional<AbstractEventNode> event;
            try {
                event = Optional.ofNullable(maps.values().stream()
                        .filter(eventNode -> rule.getEventCode().contains(eventNode.getNodeId())).findFirst()
                        .orElseThrow(() -> new Exception("事件注解范围内未找到规则定义的数据，请联系管理员")));
            } catch (Exception e) {
                continue;
            }
            if (event.isPresent()) {
                //动态创建事件节点
                final String nodeId = rule.getEventCode().concat("-").concat(rule.getRuleId());
                // 创建通用节点构建器，设置节点 ID 和类，构建节点
                LiteFlowNodeBuilder.createCommonNode().setId(nodeId).setClazz(event.get().getClass()).build();
                // 从 FlowBus 中获取节点实例，并将其强制类型转换为 AbstractEventNode 类型
                AbstractEventNode node = (AbstractEventNode) FlowBus.getNode(nodeId).getInstance();
                // 设置节点的计算逻辑模型为 rule
                node.setComputLogicModel(rule);
                // 设置节点的权重为 rule 的权重
                node.setWeight(rule.getWeight());
                // 将节点添加到节点列表中
                nodeList.add(node);

            }
        }

        if (CollUtil.isEmpty(nodeList)) {
            throw new Exception("当前系统内未找到可执行规则事件，请联系管理员");
        }

        //用户按权重执行event node
        final Map<Integer, List<AbstractEventNode>> nodeMap = MapUtil.sort(nodeList.stream()
                        .collect(Collectors.groupingByConcurrent(AbstractEventNode::getWeight))
                , Comparator.reverseOrder());

        //生成动态el
        /*final ThenELWrapper el = ELBus.then(nodeMap.values().stream()
                .map(list -> ELBus.then(
                        ELBus.when(
                                list.stream().sorted(Comparator.comparing(node -> node.getComputLogicModel().getCreateTime(), Comparator.reverseOrder()))
                                        .map(AbstractEventNode::getNodeId).collect(Collectors.toList()).toArray()
                        )
                )).collect(Collectors.toList()).toArray()
        );*/
        final ThenELWrapper el = ELBus.then(nodeMap.values().stream()
                .map(list -> ELBus.then(
                        list.stream().sorted(Comparator.comparing(node -> node.getComputLogicModel().getCreateTime(), Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(AbstractEventNode::getNodeId).collect(Collectors.toList()).toArray()
                )).collect(Collectors.toList()).toArray()
        );
        //生成事件流程标识
        final String chainId = "events_chain_".concat(workId).concat(IdWorker.getId());
        //注册EL
        LiteFlowChainELBuilder.createChain().setChainId(chainId).setEL(el.toEL()).build();
        return chainId;
    }


    private Map<String, AbstractEventNode> getEvents() throws Exception {
        final Map<String, AbstractEventNode> eventNodeComponentList
                = ServiceContextHolder.getApplicationContext().getBeansOfType(AbstractEventNode.class);
        if (CollUtil.isEmpty(eventNodeComponentList)) {
            throw new Exception("当前系统内未找到可执行规则事件，请联系管理员");
        }

        //检查规则数据中的事件编码是否在系统中已注册
        /*for (String ruleEventCode : eventIds) {
            int contains = CollUtil.count(eventNodeComponentList.keySet(), new Matcher<String>() {
                @Override
                public boolean match(String s) {
                    return ruleEventCode.contains(s);
                }
            });
            if (contains < 1) {
                throw new RuntimeException("数据库内规则数据在，本系统中未注册，请联系管理员 ".concat(ruleEventCode));
            }
        }*/

        return eventNodeComponentList;
    }
}
