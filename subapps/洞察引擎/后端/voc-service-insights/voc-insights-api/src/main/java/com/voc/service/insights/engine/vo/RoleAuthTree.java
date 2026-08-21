package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleAuthTree implements Serializable {

    @Schema(description = "菜单ID")
    String id;
    @Schema(description = "Code")
    String code;
    @Schema(description = "父级ID")
    String pid;
    @Schema(description = "icon")
    String icon;
    @Schema(description = "菜单名称")
    String name;
    @Schema(description = "路径")
    String path;
    String permissionKey;
    @Schema(description = "是否是按钮")
    Boolean checkButton;
    Integer sort;
    @Schema(description = "是否选中")
    Boolean checked = Boolean.FALSE;
    List<RoleAuthTree> children;
    String apiPath;


}
