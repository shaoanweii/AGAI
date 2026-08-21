package com.voc.service.analysis.core.v2.impl;

import com.alibaba.fastjson.JSON;
import com.voc.service.analysis.api.IAddHighFrequencyWordsService;
import com.voc.service.analysis.clients.IInsHighFrequencyWordsClient;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.model.AddHighFrequencyWordsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName RuleDataServcieImpl
 * @createTime 2024年03月12日 18:18
 * @Copyright cuick
 */

@Service
public class AddHighFrequencyWordsServiceImpl implements IAddHighFrequencyWordsService {

    private static final Logger log = LoggerFactory.getLogger(AddHighFrequencyWordsServiceImpl.class);
    @Autowired
    IInsHighFrequencyWordsClient iInsHighFrequencyWordsClient;
    @Autowired
    AnalysisConfig config;

    /**
     * 词汇新增
     *
     * @param wordsModel
     */
    @Override
    public void addHighFrequencyWords(AddHighFrequencyWordsModel wordsModel) {
        ServiceContextHolder.setToken(config.getDefaultToken());
        try {
            log.info("通知洞察引擎词汇入参:{}", JSON.toJSONString(wordsModel));
            Result<Boolean> booleanResult = iInsHighFrequencyWordsClient.addHighFrequencyWordsClient(wordsModel);
            log.info("通知洞察引擎词汇返回:{}", booleanResult);
        } catch (Exception e) {
            log.error("通知洞察引擎词汇失败:", e);
        }
    }
}
