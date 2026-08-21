package com.voc.service.insights.engine.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @创建者: fanrong
 * @创建时间: 2025/11/10 18:49
 * @描述:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsAccountVo implements Serializable {
    @Schema(description = "用户id")
    private String id;
    @Schema(description = "用户名称")
    private String name;
    @Schema(description = "员工编号")
    private String employeeId;
}
