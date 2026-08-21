package com.voc.service.insights.engine.model;

import com.voc.service.common.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InsMenuModel extends Page  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    private String id;
    @Schema(description = "父id")
    private String parentId;
    @Schema(description = "菜单标题")
    private String name;
    @Schema(description = "路径")
    private String htmlUri;
    @Schema(description = "接口路径")
    private String apiUri;
    @Schema(description = "菜单排序")
    private int sortNo;
    @Schema(description = "菜单图标")
    private String icon;
    @Schema(description = "是否路由菜单: 0:不是  1:是（默认值1）")
    private Boolean isRoute;
    @Schema(description = "是否叶子节点:      1:是   0:不是")
    private Boolean isLeaf;
    @Schema(description = "是否隐藏路由: 0否,1是")
    private Boolean hidden;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "创建人")
    private String operator;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "删除状态 0正常 1已删除")
    private String delFlag;
    @Schema(description = "系统标识")
    private String appId;
    @Schema(description = "菜单状态")
    private String enabled;
    @Schema(description = "国际化key")
    private String menuI18n;
    @Schema(description = "国际化key")
    private String userPerms;

}