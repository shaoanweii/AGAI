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
 * @创建时间: 2026/4/9 13:40
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ins_property_tag")
public class InsAttributeLabelEntity implements Serializable {
    /**
     * id
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
