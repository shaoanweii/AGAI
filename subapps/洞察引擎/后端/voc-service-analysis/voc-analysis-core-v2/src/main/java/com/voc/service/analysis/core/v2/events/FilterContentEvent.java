package com.voc.service.analysis.core.v2.events;

import cn.hutool.json.JSONObject;
import com.voc.service.analysis.core.v2.events.abstracts.AbstractEventNode;
import com.voc.service.analysis.core.v2.events.context.AnlysisEventContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;

import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName DistinctEvent
 * @createTime 2024年03月07日 11:41
 * @Copyright cuick
 * @Description //删除整条数据
 */
@LiteflowComponent(id = "R05", name = "过滤事件")
public class FilterContentEvent extends AbstractEventNode {


    @Override
    public void action_default(JSONObject data, String attrName, Set<String> values) {
        AnlysisEventContext context = this.getRequestData();
        context.getFinshData().setAbandon("1");
    }

    /**
     * 执行：
     */
    @Override
    public Boolean action_values(JSONObject data, String attrName, Set<String> value) {
//        log.warn("R04 过滤事件节点不支持内容");
//        return Boolean.FALSE;
        AnlysisEventContext context = this.getRequestData();
        context.getFinshData().setAbandon("1");
        return  Boolean.TRUE;
    }

    @Override
    public Boolean action_regexs(JSONObject data, String attrName, Set<String> values) {
//        log.warn("R04 过滤事件节点不支持正则表达式");
//        return Boolean.FALSE;
        AnlysisEventContext context = this.getRequestData();
        context.getFinshData().setAbandon("1");
        return  Boolean.TRUE;
    }

}
