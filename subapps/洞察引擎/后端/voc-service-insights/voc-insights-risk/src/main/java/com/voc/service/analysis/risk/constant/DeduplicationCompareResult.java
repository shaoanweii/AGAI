package com.voc.service.analysis.risk.constant;

import com.voc.service.analysis.risk.entity.CarDataEntity;
import com.voc.service.analysis.risk.entity.ReportModelTagsResultDataRiskEntity;
import lombok.Data;

import java.util.List;

/**
 * 两组数据对比去重的结果
 *
 * @param <T> 数据类型（如CarDataEntity）
 */
@Data
public class DeduplicationCompareResult {
    /**
     * 仅在A组存在的非重复数据
     */
    private List<ReportModelTagsResultDataRiskEntity> onlyInA;
    /**
     * 仅在B组存在的非重复数据
     */
    private List<ReportModelTagsResultDataRiskEntity> onlyInB;
    /**
     * 两组重复的数据（A和B中都存在）
     */
    private List<ReportModelTagsResultDataRiskEntity> duplicateData;
}