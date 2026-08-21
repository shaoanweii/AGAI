package com.voc.service.insights.engine.api;


import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.*;

public interface IInsHighFrequencyWordsService {


    PageInfo queryWordsList(InsBaseHighFrequencyQueryModel insHighFrequencyWordsQueryModel);

    InsWordsInfoModel queryWordsInfo(InsBaseHighFrequencyQueryModel insHighFrequencyWordsQueryModel);

    Boolean addHighFrequencyWords(AddHighFrequencyWordsModel wordsModel);

    Result<?> allocationWords(InsBaseTagInfoModel insBaseTagInfoModel);
}
