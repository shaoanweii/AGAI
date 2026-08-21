package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.api.IStaticDataServcie;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.utils.AnlysisContextHolder;
import com.voc.service.analysis.model.AysAnalFlowModelTagsResultDataExtModel;
import com.voc.service.analysis.model.AysMetaDataAnalysisModel;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;
import org.springframework.util.Assert;

import java.util.*;
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
@LiteflowComponent(id = "loadModelResultDataNode", name = "加载未完成写入结果表的数据节点")
public class LoadModelResultDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadModelResultDataNode.class);
    @Autowired
    IAysModelResltAnalysisService iAysModelResltAnalysisService;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final String clientId = context.getClientId();
            log.info("加载静态数据 tag: {} ,clientId: {}", this.getTag(),clientId);

            Set<String> ids = context.getIds();
            if (Objects.isNull(ids)) {
                log.warn("没有读取数据到源数据，无法继续执行2");
                return;
            }

            List<AysAnalFlowModelTagsResultDataExtModel> list = iAysModelResltAnalysisService.finchResultData(clientId, ids);
            if (Objects.isNull(list)){
                log.warn("没有读取数据到源数据，无法继续执行");
                return;
            }

            List<AysProcessDataModel> rs = new CopyOnWriteArrayList<>();
            for (AysAnalFlowModelTagsResultDataExtModel data : list) {
                AysProcessDataModel cData = new AysProcessDataModel();
                cData.setClientId(context.getClientId());
                cData.setChannelId(data.getChannelCode());
                cData.setContentType(data.getContentType());
                cData.setWorkId(cData.getWorkId());
                cData.setData(JSONUtil.toJsonStr(data));
                rs.add(cData);
            }

            context.setProcessData(rs);
            log.info("加载静态数据 tag: {} ,clientId: {} ,size: {}", this.getTag(),clientId,list.size());
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
//        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }
}
