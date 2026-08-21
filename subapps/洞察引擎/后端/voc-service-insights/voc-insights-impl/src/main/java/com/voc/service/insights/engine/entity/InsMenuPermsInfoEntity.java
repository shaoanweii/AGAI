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
 * @创建时间: 2024/3/4 16:24
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "ins_menu_perms")
public class InsMenuPermsInfoEntity implements Serializable {
    /**
     * 主键id
     */
    private String id;

    /**
     * 用户标识
     */
    private String userId;

    /**
     * 菜单标识
     */
    private String menuId;

    /**
     * 访问权限: r:读取 w:写入
     */
    private String userPerms;

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
    private Integer delFlag;

    /**
     * 系统标识
     */
    private String appId;

    /**
     * 是否启用
     */
    private Integer enabled;
    /**
     * 菜单名称
     */
    @TableField(exist = false)
    private String menuName;
}
