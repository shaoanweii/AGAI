package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.producers.kafka.ProcessPreRulesProducer;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName StoreSourceDataNode
 * @createTime 2024年03月07日 10:49
 * @Copyright cuick
 */
@LiteflowComponent(id = "savePreDataNode", name = "保存前置处理数据")
public class SavePreDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(SavePreDataNode.class);
    @Autowired
    IAysPreprocessDataService processDataService;
    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        Set<String> insertIds = null;
        try {
            final String workId = context.getWorkId();
            log.info("保存发送模型数据 workId:{}", workId);

            final Set<String> ids = context.getProcessData().stream().map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
            if(CollUtil.isNotEmpty(ids)) {
                this.sendPrivateDeliveryData("modifyMetaDataAnalysisStatusNode", ids);
            }
            final Set<String> ids0 = context.getProcessData().stream()
                    .filter(model -> "0".equalsIgnoreCase(model.getAbandon()))
                    .map(AysProcessDataModel::getDataId)
                    .collect(Collectors.toSet());
            log.debug("processData ids0:{}", ids0);
            final Set<String> ids1= context.getProcessData().stream()
                    .filter(model -> "1".equalsIgnoreCase(model.getAbandon()))
                    .map(AysProcessDataModel::getDataId)
                    .collect(Collectors.toSet());
            log.debug("processData ids1:{}", ids1);

            //保存数据
            final List<AysProcessDataModel> aysPreprocessData = context.getProcessData();

            insertIds = processDataService.saveBatch(context.getClientId(), aysPreprocessData);
            final Set<String> ids0_ = context.getProcessData().stream()
                    .filter(model -> "0".equalsIgnoreCase(model.getAbandon()))
                    .map(AysProcessDataModel::getDataId)
                    .collect(Collectors.toSet());
            log.debug("processData ids0:{}", ids0_);
            final Set<String> ids1_= context.getProcessData().stream()
                    .filter(model -> "1".equalsIgnoreCase(model.getAbandon()))
                    .map(AysProcessDataModel::getDataId)
                    .collect(Collectors.toSet());
            log.debug("processData ids1:{}", ids1_);

            log.info("保存发送模型数据完成");
        }catch (Exception e){
            log.error("保存发送模型数据失败", e);
//            long count = processDataService.remove(context.getClientId(), insertIds);
            log.info("insertIds:{} deleteIds{}", insertIds.size());
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();

        Assert.isTrue(CollUtil.isNotEmpty(context.getProcessData()), "processData cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }



}
