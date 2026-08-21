package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源详情(InsDataResourceDesc)实体类
 *
 * @author leiww
 * @since 2024-04-02 17:00:18
 */
@Data
@TableName("ins_data_resource_desc")
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsDataResourceDescEntity  implements Serializable {
    /**
     * 主键id
     *
     * @TableId
     */
    private String id;
    /**
     * 资源id
     */
    private String resourceId;
    /**
     * 资源详情
     */
    private String name;
    /**
     * 状态：全部、已启用、未启用、已禁用
     */
    private String status;
    /**
     * 创建时间
     */
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    private LocalDateTime createTime;
    /**
     * 修改用户
     */
    private String updateBy;
    /**
     * 创建用户
     */
    private String createBy;
    @TableField(exist = false)
    private Integer cnt;

}

