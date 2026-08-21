package com.voc.service.analysis.core.v2.nodes.valid;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysMetaDataAnalysisModel;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@LiteflowComponent(id = "loadValidPostDataNode", name = "[校验]加载后置处理数据节点")
public class LoadValidPostDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(LoadValidPostDataNode.class);
    @Autowired
    IAysModelResltAnalysisService modelResltAnalysisService;
    @Autowired
    AysConvertMapperService convertMapperService;
    @Autowired
    AnalysisConfig config;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        final Set<String> ids = this.getPrivateDeliveryData();
        Assert.isTrue(CollUtil.isNotEmpty(ids), "ids cannot be empty");

        final List<AysModelResltDataAnalysisModel> list = modelResltAnalysisService.findByIds(context.getClientId(),ids);
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
            cData.setWorkId(cData.getWorkId());
            cData.setData(JSONUtil.toJsonStr(data));
            if(ObjectUtil.isNotNull(data.getExtFields())) {
                cData.setExtFields(JSONUtil.parseObj(data.getExtFields()));
            }
//            cData.setDataMd5(JSONUtil.toJsonStr(this.getMD5Values(data)));

            rs.add(cData);
        }

        context.setProcessData(rs);
    }

    @Override
    public boolean isAccess() {

        return true;
    }

    private Map<String, String> getMD5Values(AysMetaDataAnalysisModel data) {
        Map<String, String> contentMD5 = new HashMap<>();
        JSONObject obj = JSONUtil.parseObj(data.getData());

        if (ObjectUtil.isNotEmpty(obj)) {
            Object content = obj.get("content");
            if (ObjectUtil.isNotEmpty(content)) {
                contentMD5.put("content", DigestUtil.md5Hex(StrUtil.trim(String.valueOf(content))));
            }
        }
        return contentMD5;
    }
}
