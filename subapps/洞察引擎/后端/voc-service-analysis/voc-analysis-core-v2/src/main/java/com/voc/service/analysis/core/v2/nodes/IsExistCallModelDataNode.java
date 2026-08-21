package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessCallModelProducer;
import com.voc.service.analysis.dto.MessageDTO;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;

import java.util.Set;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "isExistCallModelDataNode", name = "判断是否有调用模型处理数据节点")
public class IsExistCallModelDataNode extends AbstractNodeIf {

    private static final Logger log = LoggerFactory.getLogger(IsExistCallModelDataNode.class);
    @Autowired
    IAysPreprocessDataService preprocessDataService;
    @Autowired
    ProcessCallModelProducer processCallModelProducer;
    @Autowired
    AnalysisConfig config;

    @Override
    public boolean processIf() throws Exception {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            String workflowType = context.getWorkflowType();
            if (!config.isInvokeModel() && ObjectUtil.isNotEmpty(workflowType) && workflowType.equals("api_v1")) {
                log.warn("未开启模型调用配置，请检查配置项[invoke_model]");
                return false;
            }
            final Set<String> paramIds = context.getIds();
            if (CollUtil.isEmpty(paramIds)) {
                log.info("没有读取数据到源数据，无法继续执行1");
                return false;
            }

            //数据未完入库
            final Set<String> ids = preprocessDataService.isExitsIds(context.getClientId(), paramIds);
            /*if (CollUtil.isEmpty(ids)) {
                log.warn("没有读取数据到源数据，无法继续执行2");
                processCallModelProducer.pushEvent(MessageDTO.builder().type(context.getWorkflowType()).source(context.getClientId()).data(paramIds).build());
                Thread.sleep(200);
                return false;
            }*/
            //等待传入ids集合数据全部入库
            if (CollUtil.isEmpty(ids) || ids.size() != paramIds.size()) {
                processCallModelProducer.pushEvent(MessageDTO.builder().type(context.getWorkflowType()).source(context.getClientId()).data(paramIds).build());
                Thread.sleep(1000);
                return false;
            }

            //数据未完成初始化
            final Set<String> pushModelids = preprocessDataService.unprocessedIds(context.getClientId(), paramIds);
            log.debug("推送模型数据范围: {}", pushModelids);
            if (CollUtil.isEmpty(pushModelids)) {
                log.warn("没有读取数据到源数据，无法继续执行3");
                return false;
            }
            if (CollUtil.isNotEmpty(pushModelids)) {
                this.sendPrivateDeliveryData("loadCallModelDataNode", pushModelids);
            }
            return true;
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    //异常时重跑
    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(),e);
        throw new RetryException(e.getMessage(), e);
    }
}
