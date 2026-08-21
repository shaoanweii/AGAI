package com.voc.service.trhird.zhipuai.impl;

import cn.hutool.json.JSONUtil;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.voc.service.trhird.api.CompoundLlmAiApi;
import com.voc.service.trhird.model.AIRequestModel;
import com.voc.service.trhird.vo.AIResponseVo;
import com.voc.service.trhird.zhipuai.config.CompoundModelConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CompoundLlmAiApiImpl implements CompoundLlmAiApi {
    @Value("${third.compound.llm.apiUrl}")
    private String apiUrl;
    @Value("${third.compound.llm.modelName}")
    private String modelName;

    @Autowired
    private CompoundModelConfig config;

    @Autowired
    RestTemplate restTemplate;

    private static final String URL_SUFFIX = "/chat/completions";

    @Override
    public AIResponseVo generateAiResponse(AIRequestModel model) {
        Map<String, Object> models = config.getCompoundModels();
        Map<String, String> modelConfig = (Map<String, String>) models.get(model.getModel_name());
        List<Map<String, String>> prompt = config.getCompoundPrompt();
        List<ChatMessage> chatMessages = new ArrayList<>();
        prompt.forEach(s -> {
            ChatMessage chatMessage = new ChatMessage(s.get("role"), s.get("content"));
            chatMessages.add(chatMessage);
        });
        chatMessages.add(new ChatMessage("user", "<TEXT>" + model.getText() + "</TEXT>"));

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setBearerAuth(modelConfig.get("api_key"));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model(modelConfig.get("version"))
                .messages(chatMessages)
                .build();
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONUtil.toJsonStr(completionRequest), requestHeaders);
        ChatCompletionResult result = restTemplate.postForEntity(modelConfig.get("base_url") + URL_SUFFIX, requestEntity, ChatCompletionResult.class).getBody();

        if (result == null || result.getUsage() == null || CollectionUtils.isEmpty(result.getChoices())) {
            AIResponseVo response = new AIResponseVo();
            response.setStatus("error");
            response.setErrorMessage("模型调用失败");
            return response;
        }
        double inputCostPer1kTokens = Double.parseDouble(modelConfig.getOrDefault("input_cost_per_1k_tokens", "0.001"));
        double outputCostPer1kTokens = Double.parseDouble(modelConfig.getOrDefault("output_cost_per_1k_tokens", "0.002"));

        Long totalTokens = result.getUsage().getTotalTokens();
        long promptTokens = result.getUsage().getPromptTokens();
        long completionTokens = result.getUsage().getCompletionTokens();

        double inputCost = promptTokens / 1000.0 * inputCostPer1kTokens;
        double outputCost = completionTokens / 1000.0 * outputCostPer1kTokens;
        double totalCost = inputCost + outputCost;

        AIResponseVo response = new AIResponseVo();
        response.setResult(result.getChoices().get(0).getMessage().getContent());
        response.setModelName(model.getModel_name());
        response.setUniqueId(model.getUnique_id());
        response.setStatus("success");
        response.setCost(totalCost);
        response.setPromptTokens(promptTokens);
        response.setCompletionTokens(completionTokens);
        response.setTotalTokens(totalTokens);
        return response;
    }
}
