package com.voc.service.analysis.risk.utils;

import com.alibaba.fastjson.JSON;
import com.voc.service.risk.api.model.BatchTaskConditionsModel;

import java.util.*;

public class TopRankParser {

    /**
     * 解析 JSON 里的 topRank 规则
     * 返回：currentWhere / previousWhere
     */
    public static void parseTopRank(String jsonConfig, BatchTaskConditionsModel batchTaskConditionsModel) {
        List<String> current = new ArrayList<>();
        List<String> previous = new ArrayList<>();
        Map<String, Object> config = JSON.parseObject(jsonConfig);
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) config.get("conditions");
        for (Map<String, Object> cond : conditions) {
            if (!"topRank".equals(cond.get("indicator"))) {
                continue;
            }

            String valueType = (String) cond.get("value_type");
            String operator = (String) cond.get("operator");
            Object value = cond.get("value");

            String expr = "topRank " + convertOp(operator) + " " + value;

            if ("current_value".equals(valueType)) {
                current.add(expr);
            } else if ("previous_value".equals(valueType)) {
                previous.add(expr);
            }
        }
        batchTaskConditionsModel.setTopCurrentWhere(String.join(" AND ", current));
        batchTaskConditionsModel.setTopPreviousWhere(String.join(" AND ", previous));
    }

    private static String convertOp(String op) {
        return switch (op) {
            case "gt" -> ">";
            case "lt" -> "<";
            case "gte" -> ">=";
            case "lte" -> "<=";
            case "eq" -> "=";
            case "ne" -> "!=";
            default -> op;
        };
    }
}