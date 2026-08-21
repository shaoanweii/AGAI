package com.voc.service.trhird.api;


import com.voc.service.trhird.model.AIRequestModel;
import com.voc.service.trhird.vo.AIResponseVo;

/**
 * 定义了LLM 复合模型AI接口
 *
 */
public interface CompoundLlmAiApi {

    AIResponseVo generateAiResponse(AIRequestModel prompt);

}
