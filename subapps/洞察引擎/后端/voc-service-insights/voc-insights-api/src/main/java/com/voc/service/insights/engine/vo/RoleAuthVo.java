package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleAuthVo implements Serializable {

    @Schema(description = "角色Id编辑时必传")
    String id;
    @Schema(description = "角色名称")
    String roleName;
    @Schema(description = "菜单树")
    List<RoleAuthTree>  roleAuthTreeList;
    @Schema(description = "角色状态")
    Integer enabled;
    @Schema(description = "角色描述")
    String remark;



}
