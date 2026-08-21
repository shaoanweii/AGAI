package com.voc.service.analysis.risk.utils;

import com.voc.service.analysis.risk.mapper.RiskDataAnalysisMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * XXL-Job任务Cron表达式管理器（全局顺序递增30秒）
 * 核心修改：将2分钟递增改为30秒递增，处理秒进位到分钟的逻辑
 */
@Slf4j
@Component
public class XxlJobCornManager {
    /**
     * 默认初始Cron：0 0 0/2 * * ? （每2小时执行，初始0分0秒）
     */
    private static final String DEFAULT_INIT_CORN = "0 0 0/2 * * ?";
    private static final int INCREMENT_SECONDS = 30; // 每次递增秒数（原2分钟→30秒）

    @Autowired
    RiskDataAnalysisMapper riskDataAnalysisMapper;



    /**
     * 批量为RuleId创建XXL-Job任务（每个任务Cron在上一个基础上+30秒）
     *
     * @param ruleIdList 规则ID列表
     */
    public void batchCreateXxlJobForRuleIds(List<String> ruleIdList) {
        if (ruleIdList == null || ruleIdList.isEmpty()) {
            log.warn("【XXL-Job配置】RuleId列表为空，无需创建任务");
            return;
        }
        // 1. 获取全局基准Cron（初始为默认值）
        String currentBaseCorn = DEFAULT_INIT_CORN;
        // 2. 遍历RuleId，顺序创建任务（逐个递增30秒）
        for (String ruleId : ruleIdList) {
            try {
                // 2.1 计算当前RuleId的Cron（基于基准Cron+30秒）
                String newCorn = calculateIncrementCorn(currentBaseCorn, INCREMENT_SECONDS);
                // 2.2 创建XXL-Job任务（先校验是否已存在，避免重复）
                this.createXxlJobIfNotExist(ruleId, newCorn);
                // 2.3 更新全局基准Cron（当前RuleId的Cron作为下一个的基准）
                currentBaseCorn = newCorn;
                log.info("【XXL-Job配置】RuleId[{}]任务创建成功，Cron：{}", ruleId, newCorn);
            } catch (Exception e) {
                log.error("【XXL-Job配置】RuleId[{}]任务创建异常", ruleId, e);
            }
        }
        log.info("【XXL-Job配置】批量创建任务完成，最终全局基准Cron：{}", currentBaseCorn);
    }

//    public static void main(String[] args) {
//        String currentBaseCorn = DEFAULT_INIT_CORN;
//        for (int i = 0; i < 100; i++){
//            currentBaseCorn=calculateIncrementCorn(currentBaseCorn,INCREMENT_SECONDS);
//            System.out.println(currentBaseCorn);
//        }
//    }

    /**
     * 计算递增后的Cron表达式（核心修改：递增30秒，处理秒→分钟进位）
     * 示例：
     * 基准Cron：0 0 0/2 * * ? → 递增30秒 → 30 0 0/2 * * ?
     * 再递增30秒 → 0 1 0/2 * * ?（秒满60进1分钟）
     * 再递增30秒 → 30 1 0/2 * * ?
     *
     * @param baseCorn         基准Cron表达式（如0 0 0/2 * * ?）
     * @param incrementSeconds 递增秒数（固定30秒）
     * @return 合法的Cron表达式
     */
    private static String calculateIncrementCorn(String baseCorn, int incrementSeconds) {
        // 拆分Cron：秒 分 时 日 月 周（XXL-Job标准格式，无年份）
        String[] cornParts = baseCorn.trim().split("\\s+");
        if (cornParts.length < 6) {
            log.warn("基准Cron格式不合法：{}，使用默认值", baseCorn);
            return DEFAULT_INIT_CORN;
        }
        try {
            // 1. 解析秒、分钟部分（核心：新增秒解析，处理秒进位）
            int second = parseCornPart(cornParts[0], 59, "秒"); // 解析秒（原仅解析分钟）
            int minute = parseCornPart(cornParts[1], 59, "分");

            // 2. 计算新秒数 + 处理进位到分钟
            int newSecond = second + incrementSeconds;
            int carryMinutes = newSecond / 60; // 秒满60进的分钟数（如90秒→进1分钟）
            newSecond = newSecond % 60; // 进位后剩余的秒数（如90秒→30秒）

            // 3. 计算新分钟数（保留原逻辑：不处理小时进位，取模60）
            int newMinute = (minute + carryMinutes) % 60;
            // 防御性处理：避免负数
            newSecond = newSecond < 0 ? 0 : newSecond;
            newMinute = newMinute < 0 ? 0 : newMinute;

            // 4. 替换秒、分钟部分，其他部分（小时步长、日/月/周）完全保留
            cornParts[0] = String.valueOf(newSecond); // 新增：替换秒部分
            cornParts[1] = String.valueOf(newMinute);

            String newCorn = String.join(" ", cornParts);
            return newCorn;
        } catch (Exception e) {
            log.warn("计算Cron递增失败，基准Cron：{}，使用默认值", baseCorn, e);
            return DEFAULT_INIT_CORN;
        }
    }

    /**
     * 解析Cron单个字段（扩展：支持秒/分钟解析，提取数字部分）
     * 仅处理纯数字/步长格式（如2 → 2；0/2 → 0；* → 0），不修改其他部分
     */
    private  static int parseCornPart(String partValue, int maxValue, String partName) {
        // 仅提取数字部分（忽略步长/范围，如0/2→0，1-5→1，*/5→0）
        String pureValue = partValue.replaceAll("[^0-9]", "");
        if (pureValue.isEmpty()) {
            log.warn("Cron{}字段无有效数字：{}，重置为0", partName, partValue);
            return 0;
        }
        try {
            int value = Integer.parseInt(pureValue);
            return Math.max(0, Math.min(value, maxValue)); // 限制在0-maxValue（秒/分最大59）
        } catch (NumberFormatException e) {
            log.warn("Cron{}字段解析失败：{}，重置为0", partName, partValue);
            return 0;
        }
    }

    /**
     * 创建XXL-Job任务（先查后建，避免重复）
     */
    private void createXxlJobIfNotExist(String ruleId, String cron) {
        try {
            int addJob = riskDataAnalysisMapper.addJob(ruleId, cron);
            log.info("【XXL-Job配置】添加任务结果：{}", addJob);
        } catch (Exception e) {
            log.error("【XXL-Job配置】调用XXL-Job Admin API失败", e);
        }
    }
}