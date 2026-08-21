package com.voc.service.analysis.risk.utils;

import com.voc.service.analysis.risk.constant.DeduplicationCompareResult;
import com.voc.service.analysis.risk.entity.ReportModelTagsResultDataRiskEntity;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TwoSetDeduplicationUtil {

    private static final Logger log = LoggerFactory.getLogger(TwoSetDeduplicationUtil.class);
    // 发布时间标准化格式（统一到秒级，避免毫秒差异）
    private static final DateTimeFormatter POST_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 空值默认填充（避免拼接键时为空）
    private static final String DEFAULT_EMPTY_VALUE = "未知";

    /**
     * 核心方法：两组数据对比去重（按逻辑A+逻辑B规则）
     *
     * @param listA 数据组A
     * @param listB 数据组B
     * @return 对比结果（仅A、仅B、重复数据）
     */
    public DeduplicationCompareResult compareAndDeduplicate(
            List<ReportModelTagsResultDataRiskEntity> listA,
            List<ReportModelTagsResultDataRiskEntity> listB
    ) {
        log.info("开始对比去重，数据组A={}, 数据组B={}", listA.size(), listB.size());
        // 步骤1：预处理两组数据（渠道映射+字段校验+时间标准化）
        List<ReportModelTagsResultDataRiskEntity> processedA = preprocessData(listA);
        List<ReportModelTagsResultDataRiskEntity> processedB = preprocessData(listB);

        // 步骤2：构建两组数据的去重键映射（逻辑A键→数据、逻辑B键→数据）
        DeduplicationKeyMap keyMapA = buildDeduplicationKeyMap(processedA);
        DeduplicationKeyMap keyMapB = buildDeduplicationKeyMap(processedB);

        // 步骤3：筛选仅在A存在的非重复数据
        List<ReportModelTagsResultDataRiskEntity> onlyInA = filterNonDuplicate(processedA, keyMapB.getLogicAKeySet(), keyMapB.getLogicBKeySet());

        // 步骤4：筛选仅在B存在的非重复数据
        List<ReportModelTagsResultDataRiskEntity> onlyInB = filterNonDuplicate(processedB, keyMapA.getLogicAKeySet(), keyMapA.getLogicBKeySet());

        // 步骤5：筛选重复数据（A中出现在B的重复数据）
        List<ReportModelTagsResultDataRiskEntity> duplicateData = filterDuplicate(processedA, keyMapB.getLogicAKeySet(), keyMapB.getLogicBKeySet());

        // 封装结果
        DeduplicationCompareResult result = new DeduplicationCompareResult();
        result.setOnlyInA(onlyInA);
        result.setOnlyInB(onlyInB);
        result.setDuplicateData(duplicateData);
        log.info("数据去重结果：仅A={}, 仅B={}, 重复数据={}", onlyInA.size(), onlyInB.size(), duplicateData.size());
        return result;
    }

    /**
     * 数据预处理：渠道映射+字段校验+时间标准化
     */
    private List<ReportModelTagsResultDataRiskEntity> preprocessData(List<ReportModelTagsResultDataRiskEntity> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyList();
        }

        return dataList.stream().map(data -> {

            // 2. 发布时间标准化（统一到秒级，空值抛异常）
            if (data.getPublishTime() == null) {
                throw new IllegalArgumentException("发布时间（newsPosttime）不能为空，数据标题：" + data.getPublishTime());
            }
            LocalDateTime standardPostTime = data.getPublishTime().withNano(0); // 去掉毫秒
            data.setPublishTime(standardPostTime);
            // 3. 空值填充（避免拼接键时为空）
            data.setMainPostTitle(Optional.ofNullable(data.getMainPostTitle()).orElse(DEFAULT_EMPTY_VALUE).trim());
            data.setPostUserName(Optional.ofNullable(data.getPostUserName()).orElse(DEFAULT_EMPTY_VALUE).trim());
            data.setMainPostUrl(Optional.ofNullable(data.getMainPostUrl()).orElse(DEFAULT_EMPTY_VALUE).trim());
            return data;
        }).collect(Collectors.toList());
    }

    /**
     * 构建去重键映射：逻辑A键集合、逻辑B键集合、键→数据映射
     */
    private DeduplicationKeyMap buildDeduplicationKeyMap(List<ReportModelTagsResultDataRiskEntity> dataList) {
        Set<String> logicAKeySet = new HashSet<>();
        Set<String> logicBKeySet = new HashSet<>();
        Map<String, ReportModelTagsResultDataRiskEntity> logicAKey2Data = new HashMap<>();
        Map<String, ReportModelTagsResultDataRiskEntity> logicBKey2Data = new HashMap<>();

        for (ReportModelTagsResultDataRiskEntity data : dataList) {
            // 生成逻辑A键（MD5）
            String logicAKey = generateLogicAKey(data);
            logicAKeySet.add(logicAKey);
            logicAKey2Data.put(logicAKey, data);

            // 生成逻辑B键（URL）
            String logicBKey = data.getMainPostUrl().trim();
            logicBKeySet.add(logicBKey);
            logicBKey2Data.put(logicBKey, data);
        }

        DeduplicationKeyMap keyMap = new DeduplicationKeyMap();
        keyMap.setLogicAKeySet(logicAKeySet);
        keyMap.setLogicBKeySet(logicBKeySet);
        keyMap.setLogicAKey2Data(logicAKey2Data);
        keyMap.setLogicBKey2Data(logicBKey2Data);
        return keyMap;
    }

    /**
     * 生成逻辑A唯一键（标题+作者+标准化发布时间+二级渠道 → MD5）
     */
    private String generateLogicAKey(ReportModelTagsResultDataRiskEntity data) {
        String rawKey = String.join("|",
                data.getMainPostTitle(),
                data.getPostUserName(),
                data.getPublishTime().format(POST_TIME_FORMATTER),
                data.getSecondChannelCode()
        );
        return DigestUtils.md5Hex(rawKey); // MD5加密，统一长度且避免字符串过长
    }

    /**
     * 筛选非重复数据：数据列表中，逻辑A键和逻辑B键均不在目标键集合中
     */
    private List<ReportModelTagsResultDataRiskEntity> filterNonDuplicate(
            List<ReportModelTagsResultDataRiskEntity> dataList,
            Set<String> targetLogicAKeySet,
            Set<String> targetLogicBKeySet
    ) {
        return dataList.stream()
                .filter(data -> {
                    String logicAKey = generateLogicAKey(data);
                    String logicBKey = data.getMainPostUrl().trim();
                    // 非重复：逻辑A键不在目标A集合，且逻辑B键不在目标B集合
                    return !targetLogicAKeySet.contains(logicAKey) && !targetLogicBKeySet.contains(logicBKey);
                })
                .collect(Collectors.toList());
    }

    /**
     * 筛选重复数据：数据列表中，逻辑A键或逻辑B键出现在目标键集合中
     */
    private List<ReportModelTagsResultDataRiskEntity> filterDuplicate(
            List<ReportModelTagsResultDataRiskEntity> dataList,
            Set<String> targetLogicAKeySet,
            Set<String> targetLogicBKeySet
    ) {
        return dataList.stream()
                .filter(data -> {
                    String logicAKey = generateLogicAKey(data);
                    String logicBKey = data.getMainPostUrl().trim();
                    // 重复：逻辑A键在目标A集合，或逻辑B键在目标B集合
                    return targetLogicAKeySet.contains(logicAKey) || targetLogicBKeySet.contains(logicBKey);
                })
                .collect(Collectors.toList());
    }

    /**
     * 内部类：去重键映射（封装逻辑A/B的键集合和键→数据映射）
     */
    private static class DeduplicationKeyMap {
        private Set<String> logicAKeySet; // 逻辑A键集合
        private Set<String> logicBKeySet; // 逻辑B键集合
        private Map<String, ReportModelTagsResultDataRiskEntity> logicAKey2Data; // 逻辑A键→数据
        private Map<String, ReportModelTagsResultDataRiskEntity> logicBKey2Data; // 逻辑B键→数据

        // getter/setter 省略（Lombok可简化，此处手动写）
        public Set<String> getLogicAKeySet() {
            return logicAKeySet;
        }

        public void setLogicAKeySet(Set<String> logicAKeySet) {
            this.logicAKeySet = logicAKeySet;
        }

        public Set<String> getLogicBKeySet() {
            return logicBKeySet;
        }

        public void setLogicBKeySet(Set<String> logicBKeySet) {
            this.logicBKeySet = logicBKeySet;
        }

        public Map<String, ReportModelTagsResultDataRiskEntity> getLogicAKey2Data() {
            return logicAKey2Data;
        }

        public void setLogicAKey2Data(Map<String, ReportModelTagsResultDataRiskEntity> logicAKey2Data) {
            this.logicAKey2Data = logicAKey2Data;
        }

        public Map<String, ReportModelTagsResultDataRiskEntity> getLogicBKey2Data() {
            return logicBKey2Data;
        }

        public void setLogicBKey2Data(Map<String, ReportModelTagsResultDataRiskEntity> logicBKey2Data) {
            this.logicBKey2Data = logicBKey2Data;
        }
    }
}