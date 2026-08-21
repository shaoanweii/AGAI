package com.voc.service.insights.engine.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/27 17:21
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AysRegulationInfoVo implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 规则名称
     */
    private String clientId;
    /**
     * 规则名称
     */
    private String name;
    /**
     * 规则描述
     */
    private String description;

    /**
     * 规则类型
     */
    private String regulationType;

    /**
     * 内容类型 例如:文本、工单
     */
    private String contentType;

    /**
     * 数据渠道
     */
    private List<String> channel;

    private String processPhase;

    private String regulationWeight;
    /**
     * 匹配规则
     */

    private String matchingRule;

    private String regulationClassify;

    /**
     * 停用/启用状态 停用:0 启用:1
     */
    private String status;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    private String singleValidateStatus;
    /**
     * 完全验证状态 -1 未测试 0 测试中 1 测试成功 2 测试失败 默认 -1
     */
    private String fullyValidateStatus;

    List<RegulationDetailsVo> regulationConditions;

    List<RegulationDetailsVo> regulationPerformAction;

    private String statusCount;

}
