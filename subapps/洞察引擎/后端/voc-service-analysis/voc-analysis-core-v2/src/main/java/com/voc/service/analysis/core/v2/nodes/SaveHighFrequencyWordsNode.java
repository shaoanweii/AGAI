package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.voc.service.analysis.api.IAddHighFrequencyWordsService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.insights.engine.model.AddHighFrequencyWordsModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Title: SaveHighFrequencyWordsNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: liuhb
 * @Date: 2024/7/23 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "saveHighFrequencyWordsNode", name = "保存高频热词数据节点")
public class SaveHighFrequencyWordsNode extends AbstractNode {


    private static final Logger log = LoggerFactory.getLogger(SaveHighFrequencyWordsNode.class);
    @Autowired
    IAddHighFrequencyWordsService iAddHighFrequencyWordsService;

    @Override
    public void process() throws Exception {
        AnlysisDefaultContext context = this.getRequestData();
        AddHighFrequencyWordsModel addHighFrequencyWordsModel = context.getAddHighFrequencyWordsModel();
        log.info("高频热词数据:{}", addHighFrequencyWordsModel);
        if (ObjectUtil.isNotEmpty(addHighFrequencyWordsModel) && CollectionUtil.isNotEmpty(addHighFrequencyWordsModel.getAddWordsInfoModelList())) {
          //  iAddHighFrequencyWordsService.addHighFrequencyWords(addHighFrequencyWordsModel);
        }
    }
}
