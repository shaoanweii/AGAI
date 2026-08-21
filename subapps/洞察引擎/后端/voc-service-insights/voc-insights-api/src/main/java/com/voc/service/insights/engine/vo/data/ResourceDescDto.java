package com.voc.service.insights.engine.vo.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author leiww
 * @since 2024/04/09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDescDto implements Serializable {
    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private String id;
    /**
     * 资源id
     */
    @Schema(description = "资源id")
    private String resourceId;
    private String customer;
    /**
     * 资源详情
     */
    @Schema(description = "资源详情")
    private String name;
    /**
     * 状态：全部、已启用、未启用、已禁用
     */
    @Schema(description = "状态：全部、已启用、未启用、已禁用")
    private String status;

}
