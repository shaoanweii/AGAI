package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 13:04
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsPermissionsVo implements Serializable {
    @Schema(description = "id")
    private String id;
    @Schema(description = "userId")
    private String userId;
    @Schema(description = "菜单Id")
    private String menuId;
    @Schema(description = "菜单名称")
    private String menuName;
    @Schema(description = "是否有读权限")
    private Boolean read;
    @Schema(description = "读权限的名称")
    private String readName;
    @Schema(description = "是否有写权限")
    private Boolean write;
    @Schema(description = "写权限的名称")
    private String writeName;
}
