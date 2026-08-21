package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 批量规则分类实体
 * 对应数据库表：ins_batch_rule_category
 */
@Data
@TableName("ins_batch_rule_category")
public class InsBatchRuleCategoryEntity {

    /**
     * 分类ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID，顶级分类为0
     */
    private String parentId;

    /**
     * 分类类型
     */
    private String type;

    /**
     * 状态：Enabled/Disabled
     */
    private String status;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态：0正常 1已删除
     */
    private Integer delFlag;
}
