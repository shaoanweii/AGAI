package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PermissionPageModel implements Serializable {

    @Schema(description = "菜单IdList")
    @NotEmpty(message = "菜单不能为空")
    String permissionId;

    @Schema(description = "默认值范围")
    private Object jsonObject;

    @Schema(description = "角色类型 1:用户 2:领导")
    String roleType;

}
