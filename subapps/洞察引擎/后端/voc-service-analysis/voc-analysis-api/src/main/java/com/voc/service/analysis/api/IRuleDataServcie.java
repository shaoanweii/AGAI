package com.voc.service.analysis.api;

import com.voc.service.analysis.model.rule.ComputLogicModel;

import java.util.List;
import java.util.Set;

public interface IRuleDataServcie {

    List<ComputLogicModel> getRuleData(final String clientId, Set<String> validRuleIds);

    boolean setRuleStatusOk(final String workId,final String clientId);

    boolean setRuleStatusErr(final String workId,final String clientId);

    void removeCache();
}
