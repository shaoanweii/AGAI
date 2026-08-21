package com.voc.service.insights.engine.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.voc.service.insights.engine.vo.BrandInfoVo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "ins_brand_info",autoResultMap = true)
public class InsBrandInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 编码
     */
    private String code;
    /**
     * 英文名称
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String nameEn;
    /**
     * 车企
     */
    private String automark;
    /**
     * 车企id
     */
    private String automarkId;
    /**
     * 展示图片
     */
    private String img;
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
    private List<BrandInfoVo> competitiveProduct;
    /**
     * 排序
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private int orderBy;
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

    private String updateUser;

    /**
     * 删除状态 0正常 1已删除
     */
    private Boolean delFlag;

    /**
     * 系统标识
     */
    private String appId;
    /**
     * 性质(自主、合资、进口)
     */
    private String nature;
    /**
     * 国家
     */
    private String country;
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

}
