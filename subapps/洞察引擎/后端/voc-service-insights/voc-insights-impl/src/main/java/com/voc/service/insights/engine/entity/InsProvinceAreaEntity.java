package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 区域城市信息表(InsProvinceArea)表实体类
 *
 * @author leiww
 * @since 2024-01-25 14:50:51
 */
@Data
@TableName("ins_province_area")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class InsProvinceAreaEntity extends Model<InsProvinceAreaEntity> implements Serializable {

    /**
     * 主键id
     */
    @TableId
    private String id;
    /**
     * 区域编码
     */
    @TableField(value = "area_code")
    private String areaCode;
    /**
     * 区域名称
     */
    @TableField(value = "area_name")
    private String areaName;
    /**
     * 城市编码
     */
    @TableField(value = "province_code")
    private String provinceCode;
    /**
     * 城市名称
     */
    @TableField(value = "province_name")
    private String provinceName;
}

