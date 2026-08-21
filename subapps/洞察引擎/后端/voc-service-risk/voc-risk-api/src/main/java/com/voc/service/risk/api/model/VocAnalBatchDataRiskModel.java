package com.voc.service.risk.api.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocAnalBatchDataRiskModel {

    /**
     * 主键
     */
    private String id;

    /**
     * 预警时间
     */
    private LocalDateTime warningTime;

    /**
     * 规则id
     */
    private String ruleId;

    /**
     * 事件信息（关联闭环规则的name字段）
     */
    private String eventName;

    /**
     * 主题分类ID（关联闭环规则的category_type字段）
     */
    private String subjectCategoryId;

    /**
     * 主题分类名称（关联ins_data_resource表的name字段）
     */
    private String subjectCategoryName;

    /**
     * 预警周期
     */
    private String warningPeriod;

    /**
     * 预警事件编号（自动生成，主键）
     */
    private String warningEventNo;

    /**
     * 结果表主键id
     */
    private String ids;

    /**
     * 品牌编码
     */
    private String brandCode;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 车系编码
     */
    private String carSeriesCode;

    /**
     * 车系名称
     */
    private String carSeriesName;

    /**
     * 观点编码
     */
    private String topic;

    /**
     * 观点名称
     */
    private String topicName;

    /**
     * 提及量
     */
    private String mentionCount;

    /**
     * 提及量环比
     */
    private String mentionCountRate;

    /**
     * 负面率
     */
    private String negativeRate;

    /**
     * 负面率环比
     */
    private String negativeRateR;

    /**
     * 事件优先级（关联闭环规则的process_priority字段）
     */
    private String eventPriority;

    /**
     * 事件优先级名称（关联数据字典ins_closed_rule_priority的name）
     */
    private String eventPriorityName;

    /**
     * 审核方式（关联闭环规则的confirm_method字段）
     */
    private String reviewMethod;

    /**
     * 审核人ID（关联confirmer的id属性）
     */
    private String reviewUserId;

    /**
     * 审核人工号（关联confirmer的employeeId属性）
     */
    private String reviewUserEmpNo;

    /**
     * 审核人名称（关联confirmer的name属性）
     */
    private String reviewUserName;

    /**
     * 主责人ID（关联main_responder的id属性）
     */
    private String mainRespUserId;

    /**
     * 主责人工号（关联main_responder的employeeId属性）
     */
    private String mainRespUserEmpNo;

    /**
     * 主责人名称（关联main_responder的name属性）
     */
    private String mainRespUserName;

    /**
     * 抄送人（数组，存储JSON格式）
     */
    private String ccUsers;

    /**
     * 事件属性
     */
    private String eventAttribute;

    /**
     * 事件有效性
     */
    private String eventValidity;

    /**
     * 任务状态（待处理/处理中/已完成/已取消等）
     */
    private String taskStatus;

    /**
     * 修改人ID
     */
    private String createUserName;

    /**
     * 修改人ID
     */
    private String updateUserId;

    /**
     * 记录时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
