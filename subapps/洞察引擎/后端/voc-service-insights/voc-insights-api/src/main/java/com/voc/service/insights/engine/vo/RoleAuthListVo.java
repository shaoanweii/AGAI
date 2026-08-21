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
public class RoleAuthListVo implements Serializable {

    @Schema(description = "菜单ID")
    String id;
    @Schema(description = "父级ID")
    String pid;
    @Schema(description = "icon")
    String icon;
    @Schema(description = "菜单名称")
    String name;
    @Schema(description = "路径")
    String path;
    @Schema(description = "路径")
    String apiPath;
    String permissionKey;
    Integer sort;
    List<RoleAuthListVo> children;



}
