package com.voc.service.insights.engine.api;


import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.*;

public interface IInsHighFrequencyOpinionsService {


    PageInfo queryOpinionsList(InsBaseHighFrequencyQueryModel insBaseHighFrequencyQueryModel);

    InsOpinionsInfoModel queryOpinionsInfo(InsBaseHighFrequencyQueryModel insHighFrequencyWordsQueryModel);

    Boolean addHighFrequencyOpinion(AddHighFrequencyOpinionModel opinionModel);

    Result<?> allocationOpinions(InsBaseTagInfoModel insBaseTagInfoModel);
}
