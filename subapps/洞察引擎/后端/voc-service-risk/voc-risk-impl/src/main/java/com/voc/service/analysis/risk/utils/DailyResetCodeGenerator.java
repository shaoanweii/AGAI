package com.voc.service.analysis.risk.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于StringRedisTemplate的编号生成器
 * 特性：每天6位序列号从000001开始、原子自增不重复、持久化不丢失、跨天自动重置
 */
@Component
public class DailyResetCodeGenerator {
    // 固定前缀
    private static final String CODE_PREFIX = "CA01";
    // Redis键前缀（按日期分区）
    private static final String REDIS_KEY_PREFIX = "batchCode:daily_sequence:";
    // 6位序列号最大值
    private static final long MAX_SEQ_6BIT = 999999L;

    // 日期格式化器（指定时区，避免跨时区日期错误）
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial(() -> {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 中国时区
        return sdf;
    });

    private static final Lock EXPIRE_LOCK = new ReentrantLock();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成编号（当前日期，6位序列每天从000001开始）
     * @return 格式：CA01 + yyyyMMdd + 6位序列（如CA0120251130000001）
     */
    public String generateCode() {
        return generateCode(new Date());
    }

    /**
     * 生成编号（指定日期，用于测试/补录）
     */
    public String generateCode(Date date) {
        // 1. 格式化日期（确保每天一个独立键）
        String dateStr = DATE_FORMATTER.get().format(date);
        String redisKey = REDIS_KEY_PREFIX + dateStr;

        // 2. Redis原子自增：当天首次调用→初始化为1，后续+1（核心）
        Long sequenceNum = stringRedisTemplate.opsForValue().increment(redisKey);
        if (sequenceNum == null) {
            throw new RuntimeException("Redis自增失败，生成编号异常");
        }

        // 3. 6位序列溢出防护（避免超过999999）
        if (sequenceNum > MAX_SEQ_6BIT) {
            throw new RuntimeException(String.format(
                    "日期[%s]的6位序列号已溢出（当前值：%d），单日生成量超过999999，请扩容位数",
                    dateStr, sequenceNum
            ));
        }

        // 4. 首次生成时设置键过期时间（避免Redis数据堆积，可选但推荐）
        if (sequenceNum == 1) {
            EXPIRE_LOCK.lock();
            try {
                // 双重检查：防止多线程重复设置过期时间
                if ("1".equals(stringRedisTemplate.opsForValue().get(redisKey))) {
                    // 过期时间：保留7天（足够覆盖业务追溯周期，可调整）
                    stringRedisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
                }
            } finally {
                EXPIRE_LOCK.unlock();
            }
        }

        // 5. 6位序列补零（1→000001，100→000100）
        String sequenceStr = String.format("%06d", sequenceNum);

        // 6. 拼接最终编号
        return CODE_PREFIX + dateStr + sequenceStr;
    }

    /**
     * 手动重置指定日期的序列号（仅异常修复时使用）
     */
    public void resetDailySequence(Date date) {
        String dateStr = DATE_FORMATTER.get().format(date);
        String redisKey = REDIS_KEY_PREFIX + dateStr;
        stringRedisTemplate.opsForValue().set(redisKey, "0"); // 重置后下次increment为1
    }
}