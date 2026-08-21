package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/9/11 上午9:34
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_region_detail")
public class InsRegionDetailEntity{
    /**
     * 主键
     */
    private String id;

    /**
     * 父级id
     */
    private String parentId;

    /**
     * 区域名称/分类名称
     */
    private String name;

    /**
     * 区域英文名称
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String nameEn;
    /**
     * 区域状态
     */
    private String status;

    /**
     * 区域(省份+城市)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ProvinceEntity> region;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 修改人
     */
    private String updateUser;
}
