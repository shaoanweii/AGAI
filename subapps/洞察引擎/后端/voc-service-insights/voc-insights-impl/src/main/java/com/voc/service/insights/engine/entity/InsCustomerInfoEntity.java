package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 10:12
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_customer_info")
public class InsCustomerInfoEntity implements Serializable {
    /**
     * 主键
     */
    private String id;

    /**
     * 全称
     */
    private String fullName;

    /**
     * 简称
     */
    private String abbreviation;

    /**
     * 编码
     */
    private String code;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 联系地址
     */
    private String address;

    /**
     * 备注
     */
    private String remark;

    /**
     * 停用/启用状态 停用:0 启用:1 默认为启用
     */
    private Integer status;

    /**
     * 是否删除 是:1 否:0 默认为否
     */
    private Integer delFlag;

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
    /**
     * 排序字段，用于下拉列表
     */
    private Integer sort;
}
