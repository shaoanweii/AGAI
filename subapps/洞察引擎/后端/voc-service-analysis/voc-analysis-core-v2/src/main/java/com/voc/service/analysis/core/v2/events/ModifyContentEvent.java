package com.voc.service.analysis.core.v2.events;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
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
 * @Description //直接修改 - 动作 逻辑运算符[==]
 */
@LiteflowComponent(id = "R01", name = "修改事件")
public class ModifyContentEvent extends AbstractEventNode {


    private static final Logger log = LoggerFactory.getLogger(ModifyContentEvent.class);

    @Override
    public void action_default(JSONObject data, String attrName, Set<String> values) {
        log.warn("R01 修改事件节点不支持默认值");
    }

    /**
     * 执行：
     */
    @Override
    public Boolean action_values(JSONObject data, String attrName, Set<String> values) {
        log.trace("newValue:{}", values);
        //赋值
        Boolean flag = Boolean.FALSE;
        for (String value : values) {
            flag = this.setContentItem(data, attrName, value);
        }
        return flag;
    }

    @Override
    public Boolean action_regexs(JSONObject data, String attrName, Set<String> values) {

        Boolean flag = Boolean.FALSE;
        for (String value : values) {
            final String attrValue = this.getContentItem(attrName);
            //正则处理
            final String newValue = this.regularProcess(value, attrValue);
            //赋值
            flag = this.setContentItem(data, attrName, newValue);
        }
        return flag;
    }

    @Override
    public boolean isAccess() {
        boolean sup = super.isAccess();
        //修改事件节点必须指定结果集
        if (!sup || CollUtil.isEmpty(this.getComputLogicModel().getResultData())) {
            log.info(StrUtil.format("修改事件节点必须指定结果集 {}", this.getComputLogicModel()));
            return false;
        }

        return true;
    }
}
