package com.voc.service.analysis.core.v2.events;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.analysis.core.v2.events.abstracts.AbstractEventNode;
import com.voc.service.common.exception.BussinessException;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName DistinctEvent
 * @createTime 2024年03月07日 11:41
 * @Copyright cuick
 * @Description //满足正则表达式是用 * 号代替 - 动作 逻辑运算符[包含]
 */
@LiteflowComponent(id = "R02", name = "脱敏事件")
public class DesensitizationContentEvent extends AbstractEventNode {


    private static final Logger log = LoggerFactory.getLogger(DesensitizationContentEvent.class);

    @Override
    public void action_default(JSONObject data, String attrName, Set<String> values) {
        log.warn("R02 脱敏事件节点不支持默认处理");
    }

    /**
     * 执行：
     */
    @Override
    public Boolean action_values(JSONObject data, String attrName, Set<String> value) {
        log.warn("R02 脱敏事件节点不支持内容处理");
        return Boolean.FALSE;
    }

    @Override
    public Boolean action_regexs(JSONObject data, String attrName, Set<String> values) {
        Boolean flag = Boolean.FALSE;
        for (String value : values) {
            final String oldValue = data.get(attrName, String.class);
            final String newValue = regularProcess(value, oldValue);
            log.trace("newValue:{}", newValue);
            //赋值
            flag = this.setContentItem(data, attrName, newValue);
        }
        return flag;
    }

    /**
     * 正则处理
     * 1、*号个数为n/匹配字符长度
     *
     * @param regex
     * @param content
     * @return
     */
    @Override
    public String regularProcess(String regex, CharSequence content) {
        final String str = ReUtil.get(regex, content, 0);
        final String f = IntStream.range(0, StrUtil.isNotEmpty(str) ? str.length() : 0).mapToObj(i -> "*").collect(Collectors.joining());
        return StrUtil.replace(content, str, f);
    }

    @Override
    public boolean isAccess() {
        boolean sup = super.isAccess();
        if (!sup) {
            return false;
        }
        //修改事件节点必须指定结果集
        if (CollUtil.isEmpty(this.getComputLogicModel().getResultData())) {
            throw new BussinessException("脱敏事件节点必须指定结果集 ".concat(JSONUtil.toJsonStr(this.computLogicModel)));
        }

        return true;
    }
}
