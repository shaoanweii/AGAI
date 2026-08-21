package com.voc.service.analysis.core.v2.nodes.valid;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.voc.service.analysis.api.IAysPostprocessValidDataService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@LiteflowComponent(id = "saveValidPostDataNode", name = "[校验]保存后置处理数据")
public class SaveValidPostDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(SaveValidPostDataNode.class);
    @Autowired
    IAysPostprocessValidDataService processDataService;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();

        final String workId = context.getWorkId();
        log.info("保存发送模型数据 workId:{}", workId);

        final Set<String> ids = context.getProcessData().stream().map(AysProcessDataModel::getDataId).collect(Collectors.toSet());
        if(CollUtil.isNotEmpty(ids)) {
            this.sendPrivateDeliveryData("modifyModelResltDataAnalysisValidStatusNode", ids);
        }
        //保存数据
        final List<AysProcessDataModel> aysPreprocessData = context.getProcessData();
        processDataService.saveBatch(context.getClientId(), workId, aysPreprocessData);

        log.info("保存发送模型数据完成");
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();

        Assert.isTrue(CollUtil.isNotEmpty(context.getProcessData()), "processData cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getChannelId cannot be empty");

        return true;
    }

}
