package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.common.api.IUploadFileService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;

import java.util.HashMap;
import java.util.List;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "generatorateModelResultDataNode", name = "生成未完成写入结果表的数据JSON文件节点")
public class GeneratorateModelResultDataNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(GeneratorateModelResultDataNode.class);
    @Autowired
    IUploadFileService uploadFileService;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final String clientId = context.getClientId();
            log.info("加载静态数据 tag: {} ,clientId: {}", this.getTag(), clientId);

            List<AysProcessDataModel> list = context.getProcessData();
            if (CollUtil.isEmpty(list)) {
                log.info("加载静态数据 tag: {} ,clientId: {} ,size: {}", this.getTag(), clientId, list.size());
                return;
            }
            JSONArray jsonList = JSONUtil.createArray();
            list.forEach(item -> {
                // 将Java对象转为JSONObject
                JSONObject jsonObject = JSONUtil.parseObj(item.getData());
                HashMap<Object, Object> dataMap = MapUtil.newHashMap();
                // 遍历JSONObject，将空字符串值替换为null
                jsonObject.forEach((key, value) -> {
                    try {
                        if (value instanceof String && StrUtil.isBlank((String) value)) {
                            dataMap.put(key, null);
                        } else {
                            dataMap.put(key, value);
                        }
                    } catch (Exception e) {
                        log.error("转换数据为JSONObject失败 tag: {} ,clientId: {} ,data: {}", this.getTag(), clientId, item.getData());
                        jsonObject.set(key, value);
                    }
                });

                jsonList.add(JSONUtil.parse(dataMap, JSONConfig.create().setIgnoreNullValue(false)));
            });

            context.setData("modelResultData", jsonList);
            log.info("生成未完成写入结果表的数据JSON文件 tag: {} ,clientId: {} ,size: {}", this.getTag(), clientId, jsonList.size());

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
