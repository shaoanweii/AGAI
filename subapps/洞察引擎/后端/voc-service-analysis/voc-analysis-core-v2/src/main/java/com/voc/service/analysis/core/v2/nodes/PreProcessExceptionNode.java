package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.api.IAysBatchPushRecordV2Service;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;

import java.util.HashSet;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "preProcessExceptionNode", name = "前置过滤异常处理节点")
public class PreProcessExceptionNode extends NodeComponent {

    private static final Logger log = LoggerFactory.getLogger(PreProcessExceptionNode.class);
    @Autowired
    IAysBatchPushRecordV2Service batchPushRecordV2Service;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();

            log.info("前置规则处理异常数据 ids:{}", context.getErrorIds());
            batchPushRecordV2Service.modifyStatus(context.getClientId(), context.getErrorIds(), "-1", "A");
            context.setErrorIds(new HashSet<>());
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        if (CollUtil.isEmpty(context.getErrorIds())) {
            return false;
        }
        return true;
    }
}
