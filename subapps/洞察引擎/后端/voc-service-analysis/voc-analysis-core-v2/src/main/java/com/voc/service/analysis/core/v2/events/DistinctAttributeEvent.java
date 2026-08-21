package com.voc.service.analysis.core.v2.events;

import cn.hutool.json.JSONObject;
import com.voc.service.analysis.core.v2.events.abstracts.AbstractEventNode;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName DistinctEvent
 * @createTime 2024年03月07日 11:41
 * @Copyright cuick
 * @Description 去重
 */

@LiteflowComponent(id = "R10", name = "去重事件")
public class DistinctAttributeEvent extends AbstractEventNode {


    private static final Logger log = LoggerFactory.getLogger(DistinctAttributeEvent.class);

    @Override
    public void action_default(JSONObject data, String attrName, Set<String> values) {
        log.warn("R10 去重事件节点不支持默认处理");
    }

    /**
     * 执行：
     */
    @Override
    public Boolean action_values(JSONObject data, String attrName, Set<String> value) {
        log.warn("R10 去重事件节点不支持内容处理");
        return Boolean.FALSE;
    }

    @Override
    public Boolean action_regexs(JSONObject data, String attrName, Set<String> values) {
        log.warn("R10 去重事件节点不支持正则表达式");
        return Boolean.FALSE;
    }


}
