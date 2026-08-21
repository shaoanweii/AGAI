package com.voc.service.analysis.core.v2.nodes.abstracts;

import com.yomahub.liteflow.core.NodeIfComponent;

/**
 * @Title: AbstractNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:33
 * @Version:1.0
 */
public abstract class AbstractNodeIf extends NodeIfComponent {
    /*@Override
    public void onError(Exception e) throws Exception {
        System.out.println("" + this.getFirstContextBean());
        AnlysisDefaultContext context = this.getRequestData();
        context.getStopWatch().stop();
        context.getStopWatch().start(String.format("异常↓↓ [%s]节点：%s", this.getName(), e.getMessage()));
        context.getStopWatch().stop();
        log.error("{}异常 {}", this.getName(), e.getMessage());
        this.getSlot().setException(e);
        //是否结束整个流程
        super.setIsEnd(true);
    }

    @Override
    public void onSuccess() throws Exception {
        super.onSuccess();
        log.info(String.format("[%s-%s]节点完成", this.getName()
                , StrUtil.isNotBlank(this.getTag()) ? ":".concat(this.getTag()) : ""));
        AnlysisDefaultContext context = this.getRequestData();
        context.getStopWatch().stop();
    }

    @Override
    public void beforeProcess() {
        super.beforeProcess();
        AnlysisDefaultContext context = this.getRequestData();
        context.getStopWatch().start(String.format("[%s%s]节点", this.getName()
                , StrUtil.isNotBlank(this.getTag()) ? ":".concat(this.getTag()) : ""));
    }*/


}
