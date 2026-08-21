package com.voc.service.insights.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 16:04
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsMenuPermsInfoModel  implements Serializable {
    /**
     * id
     */
    private String id;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 菜单id
     */
    private String menuId;
    /**
     * 访问权限: r:读取 w:写入 例如: 只读权限: r__
     */
    private String userPerms;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 系统标识
     */
    private String appId;
}
