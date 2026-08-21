package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("ins_menu")
public class InsMenuEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private String id;
    /**
     * 父id
     */
    private String parentId;
    /**
     * 菜单标题
     */
    private String name;
    /**
     * 前端路径
     */
    private String htmlUri;
    /**
     * 后端API访问路径
     */
    private String apiUri;
    /**
     * 菜单排序
     */
    private int sortNo;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 是否路由菜单: 0:不是  1:是（默认值1）
     */
    private Boolean isRoute;
    /**
     * 是否叶子节点:      1:是   0:不是
     */
    private Boolean isLeaf;
    /**
     * 是否隐藏路由: 0否,1是
     */
    private Boolean hidden;
    /**
     * 描述
     */
    private String description;
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
     * 删除状态 0正常 1已删除
     */
    private String delFlag;
    /**
     * 系统标识
     */
    private String appId;
    /**
     * 国际化key
     */
    private String menuI18n;
    /**
     * 接口路径
     */
    private String apiAddress;

    @TableField(exist = false)
    private String userPerms;

}
