package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/20 15:43
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_channel")
public class InsChannelInfoEntity implements Serializable {
    /**
     * 主键
     */
    private String id;
    private String code;

    /**
     * 父级id
     */
    private String parentId;

    /**
     * 渠道名称
     */
    private String name;

    /**
     * 渠道层级
     */
    private String type;

    /**
     * 渠道英文名称
     */
    private String nameEn;
    /**
     * 渠道状态
     */
    private String status;
    /**
     * 渠道权重
     */
    private String isCoreChannel;
    /**
     * 渠道描述
     */
    private String description;
    /**
     * 渠道顶级ID
     */
    private String topId;
    /**
     * 数据源类型
     */
    private String dataSourceType;
    /**
     * 所属客户
     */
    @TableField(exist = false)
    private String clientId;
    /**
     * 渠道层级
     */
    private Integer level;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 一级渠道编码
     */
    @TableField(exist = false)
    private String channelLevelOneCode;
    /**
     * 一级渠道名称
     */
    @TableField(exist = false)
    private String channelLevelOneName;
    /**
     * 二级渠道编码
     */
    @TableField(exist = false)
    private String channelLevelTwoCode;
    /**
     * 二级渠道名称
     */
    @TableField(exist = false)
    private String channelLevelTwoName;
    /**
     * 三级渠道编码
     */
    @TableField(exist = false)
    private String channelLevelThreeCode;
    /**
     * 三级渠道名称
     */
    @TableField(exist = false)
    private String channelLevelThreeName;
}
