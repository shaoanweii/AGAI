package com.voc.service.insights.engine.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.voc.service.insights.engine.vo.CarInfoVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 车系信息表
 *
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "ins_car_series_info",autoResultMap = true)
public class InsCarSeriesInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
//    @TableId(type = IdType.ASSIGN_ID)
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
     * 品牌id
     */
    private String brandId;
    /**
     * 品牌编码
     */
    private String brandCode;
    /**
     * 别名，多个别称以逗号隔开
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String alias;
    /**
     * 排除词；多个别称以逗号隔开，用于AI调用
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String exclusionWords;
    /**
     * 编码
     */
    private String code;
    /**
     * 排序
     */
    private int orderBy;
    /**
     * 车型名称
     */
    private String carName;
    /**
     * 车型编码
     */
    private String carCode;
    /**
     * 工厂 本品车系会分析工厂字段
     */
    private String factory;

    /**
     * 车辆级别1
     */
    private String carLevel1;
    /**
     * 车辆级别2
     */
    private String carLevel2;
    /**
     * 能源类型1
     */
    private String energyType1;
    /**
     * 能源类型2
     */
    private String energyType2;
    /**
     * 级别名称 级别2+级别1拼接
     */
    private String levelName;
    /**
     * 本竞品类型 1本品，2竞品，3非关注范围
     */
    private Integer competitiveType;
    /**
     * 关联本竞品 选择本品时绑定竞品车系，选择竞品时绑定本品车系（多对多）
     */
    @TableField(typeHandler = JacksonTypeHandler.class,updateStrategy = FieldStrategy.ALWAYS)
    private List<CarInfoVo> competitiveProduct;
    /**
     * 预热开始时间
     */
    private LocalDate preheatStartTime;
    /**
     * 预热结束时间
     */
    private LocalDate preheatEndTime;
    /**
     * 上市开始时间
     */
    private LocalDate launchStartTime;
    /**
     * 上市结束时间
     */
    private LocalDate launchEndTime;
    /**
     * 稳定开始时间
     */
    private LocalDate stableStartTime;
    /**
     * 稳定结束时间
     */
    private LocalDate stableEndTime;
    /**
     * 是否核心车系 0非核心 1核心
     */
    private Integer isCore;
    /**
     * 创建人
     */
    private String operator;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 更新人
     */
    private String updateUser;
    /**
     * 删除状态 0正常 1已删除
     */
    private Boolean delFlag;
    /**
     * 展示图片
     */
    private String img;
    /**
     * 系统标识
     */
    private String appId;
    /**
     * 国家
     */
    private String country;
    /**
     * 是否新车
     */
    private Integer isNewCar;
    /**
     * 状态
     */
    private String status;
    @TableField(exist = false)
    private String competitiveTypeName;
    @TableField(exist = false)
    private String isCoreName;
    @TableField(exist = false)
    private String statusName;
    @TableField(exist = false)
    private String brandName;
}
