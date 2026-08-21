package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/26 16:25
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_regulation_info")
public class InsRegulationInfoEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    /**
     * 客户id
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
     * 处理阶段 前置处理:0 后置处理:1
     */
    private String processPhase;

    /**
     * 规则类型
     */
    private String regulationType;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 数据渠道
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> channel;

    /**
     * 匹配规则
     */
    private String matchingRule;

    /**
     * 规则权重
     */
    private Long regulationWeight;

    /**
     * 关联表
     */
    private String relevancyTable;

    /**
     * 是否为虚拟数据 虚拟数据:1 非虚拟数据:0 默认为非虚拟数据
     */
    private String virtualization;
    /**
     * 规则分类
     */
    private String regulationClassify;

    @TableField(exist = false)
    private String singleValidateStatus;
    /**
     * 完全验证状态 -1 未测试 0 测试中 1 测试成功 2 测试失败 默认 -1
     */
    @TableField(exist = false)
    private String fullyValidateStatus;

    /**
     * 停用/启用状态 停用:0 启用:1
     */
    private String status;

    /**
     * 删除标识 未删除:0 已删除:1
     */
    private String delFlag;

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
     * 更新者
     */
    private String updateUser;

    @TableField(exist = false)
    private String statusCount;
}
