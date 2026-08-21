package com.voc.service.analysis.core.v2.events;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
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
 * @Description //删除内容中文字 - 动作 逻辑运算符[==、!= 、包含 、不包含]
 */
@LiteflowComponent(id = "R04", name = "清洗事件")
public class CleanContentEvent extends AbstractEventNode {
    private static final Logger logger = LoggerFactory.getLogger(CleanContentEvent.class);
    @Override
    public boolean isAccess() {
        return super.isAccess();
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/9/18 下午12:36
     * @描述
     * @param data 待清洗内容
     * @param attrName
     * @param values
     * @return void
     **/
    @Override
    public void action_default(JSONObject data, String attrName, Set<String> values) {
//        logger.warn("R04 清洗事件节点不支持默认处理");
        if(CollUtil.isEmpty(values)){
            logger.warn("待清洗值为空，不做任何处理");
        }else {
            String oldValue = data.get(attrName, String.class);
            if(StrUtil.isNotBlank(oldValue)){
                for (String value : values){
                    final String newValue = StrUtil.removeAll(oldValue, value);
                    logger.trace("newValue:{}", newValue);
                    //赋值
                    this.setContentItem(data, attrName, newValue);
                    oldValue = newValue;
                }
            }

        }

    }

    /**
     * 执行：
     */

    public Boolean action_value(JSONObject data, String attrName, String value) {
        final String oldValue = data.get(attrName, String.class);


        //数据为空时修改
        if (StrUtil.isNotBlank(oldValue)) {
            final String newValue = StrUtil.removeAll(oldValue, value);
            logger.trace("newValue:{}", newValue);
            //赋值
            return this.setContentItem(data, attrName, newValue);
        }
        return Boolean.FALSE;
    }

    /**
     * 执行：内容中包含了集合内的值，删除内容中文字
     *
     * @param data
     * @param attrName
     * @param values
     */
    @Override
    public Boolean action_values(JSONObject data, String attrName, Set<String> values) {
        Boolean flag = Boolean.FALSE;
        for (String value : values) {
            flag = this.action_value(data, attrName, value);
        }
        return flag;
    }

    @Override
    public Boolean action_regexs(JSONObject data, String attrName, Set<String> values) {
        final String oldValue = data.get(attrName, String.class);
        //数据为空时修改
        Boolean flag = Boolean.FALSE;
        if (StrUtil.isNotBlank(oldValue)) {
            for (String value : values) {
                final String newValue = this.regularProcess(value, oldValue);
                logger.trace("newValue:{}", newValue);
                //赋值
                flag = this.setContentItem(data, attrName, newValue);
            }
            return flag;
        }
        return false;
    }

    @Override
    public String regularProcess(final String regex, final CharSequence content) {
//        StringBuilder sb = new StringBuilder(content);
        // 从content中提取匹配regex的第一个子串，并赋值给str
        final String str = ReUtil.delAll(regex, content);
        return str;
    }


}
