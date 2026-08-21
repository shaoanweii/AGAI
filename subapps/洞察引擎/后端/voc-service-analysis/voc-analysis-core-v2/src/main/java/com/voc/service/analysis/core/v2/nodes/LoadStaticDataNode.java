package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IStaticDataServcie;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.utils.AnlysisContextHolder;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "loadStaticDataNode", name = "加载静态数据节点")
public class LoadStaticDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadStaticDataNode.class);
    @Autowired
    IStaticDataServcie staticDataServcie;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final String clientId = context.getClientId();
            log.info("加载静态数据 tag: {} ,clientId: {}", this.getTag(),clientId);

            Map<String, Set<String>> map;
            //加载校验静态规则
            if (StrUtil.isNotBlank(this.getTag()) && "valid".equalsIgnoreCase(this.getTag())) {
                log.info("加载校验静态规则");
                  map = staticDataServcie.getValidResourceGroup(clientId);
                if (Objects.isNull(map)) {
                    log.info("加载校验静态规则异常，数据为空");
                }
            } else {
                log.info("加载静态数据");
                map = staticDataServcie.getAllEnabledResourceGroup(clientId);
                if (Objects.isNull(map)) {
                    log.info("加载静态数据，数据为空");
                }
            }
            log.info("加载静态数据-》getResourceGroup 结束 {}, {}", context.getClientId(),map.size());
            AnlysisContextHolder.setResourcesGroupData(context.getClientId(),map);

        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }
}
