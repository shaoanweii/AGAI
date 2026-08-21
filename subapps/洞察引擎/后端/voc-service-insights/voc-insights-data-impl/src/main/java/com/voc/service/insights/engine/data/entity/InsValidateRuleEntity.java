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
 * @创建时间: 2024/3/29 10:18
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_validate_rule")
public class InsValidateRuleEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    /**
     * 规则id
     */
    private String regulationId;

    /**
     * 数据处理链路标识
     */
    private String workId;
    /**
     * 数据处理类型 单规则类型：0 测试类型：1
     */
    private String singleOrFullType;

    /**
     * 数据处理校验状态
     * 未校验:-1 校验中:0  校验成功:1 校验失败:2
     *
     */
    private String singleValidateStatus;
    /**
     * 数据处理校验状态
     * 未测试:-1 测试中:0  测试成功:1 测试失败:2
     */
    private String fullValidateStatus;

    /**
     * 检验开始时间
     */
    private LocalDateTime createTime;

    /**
     * 校验结束时间
     */
    private LocalDateTime updateTime;

    /**
     * 渠道
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> channel;
    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 操作人
     */
    private String operator;
}
