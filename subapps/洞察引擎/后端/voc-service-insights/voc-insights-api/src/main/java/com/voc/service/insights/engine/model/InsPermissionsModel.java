package com.voc.service.insights.engine.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 11:40
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsPermissionsModel  implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "菜单id")
    private String menuId;
    @Schema(description = "读权限")
    @Builder.Default
    private Boolean read = false;
    @Schema(description = "写权限")
    @Builder.Default
    private Boolean write = false;
}
