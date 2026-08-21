package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.voc.service.insights.engine.vo.AutomarkVo;
import com.voc.service.insights.engine.vo.BrandInfoVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2026/2/11 14:48
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_automark",autoResultMap = true)
public class InsAutomarkEntity {
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 英文名称
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String nameEn;
    /**
     * 展示图片
     */
    private String img;
    /**
     * 是否核心
     */
    private Integer isCore;
    /**
     * 本竞品类型 1本品，2竞品，3非关注范围
     */
    private Integer competitiveType;

    /**
     * 本竞品关系
     */
    @TableField(typeHandler = JacksonTypeHandler.class,updateStrategy = FieldStrategy.ALWAYS)
    private List<AutomarkVo> competitiveProduct;
    /**
     * 状态
     */
    private String status;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 更新人
     */
    private String updateOperator;
    /**
     * 是否核心名称
     */
    @TableField(exist = false)
    private  String isCoreName;
    /**
     * 本竞品类型名称
     */
    @TableField(exist = false)
    private String competitiveTypeName;
    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;
}
