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
 * @创建时间: 2024/3/13 09:27
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_rule_info")
public class InsRuleInfoEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 客户id
     */
    private String clientCode;

    /**
     * 项目id
     */
    private String projectCode;
    /**
     * 内容类型 例如:文本、工单
     */
    private String contentType;
    /**
     * 处理阶段 前置处理:0 后置处理:1
     */
    private String processPhase;

    /**
     * 停用/启用状态 停用:0 启用:1 默认启用
     */
    private String enable;

    /**
     * 删除标识 未删除:0 已删除：1
     */
    private String delFlag;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 关联表
     */
    private String relevancyTable;

    /**
     * 是否为虚拟数据 虚拟数据:1 非虚拟数据:0 默认为非虚拟数据
     */
    private String virtualization;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 修改者
     */
    private String updateUser;
}
