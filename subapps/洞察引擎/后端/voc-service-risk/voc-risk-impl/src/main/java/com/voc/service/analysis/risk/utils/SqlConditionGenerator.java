package com.voc.service.analysis.risk.utils;

import com.alibaba.fastjson.JSON;
import com.voc.service.risk.api.model.BatchTaskConditionsModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqlConditionGenerator {

    private static final Map<String, String> OPERATOR_MAP = Map.of(
            "gt", ">",
            "lt", "<",
            "eq", "=",
            "gte", ">=",
            "lte", "<=",
            "ne", "!="
    );

    // 三个【仅 current_value 直接比较】的指标
    private static final Set<String> DIRECT_VALUE_INDICATORS = Set.of(
            "mention_count",
            "user_count",
            "effective_voice_count",
            "negative_rate"
    );

    // ===================== 字段映射 =====================
    private static final Map<String, String> VALUE_CURRENT = Map.of(
            "mention_count", "curr_total",
            "user_count", "curr_usercount",
            "effective_voice_count", "curr_voiceCount",
            "negative_rate", "curr_negativeRate"
    );
    private static final Map<String, String> VALUE_PREVIOUS = Map.of(
            "mention_count", "prev_total",
            "user_count", "prev_usercount",
            "effective_voice_count", "prev_voiceCount",
            "negative_rate", "prev_negativeRate"
    );
    private static final Map<String, String> YOY_CURRENT = Map.of(
            "mention_count", "curr_total_yoy",
            "user_count", "curr_user_yoy",
            "effective_voice_count", "curr_voice_yoy",
            "negative_rate", "curr_neg_yoy"
    );
    private static final Map<String, String> YOY_PREVIOUS = Map.of(
            "mention_count", "prev_total_yoy",
            "user_count", "prev_user_yoy",
            "effective_voice_count", "prev_voice_yoy",
            "negative_rate", "prev_neg_yoy"
    );
    private static final Map<String, String> MOM_CURRENT = Map.of(
            "mention_count", "curr_total_mom",
            "user_count", "curr_user_mom",
            "effective_voice_count", "curr_voice_mom",
            "negative_rate", "curr_neg_mom"
    );
    private static final Map<String, String> MOM_PREVIOUS = Map.of(
            "mention_count", "prev_total_mom",
            "user_count", "prev_user_mom",
            "effective_voice_count", "prev_voice_mom",
            "negative_rate", "prev_neg_mom"
    );

    // ===================== 全局年均值 =====================
    private static final Map<String, String> YEAR_AVG = Map.of(
            "mention_count", "year_total_avg",
            "user_count", "year_user_avg",
            "effective_voice_count", "year_voice_avg",
            "negative_rate", "year_neg_avg"
    );
    // ===================== 全局月均值 =====================
    private static final Map<String, String> MONTH_AVG = Map.of(
            "mention_count", "month_total_avg",
            "user_count", "month_user_avg",
            "effective_voice_count", "month_voice_avg",
            "negative_rate", "month_neg_avg"
    );
    // ===================== 本期动态均值（日/周/月） =====================
    private static final Map<String, String> CURR_AVG = Map.of(
            "mention_count", "curr_total_avg",
            "user_count", "curr_usercount_avg",
            "effective_voice_count", "curr_voiceCount_avg",
            "negative_rate", "curr_negativeRate_avg"
    );

    public static String buildWhereClause(String jsonConfig,BatchTaskConditionsModel batchTaskConditionsModel) {
        Map<String, Object> config = JSON.parseObject(jsonConfig);
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) config.get("conditions");
        String logic = (String) config.get("logic_operator");
        List<String> list = new ArrayList<>();
        batchTaskConditionsModel.setLogicOperator( logic);
        for (Map<String, Object> cond : conditions) {
            if ("topRank".equals(cond.get("indicator"))) {
                continue;
            }
            String indicator = (String) cond.get("indicator");
            String indicatorType = (String) cond.get("indicator_type");
            String operator = (String) cond.get("operator");
            String valueType = (String) cond.get("value_type");
            double value = ((Number) cond.get("value")).doubleValue();

            list.add(buildCondition(indicator, indicatorType, operator, valueType, value));
        }

        return String.join(" " + logic + " ", list);
    }

    private static String buildCondition(
            String indicator,
            String indicatorType,
            String operator,
            String valueType,
            double value
    ) {
        String op = OPERATOR_MAP.get(operator);
        String leftField = getLeftField(indicator, indicatorType, valueType);
        String rightField = getRightField(indicator, indicatorType, valueType);

        // 规则：只有 3 个指标 + value + current_value → 直接数字比较
        if (DIRECT_VALUE_INDICATORS.contains(indicator)
                && "current_value".equals(valueType)) {
            return leftField + " " + op + " " + value;
        }

        // 其他所有情况：百分比比较
        return String.format("%s %s (%s * %s / 100)", leftField, op, rightField, value);
    }

    // ===================== 核心：左边字段（均值保持不变，其他按 indicatorType） =====================
    private static String getLeftField(String indicator, String indicatorType, String valueType) {
        // 均值场景：保持原来正确逻辑
        if ("year_average".equals(valueType) || "month_average".equals(valueType)) {
            return CURR_AVG.get(indicator);
        }
        // 非均值：value / mom / yoy 取本期
        return switch (indicatorType) {
            case "value" -> VALUE_CURRENT.get(indicator);
            case "mom" -> MOM_CURRENT.get(indicator);
            case "yoy" -> YOY_CURRENT.get(indicator);
            default -> throw new RuntimeException("不支持 indicator_type: " + indicatorType);
        };
    }

    // ===================== 核心：右边基准字段 =====================
    private static String getRightField(String indicator, String indicatorType, String valueType) {
        return switch (valueType) {
            case "current_value" -> getCurrentByType(indicator, indicatorType);
            case "previous_value" -> getPreviousByType(indicator, indicatorType);
            case "year_average" -> YEAR_AVG.get(indicator);
            case "month_average" -> MONTH_AVG.get(indicator);
            default -> throw new RuntimeException("不支持 value_type: " + valueType);
        };
    }

    private static String getCurrentByType(String indicator, String indicatorType) {
        return switch (indicatorType) {
            case "value" -> VALUE_CURRENT.get(indicator);
            case "mom" -> MOM_CURRENT.get(indicator);
            case "yoy" -> YOY_CURRENT.get(indicator);
            default -> throw new RuntimeException("不支持 indicator_type: " + indicatorType);
        };
    }

    private static String getPreviousByType(String indicator, String indicatorType) {
        return switch (indicatorType) {
            case "value" -> VALUE_PREVIOUS.get(indicator);
            case "mom" -> MOM_PREVIOUS.get(indicator);
            case "yoy" -> YOY_PREVIOUS.get(indicator);
            default -> throw new RuntimeException("不支持 indicator_type: " + indicatorType);
        };
    }

    // ===================== 你最新的完整 JSON MAIN 方法 =====================
    public static void main(String[] args) {
        String jsonConfig = "{\n" +
                "    \"logic_operator\": \"OR\",\n" +
                "    \"conditions\": [\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"lt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 18,\n" +
                "            \"unit\": \"正整数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"lt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 73,\n" +
                "            \"unit\": \"正整数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 2.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"month_average\",\n" +
                "            \"value\": 2.3,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"year_average\",\n" +
                "            \"value\": 2.4,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 3.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"month_average\",\n" +
                "            \"value\": 3.2,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"year_average\",\n" +
                "            \"value\": 3.3,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 4.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"month_average\",\n" +
                "            \"value\": 4.2,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"year_average\",\n" +
                "            \"value\": 4.3,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 5.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"month_average\",\n" +
                "            \"value\": 5.2,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"year_average\",\n" +
                "            \"value\": 5.3,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 6.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 6.2,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 7.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 7.2,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 8.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 8.2,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 9.1,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"previous_value\",\n" +
                "            \"value\": 9.2,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 10,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 20,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 30,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 40,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 50,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 60,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"mom\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 70,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 80,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"user_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"lt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 90,\n" +
                "            \"unit\": \"正整数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 100,\n" +
                "            \"unit\": \"正整数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"negative_rate\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 110,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"mention_count\",\n" +
                "            \"indicator_type\": \"value\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 60,\n" +
                "            \"unit\": \"正整数\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"indicator\": \"effective_voice_count\",\n" +
                "            \"indicator_type\": \"yoy\",\n" +
                "            \"operator\": \"gt\",\n" +
                "            \"value_type\": \"current_value\",\n" +
                "            \"value\": 81,\n" +
                "            \"unit\": \"百分数\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        try {
            String whereClause = buildWhereClause(jsonConfig,new BatchTaskConditionsModel());
            System.out.println("===== 生成的 SQL WHERE 条件 =====");
            System.out.println(whereClause);
        } catch (Exception e) {
            System.err.println("生成失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}