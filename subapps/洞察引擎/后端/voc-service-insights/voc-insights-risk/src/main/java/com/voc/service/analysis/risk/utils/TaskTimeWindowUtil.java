package com.voc.service.analysis.risk.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 定时任务时间窗口计算工具（规避执行误差）
 */
public class TaskTimeWindowUtil {
    // 固定配置：首次执行时间（小时）、执行间隔（小时）、时区
    private static final int FIRST_EXEC_HOUR = 2; // 首次执行小时：凌晨2点
    private static final int INTERVAL_HOURS = 2; // 执行间隔：2小时
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 计算当前任务的「理论执行时间」和「2小时数据窗口」
     * @return 包含理论执行时间、窗口开始时间、窗口结束时间
     */
    public static TimeWindow calculateTimeWindow() {
        // 1. 获取当前系统时间（上海时区，避免时区偏差）
        LocalDateTime now = LocalDateTime.now(DEFAULT_ZONE);

        // 2. 计算当天首次理论执行时间（如 2025-11-18 02:00:00）
        LocalDateTime todayFirstExecTime = now.withHour(FIRST_EXEC_HOUR)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        // 3. 计算理论执行时间（最近的、不晚于当前时间的执行点）
        LocalDateTime theoreticalExecTime;
        if (now.isBefore(todayFirstExecTime)) {
            // 若当前时间早于当天首次执行时间（如 01:30），取前一天的最后一个执行点（如前一天 22:00）
            theoreticalExecTime = todayFirstExecTime.minusDays(1).plusHours(INTERVAL_HOURS * ((24 / INTERVAL_HOURS) - 1));
        } else {
            // 计算当前时间与当天首次执行时间的间隔（小时）
            long hoursSinceFirst = ChronoUnit.HOURS.between(todayFirstExecTime, now);
            // 计算间隔内的执行次数（向下取整，得到最近的理论执行时间）
            long execCount = hoursSinceFirst / INTERVAL_HOURS;
            theoreticalExecTime = todayFirstExecTime.plusHours(execCount * INTERVAL_HOURS);
        }

        // 4. 计算2小时数据窗口：开始时间=理论执行时间-2小时，结束时间=理论执行时间
        LocalDateTime windowStart = theoreticalExecTime.minusHours(INTERVAL_HOURS);
        LocalDateTime windowEnd = theoreticalExecTime;

        return new TimeWindow(theoreticalExecTime, windowStart, windowEnd);
    }

    // 时间窗口封装类
    public static class TimeWindow {
        private final LocalDateTime theoreticalExecTime; // 理论执行时间
        private final LocalDateTime windowStart; // 数据窗口开始时间
        private final LocalDateTime windowEnd; // 数据窗口结束时间

        public TimeWindow(LocalDateTime theoreticalExecTime, LocalDateTime windowStart, LocalDateTime windowEnd) {
            this.theoreticalExecTime = theoreticalExecTime;
            this.windowStart = windowStart;
            this.windowEnd = windowEnd;
        }

        // getter 方法
        public LocalDateTime getTheoreticalExecTime() { return theoreticalExecTime; }
        public LocalDateTime getWindowStart() { return windowStart; }
        public LocalDateTime getWindowEnd() { return windowEnd; }
    }
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static void main(String[] args) {


        TimeWindow timeWindow = calculateTimeWindow();

        String startStr = timeWindow.getWindowStart().format(DB_FORMATTER);
        String endStr = timeWindow.getWindowEnd().format(DB_FORMATTER);

        System.out.printf("任务实际执行时间：%s%n", LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(DB_FORMATTER));
        System.out.printf("理论执行时间：%s%n", timeWindow.getTheoreticalExecTime().format(DB_FORMATTER));
        System.out.printf("查询数据窗口：%s 至 %s%n", startStr, endStr);

    }
}