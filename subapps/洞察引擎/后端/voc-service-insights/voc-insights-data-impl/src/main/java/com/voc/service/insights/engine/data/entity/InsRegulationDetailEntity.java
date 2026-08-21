package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/26 16:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_regulation_detail")
public class InsRegulationDetailEntity implements Serializable {
    /**
     * 主键
     */
    private String id;

    /**
     * 规则id
     */
    private String regulationId;

    /**
     * 字段
     */
    private String fieldName;
    /**
     * 变量值
     */
    private String variableValue;
    /**
     * 逻辑运算符
     */
    private String logicalOperator;
    /**
     * 条件类型
     */
    private String conditionType;
    /**
     * 条件详情
     */
    private String conditionDetail;
    /**
     * 详情类型 规则条件:0 规则执行动作:1
     */
    private String detailType;

    /**
     * 停用/启用状态 停用:0 启用:1
     */
    private String status;

    /**
     * 删除状态 未删除: 0 ，已删除:1
     */
    private Integer delFlag;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 序号
     */
    private String serialNumber;
}
