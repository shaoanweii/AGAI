package com.voc.service.analysis.risk.utils;

import com.alibaba.fastjson.JSON;
import com.voc.service.risk.api.model.BatchTaskConditionsModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VocTimeUtil {

    private static final Map<String, DayOfWeek> WEEKDAY_MAP = new HashMap<>();
    static {
        WEEKDAY_MAP.put("周一", DayOfWeek.MONDAY);
        WEEKDAY_MAP.put("周二", DayOfWeek.TUESDAY);
        WEEKDAY_MAP.put("周三", DayOfWeek.WEDNESDAY);
        WEEKDAY_MAP.put("周四", DayOfWeek.THURSDAY);
        WEEKDAY_MAP.put("周五", DayOfWeek.FRIDAY);
        WEEKDAY_MAP.put("周六", DayOfWeek.SATURDAY);
        WEEKDAY_MAP.put("周日", DayOfWeek.SUNDAY);
    }

    // 工具方法：获取上月合法日期，不存在则取月末
    private static LocalDate getValidLastMonthDate(LocalDate baseDate, int day) {
        try {
            return baseDate.minusMonths(1).withDayOfMonth(day);
        } catch (Exception e) {
            return baseDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        }
    }

    public static void buildTimeParam(String avgDayType, String alertTime, BatchTaskConditionsModel batchTaskConditionsModel) {
        LocalDate now = LocalDate.now();
        LocalDate currStart, currEnd;
        LocalDate prevStart, prevEnd;
        LocalDate prevPrevStart, prevPrevEnd;
        LocalDate judgeDate = now;

        switch (avgDayType) {
            case "daily":
                currEnd = now;
                currStart = now.minusDays(1);
                prevEnd = currStart;
                prevStart = currStart.minusDays(1);
                prevPrevEnd = prevStart;
                prevPrevStart = prevStart.minusDays(1);
                judgeDate = now;
                break;

            case "weekly":
                // ===================== 周维度 最终修复 =====================
                String weekDayStr = extractWeekDay(alertTime);
                DayOfWeek targetWeekDay = WEEKDAY_MAP.get(weekDayStr);
                LocalDate currentWeekDay = now.with(TemporalAdjusters.previousOrSame(targetWeekDay));

                // 本期：上周X ~ 本周X-1天（周日-1天=周六）
                currStart = currentWeekDay.minusWeeks(1);
                currEnd = currentWeekDay.minusDays(1); // 核心修复：-1天

                // 上期
                prevStart = currStart.minusWeeks(1);
                prevEnd = currStart.minusDays(1);

                // 上上期
                prevPrevStart = prevStart.minusWeeks(1);
                prevPrevEnd = prevStart.minusDays(1);
                // ==========================================================
                judgeDate = currentWeekDay;
                break;

            case "monthly":
                int alertDay = extractMonthDay(alertTime);
                LocalDate currentAlertDay;

                if (alertDay == 31) {
                    currentAlertDay = now.with(TemporalAdjusters.lastDayOfMonth());
                } else {
                    currentAlertDay = now.withDayOfMonth(alertDay);
                }

                // 本月告警日 -1 天
                currStart = getValidLastMonthDate(currentAlertDay, alertDay);
                currEnd = currentAlertDay.minusDays(1);

                // 上期
                LocalDate prevAlertDay = getValidLastMonthDate(currentAlertDay, alertDay);
                prevStart = getValidLastMonthDate(prevAlertDay, alertDay);
                prevEnd = prevAlertDay.minusDays(1);

                // 上上期
                LocalDate prevPrevAlertDay = getValidLastMonthDate(prevAlertDay, alertDay);
                prevPrevStart = getValidLastMonthDate(prevPrevAlertDay, alertDay);
                prevPrevEnd = prevPrevAlertDay.minusDays(1);

                judgeDate = currentAlertDay;
                break;

            default:
                throw new RuntimeException("不支持的维度类型：" + avgDayType);
        }

        // 年/月开始判断
        LocalDate yearStartTime = judgeDate.getDayOfYear() == 1
                ? judgeDate.minusYears(1).with(TemporalAdjusters.firstDayOfYear())
                : judgeDate.with(TemporalAdjusters.firstDayOfYear());

        LocalDate monthStartTime = judgeDate.getDayOfMonth() == 1
                ? judgeDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                : judgeDate.with(TemporalAdjusters.firstDayOfMonth());

        // 同期
        LocalDate lastYearStartTime = currStart.minusYears(1);
        LocalDate lastYearEndTime = currEnd.minusYears(1);
        LocalDate lastLastYearStartTime = currStart.minusYears(2);
        LocalDate lastLastYearEndTime = currEnd.minusYears(2);

        // 赋值
        batchTaskConditionsModel.setStartTime(currStart.toString());
        batchTaskConditionsModel.setEndTime(currEnd.toString());
        batchTaskConditionsModel.setPrevStartTime(prevStart.toString());
        batchTaskConditionsModel.setPrevEndTime(prevEnd.toString());
        batchTaskConditionsModel.setPrevPrevStartTime(prevPrevStart.toString());
        batchTaskConditionsModel.setPrevPrevEndTime(prevPrevEnd.toString());
        batchTaskConditionsModel.setYearStartTime(yearStartTime.toString());
        batchTaskConditionsModel.setMonthStartTime(monthStartTime.toString());
        batchTaskConditionsModel.setLastYearStartTime(lastYearStartTime.toString());
        batchTaskConditionsModel.setLastYearEndTime(lastYearEndTime.toString());
        batchTaskConditionsModel.setLastLastYearStartTime(lastLastYearStartTime.toString());
        batchTaskConditionsModel.setLastLastYearEndTime(lastLastYearEndTime.toString());
        System.out.println(JSON.toJSONString(batchTaskConditionsModel));
    }

    private static String extractWeekDay(String alertTime) {
        return Arrays.stream(WEEKDAY_MAP.keySet().toArray(new String[0]))
                .filter(alertTime::contains)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("alertTime 格式错误：" + alertTime));
    }

    private static int extractMonthDay(String alertTime) {
        try {
            return Integer.parseInt(alertTime.split("日")[0].trim());
        } catch (Exception e) {
            throw new RuntimeException("alertTime 日期解析失败：" + alertTime);
        }
    }




    public static void main(String[] args) {
        buildTimeParam("monthly","1日 08:00", new BatchTaskConditionsModel());

    }
}