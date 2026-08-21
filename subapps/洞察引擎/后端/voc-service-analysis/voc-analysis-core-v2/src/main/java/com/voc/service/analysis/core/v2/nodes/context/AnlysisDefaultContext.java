package com.voc.service.analysis.core.v2.nodes.context;

import com.voc.service.analysis.largeModel.vo.ModelResponseVo;
import com.voc.service.analysis.largeModel.vo.NlpResult;
import com.voc.service.analysis.model.*;
import com.voc.service.analysis.model.rule.ComputLogicModel;
import com.voc.service.common.util.StopWatch;
import com.voc.service.insights.engine.model.AddHighFrequencyWordsModel;
import com.yomahub.liteflow.slot.DefaultContext;
import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Title: AnlysisPreContext
 * @Package: com.voc.service.analysis.core.v2.nodes.context
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:53
 * @Version:1.0
 */

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AnlysisDefaultContext extends DefaultContext implements Serializable {


    @Builder.Default
    StopWatch stopWatch = new StopWatch();

    String workId;

    String clientId;

    Integer modelType;
    @Builder.Default
    Integer modelCount = 0;
    @Builder.Default
    Integer retry = 0;

    @Builder.Default
    Set<String> channelIds = Collections.synchronizedSet(new HashSet<>());

    String contentType;

    @Builder.Default
    String analysisStatus = "0";

    List<AysProcessDataModel> processData;
    @Builder.Default
    List<ComputLogicModel> ruleList = new CopyOnWriteArrayList();

    AysValidDataModel validDataParam;

    ModelResponseVo modelResponseVo;
    @Builder.Default
    Set<String> ids = new HashSet<>();
    @Builder.Default
    Set<String> errorIds = new HashSet<>();

    //api 或 mq
    String workflowType;
    AddHighFrequencyWordsModel addHighFrequencyWordsModel;

    @Builder.Default
    Map<String, Integer> dataStatusMap = new HashMap<>();

    List<String> newIdList;

    @Builder.Default
    List<AysModelResltDataAnalysisMissModel> modelNotLabelDataList = new ArrayList<>();

    @Builder.Default
    List<AysModelResltDataAnalysisModel> modelLabelDataList = new ArrayList<>();

}
