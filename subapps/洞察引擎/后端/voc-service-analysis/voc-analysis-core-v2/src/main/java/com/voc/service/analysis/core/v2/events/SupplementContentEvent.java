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
 * @Description 补充 //数据为空时修改  - 动作 逻辑运算符[==]
 */
@LiteflowComponent(id = "R03", name = "补充事件")
public class SupplementContentEvent extends AbstractEventNode {


    private static final Logger log = LoggerFactory.getLogger(SupplementContentEvent.class);

    @Override
    public void action_default(JSONObject data, String attrName, Set<String> values) {
        log.warn("R03 补充事件节点不支持默认值");
    }

    /**
     * 执行：
     */
    @Override
    public Boolean action_values(JSONObject data, String attrName, Set<String> values) {
        final String oldValue = data.get(attrName, String.class);
        //数据为空时修改
        Boolean flag = Boolean.FALSE;
        if (StrUtil.isBlank(oldValue)) {
            log.trace("newValue:{}", values);
            //赋值
            for (String value : values) {
                flag = this.setContentItem(data, attrName, value);
            }
            return flag;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean action_regexs(JSONObject data, String attrName, Set<String> values) {
        log.warn("R03 补充事件节点不支持正则表达式");
        return Boolean.FALSE;
    }

    @Override
    public boolean isAccess() {
        boolean sup = super.isAccess();
        //补充事件节点必须指定结果集
        if (!sup || CollUtil.isEmpty(this.getComputLogicModel().getResultData())) {
            log.info(StrUtil.format("补充事件节点必须指定结果集 {}", this.getComputLogicModel()));
            return false;
        }
        return true;
    }
}
