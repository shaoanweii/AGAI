package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysMetaDataAnalysisService;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysMetaDataAnalysisModel;
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
@LiteflowComponent(id = "loadPreDataNode", name = "加载前置处理数据节点")
public class LoadPreDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadPreDataNode.class);
    @Autowired
    IAysMetaDataAnalysisService metaDataAnalysisService;
    @Autowired
    AysConvertMapperService convertMapperService;

    @Override
    public void process() throws RetryException {
        try {
            log.info("exec");
            AnlysisDefaultContext context = this.getRequestData();
            final Set<String> ids = this.getPrivateDeliveryData();
            Assert.isTrue(CollUtil.isNotEmpty(ids), "getPrivateDeliveryData  cannot be empty");
            final List<AysMetaDataAnalysisModel> list = metaDataAnalysisService.findByIds(context.getClientId(), ids);
            if (CollUtil.isEmpty(ids)) {
                log.debug("findByIds is null");
                return;
            }

            AysMetaDataAnalysisModel model = list.stream().findFirst().get();
            context.setWorkId(model.getWorkId());
            context.getChannelIds().add(model.getChannelId());
            context.setClientId(model.getClientId());
            context.setContentType(model.getContentType());

            List<AysProcessDataModel> rs = new CopyOnWriteArrayList<>();
            for (AysMetaDataAnalysisModel data : list) {
                AysProcessDataModel cData = new AysProcessDataModel();
                BeanUtil.copyProperties(data,cData);
                cData.setClientId(cData.getClientId());
                cData.setChannelId(cData.getChannelId());
                cData.setContentType(cData.getContentType());
                cData.setWorkId(cData.getWorkId());
                cData.setData(data.getData());
                cData.setModelType(data.getModelType());
                cData.setPublishTime(data.getPublishTime());
                cData.setDataMd5(JSONUtil.toJsonStr(metaDataAnalysisService.getMD5Values(data)));
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
                if(ObjectUtil.isNotNull(data.getCustExtAttrs())) {
                    cData.setCustExtAttrs(JSONUtil.parseObj(data.getCustExtAttrs()));
                }
                if (ObjectUtil.isNotNull(data.getVhlExtAttrs())){
                    cData.setVhlExtAttrs(JSONUtil.parseObj(data.getVhlExtAttrs()));
                }
                if (ObjectUtil.isNotNull(data.getPrdExtAttrs())){
                    cData.setPrdExtAttrs(JSONUtil.parseObj(data.getPrdExtAttrs()));
                }
                if (ObjectUtil.isNotNull(data.getDealerExtAttrs())){
                    cData.setDealerExtAttrs(JSONUtil.parseObj(data.getDealerExtAttrs()));
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
        if (CollUtil.isEmpty(context.getIds())) {
            return false;
        }

        return true;
    }


}
