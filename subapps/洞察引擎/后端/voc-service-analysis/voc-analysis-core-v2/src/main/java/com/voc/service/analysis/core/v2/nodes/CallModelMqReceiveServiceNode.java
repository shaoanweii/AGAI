package com.voc.service.analysis.core.v2.nodes;

import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.largeModel.vo.ModelResponseVo;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * @author liuhb
 * @version 1.0.0
 * @ClassName callModelMqPushServiceNode
 * @createTime 2024年07月23日 10:49
 * @Copyright liuhb
 * @Description 调用模型后，保存原始数据，成功后标记前置处理数据为已完成状态
 */
@LiteflowComponent(id = "callModelMqReceiveServiceNode", name = "MQ接收模型解析后数据节点")
public class CallModelMqReceiveServiceNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(CallModelMqReceiveServiceNode.class);

    @Override
    public void process() throws Exception {
        log.info("{}", this.getClass().getSimpleName());
        AnlysisDefaultContext context = this.getRequestData();
        ModelResponseVo modelResponseVo = context.getModelResponseVo();
        Assert.isTrue(!ObjectUtils.isEmpty(modelResponseVo), "result cannot be empty");
    }

    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(), e);
    }

}

