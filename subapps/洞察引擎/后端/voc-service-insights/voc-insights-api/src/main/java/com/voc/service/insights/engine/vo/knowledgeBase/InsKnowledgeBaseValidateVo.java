package com.voc.service.insights.engine.vo.knowledgeBase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsKnowledgeBaseValidateVo implements Serializable {
    private String message;
    private String inputBatchId;
    private String id;
    private String fileName;
    private String success;
    private String total;
}
