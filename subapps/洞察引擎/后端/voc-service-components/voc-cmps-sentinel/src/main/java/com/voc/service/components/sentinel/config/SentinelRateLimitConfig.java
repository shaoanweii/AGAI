package com.voc.service.components.sentinel.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流规则配置
 * 从 Nacos 配置中心读取限流规则
 */
@Configuration
@ConfigurationProperties(prefix = "voc.sentinel")
@Data
public class SentinelRateLimitConfig {
    private static final Logger log = LoggerFactory.getLogger(SentinelRateLimitConfig.class);

    private List<FlowRuleConfig> flowRules = new ArrayList<>();

    @Data
    public static class FlowRuleConfig {
        private String resource;
        private Integer grade = 1;
        private Double count = 0.0;
        private String limitApp = "default";
        private Integer strategy = 0;
        private Integer controlBehavior = 0;
    }

    @PostConstruct
    public void initFlowRules() {
        if (flowRules == null || flowRules.isEmpty()) {
            log.warn("⚠️ Sentinel 限流规则为空，未加载任何规则");
            log.warn("⚠️ 请检查 Nacos 中的 voc.sentinel.flow-rules 配置");
            return;
        }

        List<FlowRule> rules = new ArrayList<>();

        for (FlowRuleConfig config : flowRules) {
            FlowRule rule = new FlowRule();
            rule.setResource(config.getResource());
            rule.setGrade(config.getGrade() != null ? config.getGrade() : RuleConstant.FLOW_GRADE_QPS);
            rule.setCount(config.getCount() != null ? config.getCount() : 0.0);
            rule.setLimitApp(config.getLimitApp() != null ? config.getLimitApp() : "default");
            rule.setStrategy(config.getStrategy() != null ? config.getStrategy() : RuleConstant.STRATEGY_DIRECT);
            rule.setControlBehavior(config.getControlBehavior() != null ? config.getControlBehavior() : RuleConstant.CONTROL_BEHAVIOR_DEFAULT);

            rules.add(rule);
            log.info("✅ 加载 Sentinel 限流规则: resource={}, QPS={}", config.getResource(), config.getCount());
        }

        FlowRuleManager.loadRules(rules);
        log.info("✅ Sentinel 限流规则加载完成，共 {} 条规则", rules.size());

        // 打印当前所有规则用于调试
        List<FlowRule> currentRules = FlowRuleManager.getRules();
        log.info("📋 当前 Sentinel 规则列表:");
        for (FlowRule r : currentRules) {
            log.info("   - {}: QPS={}", r.getResource(), r.getCount());
        }
    }
}
