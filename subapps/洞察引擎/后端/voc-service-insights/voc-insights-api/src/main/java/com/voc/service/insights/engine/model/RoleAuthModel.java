package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleAuthModel implements Serializable {

    @Schema(description = "角色Id编辑时必传")
    String id;
    @Schema(description = "客户ID必传")
    @NotBlank(message = "客户ID不能为空")
    String clientId;
    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    String roleName;
    @Schema(description = "菜单IdList")
    @NotEmpty(message = "菜单不能为空")
    List<String> permissionIdList;
    @Schema(description = "角色状态")
    Integer enabled;
    @Schema(description = "角色描述")
    private String remark;

    List<String> userIdList;


}
