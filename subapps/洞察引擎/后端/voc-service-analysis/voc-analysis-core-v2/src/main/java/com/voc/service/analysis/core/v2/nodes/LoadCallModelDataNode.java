package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysPreprocessDataService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysPreprocessDataModel;
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
@LiteflowComponent(id = "loadCallModelDataNode", name = "加载模型处理数据节点")
public class LoadCallModelDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadCallModelDataNode.class);
    @Autowired
    IAysPreprocessDataService preprocessDataService;

    @Override
    public void process() throws Exception {
        try {
            AnlysisDefaultContext context = this.getRequestData();

            final Set<String> ids = this.getPrivateDeliveryData();
            Assert.isTrue(CollUtil.isNotEmpty(ids), "ids cannot be empty");
            final List<AysPreprocessDataModel> list = preprocessDataService.findByIds(context.getClientId(), ids);
            if (CollUtil.isEmpty(list)) {
                return;
            }
//            log.info("查出数据的集合:{}", list);
            log.info("查出数据的集合:{}", list.size());
            AysPreprocessDataModel model = list.stream().findFirst().get();
            context.setWorkId(model.getWorkId());
            List<AysProcessDataModel> rs = new CopyOnWriteArrayList<>();
            for (AysPreprocessDataModel data : list) {
                AysProcessDataModel cData = new AysProcessDataModel();
                BeanUtil.copyProperties(data, cData);
                cData.setClientId(cData.getClientId());
                cData.setChannelId(cData.getChannelId());
                cData.setOriginalId(data.getDataId());
                cData.setContentType(cData.getContentType());
                cData.setModelType(data.getModelType());
                cData.setWorkId(cData.getWorkId());
                cData.setPublishTime(data.getPublishTime());
                cData.setData(data.getData());
                cData.setOneId(data.getOneId());
                if (ObjectUtil.isNotNull(data.getExtFields())) {
                    cData.setExtFields(JSONUtil.parseObj(data.getExtFields()));
                }
                if (ObjectUtil.isNotNull(data.getBizExtAttrs())) {
                    cData.setBizExtAttrs(JSONUtil.parseObj(data.getBizExtAttrs()));
                }
                if (ObjectUtil.isNotNull(data.getBizExtAttrs2())) {
                    cData.setBizExtAttrs2(JSONUtil.parseObj(data.getBizExtAttrs2()));
                }
                if (ObjectUtil.isNotNull(data.getBizExtAttrs3())) {
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
        return true;
    }

    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(), e);
        throw new RetryException(e.getMessage(), e);
    }

}
