package com.voc.service.insights.engine.vo;

import com.voc.service.insights.engine.api.annotation.Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/1 上午11:21
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsUserInfoVo implements Serializable {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 角色id
     */
    private String roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 系统图标
     */
    private String systemIcon;
    /**
     * 头像
     */
    private String headPortrait;
    /**
     * 系统名称
     */
    private String systemName;
    /**
     * 客户id集
     */

    private ConditionVo clientIds;
    /**
     * 是否管理员
     */
    private Boolean isAdmin;
    /**
     * 默认客户id
     */
    @Client
    private String defaultClientId;
    /**
     * 菜单
     */
    private List<InsRolePermissionVo> menus;
    /**
     * 按钮
     */
    private Set<String> button;
    private Set<String> drillDowns;
    /**
     * 工号
     */
    private String employeeId;
    /**
     * 姓名
     */
    private String name;
}
