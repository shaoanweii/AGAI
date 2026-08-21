package com.voc.service.analysis.largeModel.vo;


import com.voc.service.analysis.largeModel.ModelTopicRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
public class ModelResponseVo implements Serializable {

    private ModelTopicRequest nlpParam;
    private NlpResult nlpResult;
}
