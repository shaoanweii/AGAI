package com.voc.service.analysis.core.v2.nodes.valid;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisValidService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "isExistValidPostDataNode", name = "[校验]判断是否有后置处理数据节点")
public class IsExistValidPostDataNode extends AbstractNodeIf {

    private static final Logger log = LoggerFactory.getLogger(IsExistValidPostDataNode.class);
    @Autowired
    IAysModelResltAnalysisValidService modelResltAnalysisService;

    @Override
    public boolean processIf() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        final Set<String> ids = modelResltAnalysisService.findIincompleteData(context.getClientId());
        if (CollUtil.isEmpty(ids)) {
            log.info("没有读取数据到源数据，无法继续执行");
            return false;
        }
        if(CollUtil.isNotEmpty(ids)) {
            this.sendPrivateDeliveryData("loadValidPostDataNode", ids);
        }
        return true;
    }
}
