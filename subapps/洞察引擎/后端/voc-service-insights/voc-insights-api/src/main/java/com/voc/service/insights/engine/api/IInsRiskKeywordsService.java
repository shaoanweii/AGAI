package com.voc.service.insights.engine.api;


import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.*;

import java.util.List;

public interface IInsRiskKeywordsService {


    PageInfo queryRisKeywordsList(InsRiskKeywordsQueryModel insRiskKeywordsQueryModel);


    List<InsRiskKeywordsModel> queryRiskList(InsRiskKeywordsQueryModel insRiskKeywordsQueryModel);


    Boolean addRisKeywords(AddRiskKeywordsModel riskKeywordsModel);

}
