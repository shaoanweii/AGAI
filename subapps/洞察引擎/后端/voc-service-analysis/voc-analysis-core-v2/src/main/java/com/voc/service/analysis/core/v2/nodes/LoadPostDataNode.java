package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "loadPostDataNode", name = "加载后置处理数据节点")
public class LoadPostDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadPostDataNode.class);
    @Autowired
    IAysModelResltAnalysisService modelResltAnalysisService;
    @Autowired
    AysConvertMapperService convertMapperService;
    @Autowired
    AnalysisConfig config;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final Set<String> ids = context.getIds();
            Assert.isTrue(CollUtil.isNotEmpty(ids), "ids cannot be empty");

            final List<AysModelResltDataAnalysisModel> list = modelResltAnalysisService.findByIds(context.getClientId(), ids);
            Assert.isTrue(ObjUtil.isNotNull(list), "findByWorkId list cannot be empty");

            AysModelResltDataAnalysisModel model = list.stream().findFirst().get();
            context.setWorkId(model.getWorkId());
            context.getChannelIds().add(model.getChannelId());
            context.setClientId(model.getClientId());
            context.setContentType(model.getContentType());

            List<AysProcessDataModel> rs = new CopyOnWriteArrayList<>();
            for (AysModelResltDataAnalysisModel data : list) {
                AysProcessDataModel cData = convertMapperService.converToAysProcessDataModel3(data);
                cData.setClientId(cData.getClientId());
                cData.setChannelId(cData.getChannelId());
                cData.setContentType(cData.getContentType());
                cData.setOriginalId(data.getOriginalId());
                cData.setWorkId(cData.getWorkId());
                cData.setData(JSONUtil.toJsonStr(data));
                cData.setModelType(data.getModelType());
                cData.setPublishTime(data.getPublishTime());
                cData.setOneId(data.getOneId());
                if(ObjectUtil.isNotNull(data.getExtFields())) {
                    cData.setExtFields(JSONUtil.parseObj(data.getExtFields()));
                }
                if(ObjectUtil.isNotNull(data.getBizExtAttrs())) {
                    cData.setBizExtAttrs(JSONUtil.parseObj(data.getBizExtAttrs()));
                }
                if(ObjectUtil.isNotNull(data.getBizExtAttrs2())) {
                    cData.setBizExtAttrs2(JSONUtil.parseObj(data.getBizExtAttrs2()));
                }
                if(ObjectUtil.isNotNull(data.getBizExtAttrs3())) {
                    cData.setBizExtAttrs3(JSONUtil.parseObj(data.getBizExtAttrs3()));
                }
                rs.add(cData);
            }

            context.setProcessData(rs);
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");
        Assert.isTrue(CollUtil.isNotEmpty(context.getIds()), "getIds cannot be empty");


        return true;
    }

}
